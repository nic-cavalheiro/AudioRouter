// package com.audiorouter;

// import javax.sound.sampled.AudioFormat;
// import javax.sound.sampled.AudioSystem;
// import javax.sound.sampled.DataLine;
// import javax.sound.sampled.LineUnavailableException;
// import javax.sound.sampled.SourceDataLine;

// public class AudioPlayer {
//     private SourceDataLine speakers;

//     public void start() throws LineUnavailableException {
//         AudioFormat format = new AudioFormat(
//             AudioConfig.SAMPLE_RATE,
//             AudioConfig.SAMPLE_SIZE_IN_BITS,
//             AudioConfig.CHANNELS,
//             AudioConfig.SIGNED,
//             AudioConfig.BIG_ENDIAN
//         );

//         DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        
//         if (!AudioSystem.isLineSupported(info)) {
//             throw new LineUnavailableException("Formato de áudio não suportado pelo sistema.");
//         }

//         speakers = (SourceDataLine) AudioSystem.getLine(info);
//         speakers.open(format);
//         speakers.start();
        
//         System.out.println("[AudioPlayer] Alto-falantes inicializados com sucesso.");
//     }

//     // Método exposto para receber os dados limpos
//     public void play(byte[] data, int length) {
//         if (speakers != null && speakers.isOpen()) {
//             speakers.write(data, 0, length);
//         }
//     }

//     public void stop() {
//         if (speakers != null) {
//             speakers.drain();
//             speakers.close();
//             System.out.println("[AudioPlayer] Alto-falantes liberados.");
//         }
//     }
// }
package com.audiorouter;

import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

//import main.java.com.audiorouter.AudioDeviceManager;

public class AudioPlayer {
    private SourceDataLine outputLine;
    private Consumer<byte[]> onBufferDone; // Callback para devolver o buffer

    public void start() {
            try {
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                // Busca o VB-Cable
                outputLine = AudioDeviceManager.getVBCableLine(format);
                
                if (outputLine != null) {
                    // A CORREÇÃO LÓGICA: Tem que abrir e dar o play na placa de som!
                    if (!outputLine.isOpen()) {
                        outputLine.open(format);
                    }
                    outputLine.start(); // <-- Sem isso, o som NÃO SAI!
                    System.out.println("[AudioPlayer] Placa de som ativada e escutando...");
                } else {
                    System.err.println("[AudioPlayer] FALHA: VB-Cable não encontrado.");
                }
            } catch (Exception e) {
                System.err.println("Erro ao iniciar o roteamento de áudio: " + e.getMessage());
            }
        }
    
    public void setBufferReleaseListener(Consumer<byte[]> listener) {
        this.onBufferDone = listener;
    }

    public void play(byte[] data, int length) {
        
        if (outputLine != null) {
            outputLine.write(data, 0, length);
        }
        if (onBufferDone != null) {
                onBufferDone.accept(data);
            }
    }

    public void stop() {
        if (outputLine != null) {
            outputLine.drain();
            outputLine.stop();
            outputLine.close();
            outputLine = null;
            System.out.println("[AudioPlayer] Linha de áudio fechada e liberada.");
        }
    }
}
