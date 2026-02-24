package com.example.audiorouter; // Ajuste para o seu pacote

import android.media.AudioFormat;

public class AudioConfig {
    // Configurações da Rede
    public static final int PORT = 4747;
    public static final int BUFFER_SIZE = 1024; // Tamanho do pacote (Payload)

    // Configurações do Áudio
    public static final int SAMPLE_RATE = 44100;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
}