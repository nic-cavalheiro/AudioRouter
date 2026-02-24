package com.example.audiorouter;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AudioService extends Service {
    private AudioController audioController; // O SEU CONTROLADOR AQUI
    private static final String CHANNEL_ID = "AudioRouterChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        // Instancia o controlador uma única vez quando o serviço nasce
        // Passamos null porque a Activity já gerencia o próprio estado visual
        audioController = new AudioController(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String ip = intent.getStringExtra("IP_ALVO");
        
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AudioRouter")
                .setContentText("Transmitindo para: " + ip)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();

        // Android 14 exige o tipo de serviço de microfone aqui
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground(1, notification);
        }

        // USAMOS O SEU MÉTODO: toggleTransmission fará o start() com o IP recebido
        if (ip != null) {
            audioController.toggleTransmission(ip);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // USAMOS O SEU MÉTODO: release() fecha o recorder e o sender de forma limpa
        if (audioController != null) {
            audioController.release();
        }
        super.onDestroy();
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "Fluxo de Áudio", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }
}