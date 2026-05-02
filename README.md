# VibeVision Dining

![Repo](https://img.shields.io/badge/repo-VibeVisionDining-1f6feb)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84)
![Language](https://img.shields.io/badge/Kotlin-Compose-7F52FF)
![ML](https://img.shields.io/badge/ML-Logistic%20Regression-orange)
![Status](https://img.shields.io/badge/status-v1.0%20complete-brightgreen)

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

Google Maps API setup:

1. Open Google Cloud Console and create or select a project.
2. Enable billing for the project.
3. Enable APIs: Maps SDK for Android and Places API (optional but recommended for restaurant search).
4. Create an API key.
5. Restrict the key to Android apps and set your package name plus SHA-1 certificate fingerprint.
6. Add the key to android/local.properties:
  MAPS_API_KEY=YOUR_REAL_KEY
7. For Google Places HTTP search used by the Search screen, add a separate web-service key:
  PLACES_WEB_API_KEY=YOUR_PLACES_WEB_SERVICE_KEY
7. Sync Gradle and run the app.

Notes:

- local.properties is ignored by git in this repository, so your key will not be committed.
- You can also set MAPS_API_KEY as an environment variable instead of local.properties.
- You can also set PLACES_WEB_API_KEY as an environment variable.

Firebase Auth setup:

1. Create or select a Firebase project.
2. Add Android app package `com.example.vibevision` in Firebase project settings.
3. Download `google-services.json` and place it at `android/google-services.json`.
4. In Firebase Authentication, enable providers:
  - Email/Password
  - Anonymous
  - Google
5. From Firebase project settings, copy the Web client ID and add to `android/local.properties`:
  FIREBASE_WEB_CLIENT_ID=YOUR_WEB_CLIENT_ID
6. Sync Gradle and run the app.

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
