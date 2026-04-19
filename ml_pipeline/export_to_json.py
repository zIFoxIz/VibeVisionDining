"""
Export scikit-learn model to JSON format for Android compatibility
"""
import pickle
import json
import numpy as np

# Load the trained model and vectorizer
with open('sentiment_model.pkl', 'rb') as f:
    model = pickle.load(f)

with open('vectorizer.pkl', 'rb') as f:
    vectorizer = pickle.load(f)

print("Extracting model parameters...")

# Get model parameters
classes = model.classes_.tolist()  # ['negative', 'neutral', 'positive']
coef = model.coef_.astype(np.float32).tolist()  # Convert to float32 then list
intercept = model.intercept_.astype(np.float32).tolist()  # Convert to float32

# Get feature names
feature_names = vectorizer.get_feature_names_out().tolist()

# Get vocabulary and convert int64 to int
vocabulary = {k: int(v) for k, v in vectorizer.vocabulary_.items()}

print(f"Classes: {classes}")
print(f"Number of features: {len(feature_names)}")
print(f"Model coefficients shape: {np.array(coef).shape}")

# Create a minimal model with only top features for faster inference
print("Creating minimal model version...")

# Keep only top features (most important for classification)
feature_importance = np.abs(np.array(coef, dtype=np.float32)).max(axis=0)
top_indices = np.argsort(feature_importance)[-500:]  # Top 500 features

# Create minimal model suitable for Android
minimal_model = {
    'type': 'LogisticRegression_Minimal',
    'classes': classes,
    'feature_indices': top_indices.astype(int).tolist(),
    'coef': np.array(coef, dtype=np.float32)[:, top_indices].astype(np.float32).tolist(),
    'intercept': intercept,
    'top_features': [feature_names[i] for i in top_indices.tolist()],
}

minimal_file = 'sentiment_model_minimal.json'
with open(minimal_file, 'w') as f:
    json.dump(minimal_model, f)

print(f"Minimal model exported to {minimal_file}")
print(f"File size: {len(json.dumps(minimal_model)) / 1024:.2f} KB")
print(f"Top 500 features for faster inference")
print("For TensorFlow Lite conversion, run: python convert_to_tflite.py")
print("\nExport complete!")
