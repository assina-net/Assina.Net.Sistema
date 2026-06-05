package br.com.assinanet.storage;

import java.io.IOException;

public interface Storage {
    public String uploadFile(String storageFilePath, byte[] fileBytes);
    public byte[] downloadFile(String storageFilePath);

}
