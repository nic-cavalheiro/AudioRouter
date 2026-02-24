package com.example.audiorouter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class ServerDiscoverer {

    // Interface para avisar a View
    public interface DiscoveryListener {
        void onServerFound(String ip);
        void onError(String message);
    }

    public void discover(DiscoveryListener listener) {
        new Thread(() -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true); // Permite enviar para toda a rede
                socket.setSoTimeout(3000); // Espera no máximo 3 segundos

                // Grita "ONDE ESTÁ O PC?" na porta 5001
                byte[] sendData = "AUDIOROUTER_DISCOVER".getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), 5001);
                socket.send(sendPacket);

                // Fica escutando a resposta
                byte[] recvBuf = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(receivePacket);

                // Se recebeu, pega o IP e avisa a UI
                String ip = receivePacket.getAddress().getHostAddress();
                listener.onServerFound(ip);

            } catch (SocketTimeoutException e) {
                listener.onError("Servidor não encontrado. Tente inserir manualmente.");
            } catch (Exception e) {
                listener.onError("Erro na busca: " + e.getMessage());
            } finally {
                if (socket != null) socket.close();
            }
        }).start();
    }
}