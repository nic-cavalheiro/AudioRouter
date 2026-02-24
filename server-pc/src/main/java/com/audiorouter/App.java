// package com.audiorouter;

// public class App {
//     public static void main(String[] args) {
//         System.out.println("=== AudioRouter PC (Modo Receptor) ===");

//         AudioPlayer player = new AudioPlayer();
        
//         try {
//             // 1. Liga o hardware de áudio
//             player.start();

//             // 2. Cria o receptor de rede e passa o método do player como Callback
//             UdpReceiver receiver = new UdpReceiver(player::play);

//             // 3. Roda o receptor em uma Thread separada para não travar o sistema principal
//             Thread networkThread = new Thread(receiver);
//             networkThread.start();

//             // Hook para desligar graciosamente se o usuário der Ctrl+C no terminal
//             Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                 receiver.stop();
//                 player.stop();
//             }));

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }

package com.audiorouter;

public class App {
    public static void main(String[] args) {
        System.out.println("=== AudioRouter PC (Modo Receptor) ===");

        try {
            AudioPlayer player = new AudioPlayer();
            UdpReceiver receiver = new UdpReceiver(player::play);
            player.setBufferReleaseListener(receiver::releaseBuffer);
            DiscoveryServer discovery = new DiscoveryServer();

            // PASSO 1: Iniciar o Discovery (Porta 5001)
            System.out.println("[DEBUG 1] Iniciando Discovery...");
            new Thread(discovery).start();

            // PASSO 2: Iniciar o Receptor de Áudio (Porta 5000)
            // Iniciamos a rede antes do hardware para evitar travamentos
            System.out.println("[DEBUG 2] Iniciando Receptor de Rede...");
            new Thread(receiver).start();

            // PASSO 3: Iniciar o Hardware de Som
            // Se isso travar, a rede já estará rodando em paralelo
            System.out.println("[DEBUG 3] Iniciando Placa de Som...");
            player.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[SISTEMA] Encerrando...");
                receiver.stop();
                player.stop();
                discovery.stop();
            }));

        } catch (Exception e) {
            System.err.println("[ERRO CRÍTICO] Falha na inicialização: " + e.getMessage());
            e.printStackTrace();
        }
    }
}