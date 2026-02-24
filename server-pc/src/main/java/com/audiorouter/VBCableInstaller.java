package com.audiorouter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VBCableInstaller {

    // Link oficial da versão Lite/Free do VB-Cable
    private static final String DOWNLOAD_URL = "https://download.vb-audio.com/Download_CABLE/VBCABLE_Driver_Pack43.zip";
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "VBCableSetup";

    public static boolean downloadAndInstall() {
        try {
            System.out.println("[Instalador] Iniciando download do VB-Cable...");
            File zipFile = new File(TEMP_DIR + ".zip");
            
            // 1. Faz o Download fingindo ser um navegador (User-Agent)
            URL url = new URL(DOWNLOAD_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("[Instalador] Download concluído. Descompactando...");
            File extractDir = new File(TEMP_DIR);
            unzip(zipFile, extractDir);

            // 2. Executa a instalação via terminal
            System.out.println("[Instalador] Solicitando permissão do Windows para instalar o Driver...");
            
            // O VB-Cable tem executáveis diferentes para 32 e 64 bits. Assumimos 64 bits.
            String installerPath = extractDir.getAbsolutePath() + "\\VBCABLE_Setup_x64.exe";
            
            System.out.println("[Instalador] Solicitando privilégios de Administrador (Aprove na tela do Windows)...");
            
            // Usando o caminho absoluto e infalível do PowerShell no Windows
            String powerShellPath = "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe";
            
            ProcessBuilder pb = new ProcessBuilder(
                powerShellPath,
                "-Command",
                "Start-Process",
                "-FilePath", "'" + installerPath + "'",
                "-Verb", "RunAs",
                "-Wait"
            );
            
            
            Process process = pb.start();
            process.waitFor(); // Espera o usuário fechar/terminar a instalação

            System.out.println("[Instalador] Processo de instalação finalizado.");
            return true;

        } catch (Exception e) {
            System.err.println("[Instalador] Falha catastrófica na instalação automática: " + e.getMessage());
            return false;
        }
    }

    // Método utilitário para extrair o ZIP
    private static void unzip(File zipFile, File destDir) throws IOException {
        if (!destDir.exists()) destDir.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }
}