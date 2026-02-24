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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {
    private SourceDataLine outputLine;

    public void start() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            // Busca o VB-Cable automaticamente usando o nosso Manager
            outputLine = AudioDeviceManager.getVBCableLine(format);
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o roteamento de áudio: " + e.getMessage());
        }
    }

    public void play(byte[] data, int length) {
        if (outputLine != null) {
            outputLine.write(data, 0, length);
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
