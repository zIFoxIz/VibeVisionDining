"""
VibeVision Dining - Sentiment Classification Model Training Pipeline
Trains a sentiment classifier on Yelp reviews using Logistic Regression.
Exports model weights and vectorizer for Android inference.
"""

import pandas as pd
import numpy as np
import os
import warnings
import json
import re
warnings.filterwarnings('ignore')

from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
import pickle

# ============================================================================
# STEP 1: Load and preprocess data
# ============================================================================

print("Loading Yelp dataset...")
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
DATASET_DIR = os.path.join(BASE_DIR, 'Dataset')
LEGACY_DATASET_PATH = os.path.join(DATASET_DIR, 'Yelp Restaurant Reviews.csv')
OPEN_BUSINESS_PATH = os.path.join(DATASET_DIR, 'yelp_academic_dataset_business.json')
OPEN_REVIEW_PATH = os.path.join(DATASET_DIR, 'yelp_academic_dataset_review.json')
ML_PIPELINE_DIR = os.path.dirname(__file__)
MAX_OPEN_REVIEWS = int(os.environ.get('YELP_MAX_REVIEWS', '300000'))

def load_restaurant_reviews_from_open_dataset(business_path, review_path, max_reviews):
    print("Using Yelp Open Dataset JSON files...")
    print("Scanning businesses to identify restaurants...")

    restaurant_ids = set()
    with open(business_path, 'r', encoding='utf-8') as business_file:
        for line in business_file:
            line = line.strip()
            if not line:
                continue
            business = json.loads(line)
            categories = (business.get('categories') or '').lower()
            if 'restaurant' in categories:
                business_id = business.get('business_id')
                if business_id:
                    restaurant_ids.add(business_id)

    print(f"Restaurant businesses found: {len(restaurant_ids):,}")
    print("Scanning reviews and keeping restaurant reviews...")

    rows = []
    with open(review_path, 'r', encoding='utf-8') as review_file:
        for line in review_file:
            line = line.strip()
            if not line:
                continue
            review = json.loads(line)
            if review.get('business_id') not in restaurant_ids:
                continue

            text = (review.get('text') or '').strip()
            stars = review.get('stars')
            if not text or stars is None:
                continue

            rows.append({'Rating': int(round(float(stars))), 'Review Text': text})
            if len(rows) >= max_reviews:
                break

    if not rows:
        raise ValueError('No restaurant reviews were loaded from Yelp Open Dataset JSON files.')

    print(f"Loaded {len(rows):,} restaurant reviews from Yelp Open Dataset")
    return pd.DataFrame(rows)


if os.path.exists(OPEN_BUSINESS_PATH) and os.path.exists(OPEN_REVIEW_PATH):
    data_source = 'yelp_open_json'
    df = load_restaurant_reviews_from_open_dataset(
        OPEN_BUSINESS_PATH,
        OPEN_REVIEW_PATH,
        MAX_OPEN_REVIEWS
    )
else:
    data_source = 'legacy_csv'
    print("Yelp Open Dataset JSON files not found; falling back to legacy CSV...")
    if not os.path.exists(LEGACY_DATASET_PATH):
        raise FileNotFoundError(
            "No dataset found. Add Yelp Open Dataset JSON files to Dataset/ or provide Yelp Restaurant Reviews.csv"
        )
    df = pd.read_csv(LEGACY_DATASET_PATH)

print(f"Dataset shape: {df.shape}")
print(f"Columns: {df.columns.tolist()}")

# Map ratings to sentiment labels
def map_rating_to_sentiment(rating):
    if rating <= 2:
        return 'negative'
    elif rating == 3:
        return 'neutral'
    else:  # 4-5
        return 'positive'

df = df.dropna(subset=['Rating', 'Review Text']).copy()
df['Rating'] = pd.to_numeric(df['Rating'], errors='coerce')
df = df[df['Rating'].notna()].copy()
df['Rating'] = df['Rating'].astype(int)
df['sentiment'] = df['Rating'].apply(map_rating_to_sentiment)
print(f"\nSentiment distribution:\n{df['sentiment'].value_counts()}")

# Remove rows with NaN review text
df = df.dropna(subset=['Review Text'])
print(f"Dataset after removing NaN: {df.shape}")

# Preprocess text: lowercase, strip whitespace
df['Review Text'] = df['Review Text'].str.lower().str.strip()

# ============================================================================
# STEP 2: Tokenization and stopword removal
# ============================================================================

from nltk.corpus import stopwords
import nltk

# Download required NLTK data
print("\nDownloading NLTK stopwords...")
try:
    nltk.data.find('corpora/stopwords')
except LookupError:
    nltk.download('stopwords')

stop_words = set(stopwords.words('english'))

def preprocess_text(text):
    """Tokenize (simple split) and remove stopwords"""
    # Convert to lowercase
    text = text.lower()
    # Split on punctuation/whitespace for cleaner tokenization
    tokens = re.split(r"[^a-z0-9]+", text)
    # Remove short tokens and stopwords
    tokens = [t for t in tokens if len(t) > 1 and t not in stop_words]
    return ' '.join(tokens)

print("Preprocessing text (tokenization + stopword removal)...")
df['processed_text'] = df['Review Text'].apply(preprocess_text)

# ============================================================================
# STEP 3: Train Logistic Regression Model (for comparison)
# ============================================================================

print("\n" + "="*70)
print("TRAINING LOGISTIC REGRESSION MODEL")
print("="*70)

X_train, X_test, y_train, y_test = train_test_split(
    df['processed_text'], 
    df['sentiment'], 
    test_size=0.2, 
    random_state=42,
    stratify=df['sentiment']
)

# TF-IDF vectorization
print("Vectorizing text with TF-IDF...")
vectorizer = TfidfVectorizer(max_features=5000, ngram_range=(1, 2))
X_train_vec = vectorizer.fit_transform(X_train)
X_test_vec = vectorizer.transform(X_test)

# Train Logistic Regression
print("Training Logistic Regression...")
lr_model = LogisticRegression(max_iter=200, random_state=42)
lr_model.fit(X_train_vec, y_train)

# Evaluate
y_pred_lr = lr_model.predict(X_test_vec)
lr_accuracy = accuracy_score(y_test, y_pred_lr)
print(f"\nLogistic Regression Accuracy: {lr_accuracy:.4f}")
print("\nClassification Report:")
print(classification_report(y_test, y_pred_lr))

# Save for later use in Android
with open(os.path.join(ML_PIPELINE_DIR, 'lr_model.pkl'), 'wb') as f:
    pickle.dump(lr_model, f)
with open(os.path.join(ML_PIPELINE_DIR, 'vectorizer.pkl'), 'wb') as f:
    pickle.dump(vectorizer, f)
print("Logistic Regression model saved to pkl files")

# ============================================================================
# STEP 4: Export Model Artifacts for Android
# ============================================================================

print("\n" + "="*70)
print("EXPORTING MODEL FOR ANDROID")
print("="*70)

# Save the trained model and vectorizer
pkl_dir = ML_PIPELINE_DIR

print("Saving Logistic Regression model...")
with open(os.path.join(pkl_dir, 'sentiment_model.pkl'), 'wb') as f:
    pickle.dump(lr_model, f)

print("Saving TF-IDF vectorizer...")
with open(os.path.join(pkl_dir, 'vectorizer.pkl'), 'wb') as f:
    pickle.dump(vectorizer, f)

# Export model parameters to JSON for lightweight Android integration
print("Exporting model metadata...")
model_metadata = {
    'model_type': 'LogisticRegression',
    'n_features': len(vectorizer.get_feature_names_out()),
    'classes': lr_model.classes_.tolist(),
    'accuracy': float(lr_accuracy),
    'test_size': len(X_test),
    'feature_extractor': 'TF-IDF'
}

with open(os.path.join(pkl_dir, 'model_metadata.json'), 'w') as f:
    json.dump(model_metadata, f, indent=2)

print("Model metadata saved to JSON")

# ============================================================================
# STEP 5: Summary
# ============================================================================

print("\n" + "="*70)
print("TRAINING COMPLETE - SUMMARY")
print("="*70)
print(f"Dataset Source: {data_source}")
print(f"Dataset Size: {len(df)} reviews")
print(f"Training Set: {len(X_train)} | Test Set: {len(X_test)}")
print(f"Logistic Regression Accuracy: {lr_accuracy:.4f}")
print(f"\nModel artifacts exported:")
print(f"  - sentiment_model.pkl (Logistic Regression classifier)")
print(f"  - vectorizer.pkl (TF-IDF vectorizer with {len(vectorizer.get_feature_names_out())} features)")
print(f"  - model_metadata.json (model info for Android)")
print(f"\nNLP Preprocessing Applied:")
print(f"  - Lowercase normalization")
print(f"  - Tokenization (word-level)")
print(f"  - Stopword removal (NLTK English stopwords)")
print(f"  - TF-IDF feature extraction (max 5000 features, unigrams + bigrams)")
print("="*70)
