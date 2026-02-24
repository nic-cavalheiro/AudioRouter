package com.audiorouter;

public class AudioConfig {
    // Configurações da Rede
    public static final int PORT = 4747;
    public static final int BUFFER_SIZE = 1024;

    // Configurações do Áudio (Obrigatório ser idêntico no Android)
    public static final float SAMPLE_RATE = 44100.0f;
    public static final int SAMPLE_SIZE_IN_BITS = 16;
    public static final int CHANNELS = 1; // Mono
    public static final boolean SIGNED = true;
    public static final boolean BIG_ENDIAN = false; // False = Little Endian (Padrão PCM Android/Windows)
}