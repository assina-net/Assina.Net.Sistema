package br.com.assinanet.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;

public class StorageAzure implements Storage {

    private final String connectionString;
    private final String containerName;

    public StorageAzure() {
        this(null, null);
    }

    public StorageAzure(String connectionString, String containerName) {
        this.connectionString = getRequiredConfig(
                "AZURE_STORAGE_CONNECTION_STRING",
                connectionString
        );
        this.containerName = getRequiredConfig(
                "AZURE_STORAGE_CONTAINER_NAME",
                containerName
        );
    }

    public String uploadFile(String storageFilePath, byte[] fileBytes) {
        String blobName = normalizeBlobName(storageFilePath);

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        System.out.println("[AZURE_STORAGE_UPLOAD] Container: " + containerName
                + " | URL: " + containerClient.getBlobContainerUrl()
                + " | Blob: " + blobName);

        BlockBlobClient blobClient = containerClient.getBlobClient(blobName).getBlockBlobClient();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            blobClient.upload(inputStream, fileBytes.length, true);
            return "Arquivo " + blobName + " enviado com sucesso!";
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao enviar o arquivo " + blobName, e);
        }
    }

    public byte[] downloadFile(String storageFilePath) {
        String blobName = normalizeBlobName(storageFilePath);

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        System.out.println("[AZURE_STORAGE_DOWNLOAD] Container: " + containerName
                + " | URL: " + containerClient.getBlobContainerUrl()
                + " | Blob: " + blobName);

        try {
            return downloadBlob(containerClient, blobName);
        } catch (BlobStorageException exception) {
            if (exception.getStatusCode() != 404) {
                throw exception;
            }

            Set<String> alternativeBlobNames = getAlternativeBlobNames(storageFilePath, blobName);
            for (String alternativeBlobName : alternativeBlobNames) {
                BlockBlobClient alternativeBlobClient = containerClient
                        .getBlobClient(alternativeBlobName)
                        .getBlockBlobClient();
                if (alternativeBlobClient.exists()) {
                    System.out.println("[AZURE_STORAGE_DOWNLOAD] Blob encontrado em caminho alternativo: "
                            + alternativeBlobName);
                    return alternativeBlobClient.downloadContent().toBytes();
                }
            }

            System.out.println("[AZURE_STORAGE_DOWNLOAD_NOT_FOUND] Candidatos testados: "
                    + String.join(" | ", alternativeBlobNames));
            throw exception;
        }
    }

    private static byte[] downloadBlob(BlobContainerClient containerClient, String blobName) {
        BinaryData binaryData = containerClient
                .getBlobClient(blobName)
                .getBlockBlobClient()
                .downloadContent();
        return binaryData.toBytes();
    }

    private static Set<String> getAlternativeBlobNames(String storageFilePath, String normalizedBlobName) {
        Set<String> blobNames = new LinkedHashSet<>();
        blobNames.add(normalizedBlobName);
        blobNames.add("/" + removeLeadingSeparators(normalizedBlobName));
        blobNames.add(storageFilePath);

        String withoutLeadingSeparator = removeLeadingSeparators(normalizedBlobName);
        blobNames.add(withoutLeadingSeparator);
        if (withoutLeadingSeparator.startsWith("assina.net/")) {
            blobNames.add(withoutLeadingSeparator.substring("assina.net/".length()));
        }

        blobNames.remove(normalizedBlobName);
        return blobNames;
    }

    private static String removeLeadingSeparators(String path) {
        return path.replaceFirst("^[\\\\/]+", "");
    }

    private static String normalizeBlobName(String storageFilePath) {
        return storageFilePath.replace('\\', '/');
    }

    private static String getRequiredConfig(String name, String configuredValue) {
        String value = getConfig(name, configuredValue, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Configuracao obrigatoria nao informada: " + name
                    + " (sistema_atributo ou variavel de ambiente)");
        }
        return value;
    }

    private static String getConfig(String name, String configuredValue, String defaultValue) {
        String value = configuredValue;
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(name);
        }
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(name);
        }
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
