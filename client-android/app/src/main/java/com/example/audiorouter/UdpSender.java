package com.example.audiorouter;

import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender {
    private DatagramSocket socket;
    private InetAddress destinationAddress;
    private final int port;

    public UdpSender(String ip, int port) throws Exception {
        this.port = port;
        this.destinationAddress = InetAddress.getByName(ip);
        this.socket = new DatagramSocket(); // Porta local aleatória
    }

    // Método "Fire and Forget" de alta performance
    public void send(byte[] data, int length) {
        try {
            if (socket != null && !socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(data, length, destinationAddress, port);
                socket.send(packet);
            }
        } catch (Exception e) {
            // Em app de áudio real, evitamos logar a cada erro de pacote para não travar a thread
            // Log.e("UdpSender", "Falha no envio", e);
        }
    }

    public void close() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}