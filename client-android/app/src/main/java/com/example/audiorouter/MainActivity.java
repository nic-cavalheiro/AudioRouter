package com.example.audiorouter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private EditText ipInput;
    private Button btnToggle;
    
    private AudioRecorder recorder;
    private UdpSender sender;
    private boolean isStreaming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout Simples via Código
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 60, 60, 60);

        ipInput = new EditText(this);
        ipInput.setHint("Digite o IP do PC (ex: 192.168.0.10)");
        
        btnToggle = new Button(this);
        btnToggle.setText("INICIAR TRANSMISSÃO");

        layout.addView(ipInput);
        layout.addView(btnToggle);
        setContentView(layout);

        btnToggle.setOnClickListener(v -> toggleStreaming());
    }

    private void toggleStreaming() {
        if (isStreaming) {
            stopStreaming();
        } else {
            // Verifica permissão antes de iniciar
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                return;
            }
            
            String ip = ipInput.getText().toString();
            if (ip.isEmpty()) {
                Toast.makeText(this, "Por favor, digite o IP do PC.", Toast.LENGTH_SHORT).show();
                return;
            }
            startStreaming(ip);
        }
    }

    private void startStreaming(String ip) {
        try {
            // 1. Cria a conexão de rede
            sender = new UdpSender(ip, AudioConfig.PORT);

            // 2. Cria o gravador e injeta a dependência do envio
            // Aqui acontece a mágica: O recorder chama sender.send() a cada buffer cheio
            recorder = new AudioRecorder((data, length) -> sender.send(data, length));

            // 3. Inicia
            recorder.start();
            
            isStreaming = true;
            btnToggle.setText("PARAR TRANSMISSÃO");
            ipInput.setEnabled(false); // Trava o IP durante o uso

        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void stopStreaming() {
        if (recorder != null) recorder.stop();
        if (sender != null) sender.close();
        
        isStreaming = false;
        btnToggle.setText("INICIAR TRANSMISSÃO");
        ipInput.setEnabled(true);
    }
}