"""
Convert trained scikit-learn Logistic Regression sentiment model to TensorFlow Lite.

Output:
- sentiment_model.tflite
- sentiment_model_tflite_metadata.json
"""

import json
import os
import pickle

import numpy as np


def main() -> None:
    try:
        import tensorflow as tf
    except ImportError as exc:
        raise RuntimeError(
            "TensorFlow is required for TFLite conversion. Install with: pip install tensorflow"
        ) from exc

    base_dir = os.path.dirname(__file__)
    model_path = os.path.join(base_dir, "sentiment_model.pkl")
    vectorizer_path = os.path.join(base_dir, "vectorizer.pkl")

    with open(model_path, "rb") as f:
        model = pickle.load(f)

    with open(vectorizer_path, "rb") as f:
        vectorizer = pickle.load(f)

    classes = model.classes_.tolist()
    feature_names = vectorizer.get_feature_names_out().tolist()

    coef = model.coef_.astype(np.float32)
    intercept = model.intercept_.astype(np.float32)

    feature_importance = np.abs(coef).max(axis=0)
    top_indices = np.argsort(feature_importance)[-500:]

    top_features = [feature_names[i] for i in top_indices.tolist()]
    top_coef = coef[:, top_indices].astype(np.float32)

    keras_model = tf.keras.Sequential(
        [
            tf.keras.layers.Input(shape=(len(top_features),), dtype=tf.float32),
            tf.keras.layers.Dense(len(classes), activation="softmax"),
        ]
    )

    dense = keras_model.layers[0]
    dense.set_weights([top_coef.T, intercept])

    converter = tf.lite.TFLiteConverter.from_keras_model(keras_model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    tflite_path = os.path.join(base_dir, "sentiment_model.tflite")
    with open(tflite_path, "wb") as f:
        f.write(tflite_model)

    metadata = {
        "model_type": "tflite_dense_softmax",
        "classes": classes,
        "top_features": top_features,
        "n_features": len(top_features),
    }

    metadata_path = os.path.join(base_dir, "sentiment_model_tflite_metadata.json")
    with open(metadata_path, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)

    print(f"Saved: {tflite_path}")
    print(f"Saved: {metadata_path}")


if __name__ == "__main__":
    main()
