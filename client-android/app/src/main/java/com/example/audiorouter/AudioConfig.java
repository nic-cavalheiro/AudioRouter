package com.example.audiorouter; // Ajuste para o seu pacote

import android.media.AudioFormat;

public class AudioConfig {
    public static final String SERVER_IP = "192.168.1.100"; // O IP do seu PC
    public static final int SERVER_PORT = 4747;
    public static final int SAMPLE_RATE = 44100;

    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BUFFER_SIZE = 512; // Usaremos esta constante em vez de método
}