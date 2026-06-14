package br.com.assinanet.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StorageLocal implements Storage {

    String localStorage = "F:/Storage/";

    public StorageLocal() {

    }

    public String uploadFile(String storageFilePath, byte[] fileBytes) {

        //String fullPathFile = localStorage+storageFilePath;

        try {

            File file = new File(storageFilePath);
            String name = file.getName();
            String dir = file.getPath().replace(name, "");

            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            if (!file.exists())
                file.createNewFile();

            OutputStream out = new FileOutputStream(file);

            out.write(fileBytes);
            out.close();


        } catch (IOException e) {
            throw new IllegalStateException("Erro ao gravar o arquivo local " + storageFilePath, e);
        }
        return "";
    }

    public byte[] downloadFile(String storageFilePath) {
        //String fullPathFile = localStorage+storageFilePath;
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get(storageFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileBytes;
    }
}
