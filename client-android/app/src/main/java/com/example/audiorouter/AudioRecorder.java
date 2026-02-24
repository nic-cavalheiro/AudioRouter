package com.example.audiorouter;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecorder {
    
    public interface AudioDataCallback {
        void onDataReady(byte[] data, int length);
    }

    private AudioRecord recorder;
    private Thread recordingThread;
    private volatile boolean isRecording = false;
    private final AudioDataCallback callback;

    public AudioRecorder(AudioDataCallback callback) {
        this.callback = callback;
    }

    @SuppressLint("MissingPermission")
    public void start() {
        if (isRecording) return;

        int minBufferSize = AudioRecord.getMinBufferSize(
            AudioConfig.SAMPLE_RATE,
            AudioConfig.CHANNEL_CONFIG,
            AudioConfig.AUDIO_FORMAT
        );

        int finalBufferSize = Math.max(minBufferSize, AudioConfig.BUFFER_SIZE);

        recorder = new AudioRecord(
            MediaRecorder.AudioSource.MIC,
            AudioConfig.SAMPLE_RATE,
            AudioConfig.CHANNEL_CONFIG,
            AudioConfig.AUDIO_FORMAT,
            finalBufferSize
        );

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecorder", "Falha ao inicializar hardware de áudio!");
            return;
        }

        isRecording = true;
        recorder.startRecording();

        recordingThread = new Thread(this::recordingLoop, "AudioCaptureThread");
        recordingThread.start();
    }

    private void recordingLoop() {
        byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];

        // O loop verifica a flag volatile
        while (isRecording) {
            if (recorder == null) break;

            // Bloqueia por alguns milissegundos para ler a voz
            int bytesRead = recorder.read(buffer, 0, buffer.length);

            // Se ainda estiver gravando após a leitura, envia pro UDP
            if (bytesRead > 0 && isRecording) {
                callback.onDataReady(buffer, bytesRead);
            }
        }

        // --- O SEGREDO ESTÁ AQUI ---
        // A própria thread de áudio se encarrega de desligar a luz antes de sair
        if (recorder != null) {
            try {
                if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop();
                }
                recorder.release();
            } catch (Exception e) {
                Log.e("AudioRecorder", "Erro ao liberar hardware", e);
            }
            recorder = null;
        }
    }

    public void stop() {
        // A MainActivity só faz isso: vira a chave.
        // O loop while vai quebrar no próximo milissegundo e liberar o microfone de forma segura.
        isRecording = false; 
    }
}