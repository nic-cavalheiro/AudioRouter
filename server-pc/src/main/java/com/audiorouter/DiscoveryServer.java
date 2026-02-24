package com.audiorouter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DiscoveryServer implements Runnable {
    
    // A mesma porta que o Android está usando para gritar
    private static final int DISCOVERY_PORT = 5001; 
    private volatile boolean running = true;

    @Override
    public void run() {
        // O bloco try-with-resources já fecha o socket automaticamente se der erro
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            System.out.println("[DISCOVERY] Servidor de busca ativo na porta " + DISCOVERY_PORT + "...");
            
            byte[] receiveBuffer = new byte[1024];
            
            while (running) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                
                // A thread fica travada (bloqueada) aqui até algum Android gritar na rede
                socket.receive(receivePacket); 
                
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                
                // Se o pacote for o código secreto do nosso app...
                if ("AUDIOROUTER_DISCOVER".equals(message)) {
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    
                    System.out.println("[DISCOVERY] Android encontrado no IP: " + clientAddress.getHostAddress());
                    
                    // Respondemos: "ESTOU AQUI!" para o IP exato do celular que chamou
                    byte[] sendData = "AUDIOROUTER_HERE".getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    socket.send(sendPacket);
                }
            }
        } catch (Exception e) {
            System.err.println("[DISCOVERY] Erro crítico no vigilante: " + e.getMessage());
        }
    }
    
    public void stop() {
        running = false;
    }
}