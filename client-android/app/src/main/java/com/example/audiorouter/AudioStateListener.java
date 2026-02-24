package com.example.audiorouter;

public interface AudioStateListener {
    void onTransmissionStateChanged(boolean isTransmitting);
    void onError(String message);
}