// package com.example.audiorouter;

// import android.Manifest;
// import android.content.pm.PackageManager;
// import android.os.Bundle;
// import android.widget.Button;
// import android.widget.EditText;
// import android.widget.LinearLayout;
// import android.widget.Toast;
// import androidx.appcompat.app.AppCompatActivity;
// import androidx.core.app.ActivityCompat;

// public class MainActivity extends AppCompatActivity {

//     private EditText ipInput;
//     private Button btnToggle;
    
//     private AudioRecorder recorder;
//     private UdpSender sender;
//     private boolean isStreaming = false;

//     @Override
//     protected void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);

//         // Layout Simples via Código
//         LinearLayout layout = new LinearLayout(this);
//         layout.setOrientation(LinearLayout.VERTICAL);
//         layout.setPadding(60, 60, 60, 60);

//         ipInput = new EditText(this);
//         ipInput.setHint("Digite o IP do PC (ex: 192.168.0.10)");
        
//         btnToggle = new Button(this);
//         btnToggle.setText("INICIAR TRANSMISSÃO");

//         layout.addView(ipInput);
//         layout.addView(btnToggle);
//         setContentView(layout);

//         btnToggle.setOnClickListener(v -> toggleStreaming());
//     }

//     private void toggleStreaming() {
//         if (isStreaming) {
//             stopStreaming();
//         } else {
//             // Verifica permissão antes de iniciar
//             if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
//                 return;
//             }
            
//             String ip = ipInput.getText().toString();
//             if (ip.isEmpty()) {
//                 Toast.makeText(this, "Por favor, digite o IP do PC.", Toast.LENGTH_SHORT).show();
//                 return;
//             }
//             startStreaming(ip);
//         }
//     }

//     private void startStreaming(String ip) {
//         try {
//             // 1. Cria a conexão de rede
//             sender = new UdpSender(ip, AudioConfig.PORT);

//             // 2. Cria o gravador e injeta a dependência do envio
//             // Aqui acontece a mágica: O recorder chama sender.send() a cada buffer cheio
//             recorder = new AudioRecorder((data, length) -> sender.send(data, length));

//             // 3. Inicia
//             recorder.start();
            
//             isStreaming = true;
//             btnToggle.setText("PARAR TRANSMISSÃO");
//             ipInput.setEnabled(false); // Trava o IP durante o uso

//         } catch (Exception e) {
//             Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
//             e.printStackTrace();
//         }
//     }

//     private void stopStreaming() {
//         if (recorder != null) recorder.stop();
//         if (sender != null) sender.close();
        
//         isStreaming = false;
//         btnToggle.setText("INICIAR TRANSMISSÃO");
//         ipInput.setEnabled(true);
//     }
// }

// package com.example.audiorouter;

// import android.Manifest;
// import android.content.pm.PackageManager;
// import android.graphics.Color;
// import android.os.Bundle;
// import android.util.Log;
// import android.widget.Button;
// import android.widget.EditText;
// import android.widget.TextView;
// import android.widget.Toast;
// import androidx.appcompat.app.AppCompatActivity;
// import androidx.core.app.ActivityCompat;
// import androidx.core.content.ContextCompat;

// public class MainActivity extends AppCompatActivity implements AudioStateListener {

//     private static final int PERMISSION_REQ_CODE = 1;
    
//     private Button btnIniciar;
//     private Button btnParar;
//     private Button btnBuscarPc;
//     private TextView txtStatus;
//     private EditText editIpAddress;
    
//     private AudioController audioController;
//     private ServerDiscoverer serverDiscoverer;

//     @Override
//     protected void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         setContentView(R.layout.activity_main);

//         // 1. Vinculação com o XML
//         btnIniciar = findViewById(R.id.btnIniciar);
//         btnParar = findViewById(R.id.btnParar);
//         btnBuscarPc = findViewById(R.id.btnBuscarPc);
//         txtStatus = findViewById(R.id.txtStatus);
//         editIpAddress = findViewById(R.id.editIpAddress);

//         // 2. Instanciação das camadas lógicas
//         audioController = new AudioController(this);
//         serverDiscoverer = new ServerDiscoverer();

//         // 3. Checagem de Permissão de Microfone
//         if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//             ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQ_CODE);
//         }

//         // 4. Lógica do botão BUSCAR (Broadcast UDP)
//         btnBuscarPc.setOnClickListener(v -> {
//             btnBuscarPc.setEnabled(false);
//             btnBuscarPc.setText("Buscando...");
            
//             serverDiscoverer.discover(new ServerDiscoverer.DiscoveryListener() {
//                 @Override
//                 public void onServerFound(String ip) {
//                     runOnUiThread(() -> {
//                         editIpAddress.setText(ip);
//                         btnBuscarPc.setEnabled(true);
//                         btnBuscarPc.setText("BUSCAR");
//                         Toast.makeText(MainActivity.this, "PC Encontrado!", Toast.LENGTH_SHORT).show();
//                     });
//                 }

//                 @Override
//                 public void onError(String message) {
//                     runOnUiThread(() -> {
//                         btnBuscarPc.setEnabled(true);
//                         btnBuscarPc.setText("BUSCAR");
//                         Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
//                     });
//                 }
//             });
//         });

//         // 5. Lógica do botão INICIAR (Lê o IP e inicia o Controller)
//         btnIniciar.setOnClickListener(v -> {
//             // Verifica se a permissão foi dada antes de tentar gravar
//             if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                 Toast.makeText(this, "Permissão de microfone necessária!", Toast.LENGTH_LONG).show();
//                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQ_CODE);
//                 return;
//             }

//             String ipAlvo = editIpAddress.getText().toString().trim();
//             if (ipAlvo.isEmpty()) {
//                 Toast.makeText(this, "Digite o IP do PC ou clique em Buscar!", Toast.LENGTH_SHORT).show();
//                 return;
//             }
//             audioController.toggleTransmission(ipAlvo);
//         });

//         // 6. Lógica do botão PARAR
//         btnParar.setOnClickListener(v -> audioController.toggleTransmission(""));
        
//         // 7. Configuração inicial das cores e estado
//         onTransmissionStateChanged(false); 
//     }

//     // --- Implementação da Interface AudioStateListener ---

//     @Override
//     public void onTransmissionStateChanged(boolean isTransmitting) {
//         Log.i("AudioRouter_Flow", "UI: Estado alterado para " + isTransmitting);
        
//         runOnUiThread(() -> {
//             if (isTransmitting) {
//                 // Estado: Gravando e Enviando
//                 btnIniciar.setEnabled(false);
//                 btnIniciar.setAlpha(0.5f);
//                 btnParar.setEnabled(true);
//                 btnParar.setAlpha(1.0f);
//                 editIpAddress.setEnabled(false); // Trava o campo de IP
//                 txtStatus.setText("● TRANSMITINDO");
//                 txtStatus.setTextColor(Color.parseColor("#4CAF50")); // Verde
//             } else {
//                 // Estado: Parado
//                 btnIniciar.setEnabled(true);
//                 btnIniciar.setAlpha(1.0f);
//                 btnParar.setEnabled(false);
//                 btnParar.setAlpha(0.5f);
//                 editIpAddress.setEnabled(true); // Libera o campo de IP
//                 txtStatus.setText("○ DESCONECTADO");
//                 txtStatus.setTextColor(Color.parseColor("#808080")); // Cinza
//             }
//         });
//     }

//     @Override
//     public void onError(String message) {
//         runOnUiThread(() -> {
//             Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//             // Se deu erro, garante que a UI volte ao estado parado
//             onTransmissionStateChanged(false);
//         });
//     }

//     // -----------------------------------------------------

//     @Override
//     protected void onDestroy() {
//         super.onDestroy();
//         if (audioController != null) {
//             audioController.release(); // Manda o Controller limpar a bagunça no hardware e rede
//         }
//     }
// }

package com.example.audiorouter;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_CODE = 1;
    
    private Button btnIniciar;
    private Button btnParar;
    private Button btnBuscarPc;
    private TextView txtStatus;
    private EditText editIpAddress;
    private ServerDiscoverer serverDiscoverer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Vinculação
        btnIniciar = findViewById(R.id.btnIniciar);
        btnParar = findViewById(R.id.btnParar);
        btnBuscarPc = findViewById(R.id.btnBuscarPc);
        txtStatus = findViewById(R.id.txtStatus);
        editIpAddress = findViewById(R.id.editIpAddress);

        // 2. Instanciação (O Discovery continua na Activity por ser curto)
        serverDiscoverer = new ServerDiscoverer();

        // 3. Checagem de Permissões (Microfone + Notificação para Android 13+)
        checkPermissions();

        // 4. Lógica do botão BUSCAR
        btnBuscarPc.setOnClickListener(v -> {
            btnBuscarPc.setEnabled(false);
            btnBuscarPc.setText("Buscando...");
            
            serverDiscoverer.discover(new ServerDiscoverer.DiscoveryListener() {
                @Override
                public void onServerFound(String ip) {
                    runOnUiThread(() -> {
                        editIpAddress.setText(ip);
                        btnBuscarPc.setEnabled(true);
                        btnBuscarPc.setText("BUSCAR");
                        Toast.makeText(MainActivity.this, "PC Encontrado!", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        btnBuscarPc.setEnabled(true);
                        btnBuscarPc.setText("BUSCAR");
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        // 5. Lógica do botão INICIAR (Chama o Serviço)
        btnIniciar.setOnClickListener(v -> {
            String ipAlvo = editIpAddress.getText().toString().trim();
            if (ipAlvo.isEmpty()) {
                Toast.makeText(this, "Digite o IP do PC!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Dispara o serviço de primeiro plano
            Intent intent = new Intent(this, AudioService.class);
            intent.putExtra("IP_ALVO", ipAlvo);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            
            // Atualiza UI imediatamente (ou você pode usar um BroadcastReceiver para ser mais preciso)
            updateUI(true);
        });

        // 6. Lógica do botão PARAR (Mata o Serviço)
        btnParar.setOnClickListener(v -> {
            stopService(new Intent(this, AudioService.class));
            updateUI(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ESTA É A CHAVE: Ao voltar para a tela (ou virar a tela), 
        // checamos se o serviço ainda está vivo para sincronizar os botões.
        updateUI(isAudioServiceRunning());
    }

    private void updateUI(boolean isTransmitting) {
        runOnUiThread(() -> {
            if (isTransmitting) {
                // ESTADO: TRANSMITINDO
                btnIniciar.setEnabled(false);
                btnIniciar.setAlpha(0.2f); // Fica quase invisível
                
                btnParar.setEnabled(true);
                btnParar.setAlpha(1.0f);
                
                editIpAddress.setEnabled(false);
                editIpAddress.setAlpha(0.5f);
                
                // Puxa o verde do colors.xml
                txtStatus.setText("● TRANSMITINDO ÁUDIO");
                txtStatus.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.status_transmitting)); 
            } else {
                // ESTADO: DESCONECTADO
                btnIniciar.setEnabled(true);
                btnIniciar.setAlpha(1.0f);
                
                btnParar.setEnabled(false);
                btnParar.setAlpha(0.2f); 
                
                editIpAddress.setEnabled(true);
                editIpAddress.setAlpha(1.0f);
                
                // Puxa a cor desconectada (tom do tema) do colors.xml
                txtStatus.setText("○ AGUARDANDO CONEXÃO");
                txtStatus.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.status_disconnected)); 
            }
        });
    }

    private boolean isAudioServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AudioService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS};
        } else {
            permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQ_CODE);
        }
    }
}