package br.com.assinanet.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
public class StorageAzure implements Storage {

    private final String connectionString = getRequiredConfig("AZURE_STORAGE_CONNECTION_STRING");
    private final String containerName = getConfig("AZURE_STORAGE_CONTAINER_NAME", "assinanet-container1");

    public StorageAzure() {

    }

    public String uploadFile(String storageFilePath, byte[] fileBytes) {

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        BlockBlobClient blobClient = containerClient.getBlobClient(storageFilePath).getBlockBlobClient();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            blobClient.upload(inputStream, fileBytes.length, true);
            return "Arquivo " + storageFilePath + " enviado com sucesso!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro ao enviar o arquivo " + storageFilePath + ": " + e.getMessage();
        }
    }

    public byte[] downloadFile(String storageFilePath) {
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        BlockBlobClient blobClient = containerClient.getBlobClient(storageFilePath).getBlockBlobClient();


        BinaryData binaryData = blobClient.downloadContent();
        return binaryData.toBytes();
    }

    private static String getRequiredConfig(String name) {
        String value = getConfig(name, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Configuracao obrigatoria nao informada: " + name);
        }
        return value;
    }

    private static String getConfig(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(name);
        }
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
