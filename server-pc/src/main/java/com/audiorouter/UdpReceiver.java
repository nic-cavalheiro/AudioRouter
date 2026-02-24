package com.audiorouter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.function.BiConsumer;

public class UdpReceiver implements Runnable {
    
    // O Callback: Repassa um Array de Bytes e o Tamanho dele
    private final BiConsumer<byte[], Integer> onPacketReceived;
    private volatile boolean isRunning;

    public UdpReceiver(BiConsumer<byte[], Integer> onPacketReceived) {
        this.onPacketReceived = onPacketReceived;
    }

    @Override
    public void run() {
        isRunning = true;
        try (DatagramSocket socket = new DatagramSocket(AudioConfig.PORT)) {
            
            byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("[UdpReceiver] Escutando Wi-Fi na porta UDP " + AudioConfig.PORT + "...");

            while (isRunning) {
                socket.receive(packet);
                // Quando o pacote chega, joga imediatamente para a função de Callback
                onPacketReceived.accept(packet.getData(), packet.getLength());
            }

        } catch (Exception e) {
            if (isRunning) {
                System.err.println("[UdpReceiver] Erro na rede: " + e.getMessage());
            }
        }
    }

    public void stop() {
        isRunning = false;
        System.out.println("[UdpReceiver] Desligando receptor de rede.");
    }
}