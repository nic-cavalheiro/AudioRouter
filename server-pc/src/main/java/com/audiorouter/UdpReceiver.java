// package com.audiorouter;

// import java.net.DatagramPacket;
// import java.net.DatagramSocket;
// import java.util.function.BiConsumer;

// public class UdpReceiver implements Runnable {
    
//     // O Callback: Repassa um Array de Bytes e o Tamanho dele
//     private final BiConsumer<byte[], Integer> onPacketReceived;
//     private volatile boolean isRunning;

//     public UdpReceiver(BiConsumer<byte[], Integer> onPacketReceived) {
//         this.onPacketReceived = onPacketReceived;
//     }

//     @Override
//     public void run() {
//         isRunning = true;
//         try (DatagramSocket socket = new DatagramSocket(AudioConfig.PORT)) {
            
//             byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];
//             DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

//             System.out.println("[UdpReceiver] Escutando Wi-Fi na porta UDP " + AudioConfig.PORT + "...");

//             while (isRunning) {
//                 socket.receive(packet);
//                 System.out.println("Pacote de áudio recebido! Tamanho: " + packet.getLength() + " bytes");
//                 // Quando o pacote chega, joga imediatamente para a função de Callback
//                 onPacketReceived.accept(packet.getData(), packet.getLength());
//             }

//         } catch (Exception e) {
//             if (isRunning) {
//                 System.err.println("[UdpReceiver] Erro na rede: " + e.getMessage());
//             }
//         }
//     }

//     public void stop() {
//         isRunning = false;
//         System.out.println("[UdpReceiver] Desligando receptor de rede.");
//     }
// }

package com.audiorouter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;

public class UdpReceiver implements Runnable {
    private final BiConsumer<byte[], Integer> onPacketReceived;
    private volatile boolean isRunning;
    
    // Pool de buffers para evitar GC (Garbage Collection)
    // Criamos 100 "baldes" de 1024 bytes já prontos na memória
    private final BlockingQueue<byte[]> bufferPool = new ArrayBlockingQueue<>(100);

    public UdpReceiver(BiConsumer<byte[], Integer> onPacketReceived) {
        this.onPacketReceived = onPacketReceived;
        for (int i = 0; i < 100; i++) {
            bufferPool.add(new byte[AudioConfig.BUFFER_SIZE]);
        }
    }

    @Override
    public void run() {
        isRunning = true;
        try (DatagramSocket socket = new DatagramSocket(AudioConfig.PORT)) {

            System.out.println("[UdpReceiver] Porta " + AudioConfig.PORT + " aberta. Aguardando áudio do Android...");
            
            while (isRunning) {
                // 1. Pegamos um buffer vazio do Pool (Alta performance, zero alocação)
                byte[] buffer = bufferPool.take(); 
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                //System.out.println("[REDE] Recebi " + packet.getLength() + " bytes de: " + packet.getAddress().getHostAddress());
                
                int len = packet.getLength();
                if (len > 0) {
                    // 2. Enviamos para o Player. 
                    // IMPORTANTE: O Player deve devolver o buffer para o pool após tocar!
                    onPacketReceived.accept(buffer, len);
                } else {
                    bufferPool.offer(buffer); // Devolve se o pacote for inválido
                }
            }
        } catch (Exception e) {
            System.err.println("[UdpReceiver] Erro: " + e.getMessage());
        }
    }

    public void releaseBuffer(byte[] buffer) {
        bufferPool.offer(buffer);
    }

    public void stop() { isRunning = false; }
}