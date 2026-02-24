
package com.example.audiorouter;
import android.util.Log;

public class AudioController {
    private static final String TAG = "AudioRouter_Flow";
    private AudioRecorder audioRecorder;
    private UdpSender udpSender;
    private AudioStateListener stateListener;
    private boolean isTransmitting = false;

    // Construtor: Apenas guarda o listener. Não cria rede aqui!
    public AudioController(AudioStateListener listener) {
        this.stateListener = listener;
    }

    // Agora exige o IP para decidir o que fazer
    public void toggleTransmission(String serverIp) {
        Log.i(TAG, "AudioController: toggleTransmission() acionado");
        if (isTransmitting) {
            stop();
        } else {
            start(serverIp);
        }
    }

    // Cria as instâncias com o IP fresquinho vindo da interface
    private void start(String serverIp) {
        try {
            this.udpSender = new UdpSender(serverIp, AudioConfig.SERVER_PORT);
            this.audioRecorder = new AudioRecorder(udpSender);
            
            audioRecorder.start();
            isTransmitting = true;
            if (stateListener != null) stateListener.onTransmissionStateChanged(true);
        } catch (Exception e) {
            Log.e(TAG, "Falha ao inicializar componentes: " + e.getMessage());
            if (stateListener != null) stateListener.onError("Erro ao iniciar: " + e.getMessage());
        }
    }

    private void stop() {
        if (audioRecorder != null) {
            audioRecorder.stop();
        }
        if (udpSender != null) {
            udpSender.close();
        }
        isTransmitting = false;
        if (stateListener != null) stateListener.onTransmissionStateChanged(false);
    }

    public void release() {
        stop();
    }
}