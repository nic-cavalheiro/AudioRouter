package com.audiorouter;

public class App {
    public static void main(String[] args) {
        System.out.println("=== AudioRouter PC (Modo Receptor) ===");

        AudioPlayer player = new AudioPlayer();
        
        try {
            // 1. Liga o hardware de áudio
            player.start();

            // 2. Cria o receptor de rede e passa o método do player como Callback
            UdpReceiver receiver = new UdpReceiver(player::play);

            // 3. Roda o receptor em uma Thread separada para não travar o sistema principal
            Thread networkThread = new Thread(receiver);
            networkThread.start();

            // Hook para desligar graciosamente se o usuário der Ctrl+C no terminal
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                receiver.stop();
                player.stop();
            }));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}