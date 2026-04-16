# VibeVision Dining

![Repo](https://img.shields.io/badge/repo-VibeVisionDining-1f6feb)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84)
![Language](https://img.shields.io/badge/Kotlin-Compose-7F52FF)
![ML](https://img.shields.io/badge/ML-Logistic%20Regression-orange)
![Status](https://img.shields.io/badge/status-active%20development-blue)

VibeVision Dining is an Android application in active development that analyzes restaurant review text and predicts sentiment as negative, neutral, or positive.

This repository validates that the selected tech stack works end-to-end across data preparation, model training, model export, and on-device Android inference.

## Key Highlights

- On-device sentiment inference with no backend dependency
- 3-class prediction output: negative, neutral, positive
- Confidence and class score distribution shown in the UI
- Python ML pipeline for retraining and exporting model artifacts
- Lightweight JSON model loaded from Android assets

## Current Validation Results

From the current model metadata:

- Model type: Logistic Regression
- Feature extractor: TF-IDF
- Number of features: 5000
- Classes: negative, neutral, positive
- Accuracy: 0.8661
- Test set size: 3980

## Tech Stack

- Android app: Kotlin, Jetpack Compose
- ML training: Python, pandas, scikit-learn, NLTK
- Model export: Python JSON export workflow for mobile integration
- Data source: Yelp Restaurant Reviews dataset included in this repository

## Project Structure

- android
  - Main Android application source
  - Sentiment analyzer implementation
  - Compose UI and view model
  - Model asset: sentiment_model_minimal.json
- ml_pipeline
  - Model training script
  - JSON export script
  - Model metadata and minimal model artifacts
- Dataset
  - Yelp Restaurant Reviews.csv

## Run and Develop

Prerequisites:

- Android SDK and Android build tools
- JDK for Android builds
- Python 3 environment for ML pipeline work

Android app workflow:

1. Open the project folder in your IDE.
2. Ensure the Android environment is configured.
3. Build and run the app on an emulator or connected Android device.

ML pipeline workflow:

1. Create and activate a Python virtual environment.
2. Install required Python libraries such as pandas, scikit-learn, and nltk.
3. Run ml_pipeline/train_sentiment_model.py to retrain.
4. Run ml_pipeline/export_to_json.py to regenerate export artifacts.
5. Copy the generated minimal JSON into android/assets when updating the in-app model.

## Current Scope

This repository currently demonstrates a working end-to-end stack validation for:

- Data to model training
- Model to Android export
- Android app inference and UI output

Production hardening items such as broader testing, advanced analytics, and larger feature set expansion are planned as next-stage development work.

## Author

zIFoxIz
