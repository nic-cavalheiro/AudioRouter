package com.audiorouter;

import javax.sound.sampled.*;

public class AudioDeviceManager {

    public static SourceDataLine getVBCableLine(AudioFormat format) throws LineUnavailableException {
        return findCable(format, false);
    }

    private static SourceDataLine findCable(AudioFormat format, boolean isRetry) throws LineUnavailableException {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            String name = mixerInfo.getName().toLowerCase();
            if (name.contains("cable input") || name.contains("vb-audio")) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, format);

                if (mixer.isLineSupported(lineInfo)) {
                    SourceDataLine line = (SourceDataLine) mixer.getLine(lineInfo);
                    line.open(format);
                    line.start();
                    System.out.println("[AudioDeviceManager] Conectado com sucesso ao: " + mixerInfo.getName());
                    return line;
                }
            }
        }
        
        // NOVO RAMO: Se não achou e ainda não tentamos instalar
        if (!isRetry) {
            System.out.println("[AudioDeviceManager] VB-Cable não detectado no sistema.");
            boolean installed = VBCableInstaller.downloadAndInstall();
            
            if (installed) {
                System.out.println("[AudioDeviceManager] Reiniciando busca após instalação...");
                return findCable(format, true); // Chama a si mesmo novamente para a 2ª tentativa
            }
        }

        throw new RuntimeException("Falha crítica: O áudio não pode ser roteado. Instale o VB-Cable manualmente.");
    }
}