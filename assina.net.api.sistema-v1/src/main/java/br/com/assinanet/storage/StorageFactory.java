package br.com.assinanet.storage;

public class StorageFactory {

    public static Storage createStorage(TipoStorage tipo) {
        return createStorage(tipo, null, null);
    }

    public static Storage createStorage(TipoStorage tipo, String connectionString, String containerName) {
        if (tipo == TipoStorage.LOCAL) {
            return new StorageLocal();
        }
        if (tipo == TipoStorage.AZURE) {
            return new StorageAzure(connectionString, containerName);
        }
        if (tipo == TipoStorage.AWS) {
            try {
                throw new Exception("Storage AWS náo implementado!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}

