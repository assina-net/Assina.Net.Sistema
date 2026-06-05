package br.com.assinanet.storage;

public class StorageFactory {

    public static Storage createStorage(TipoStorage tipo) {
        if (tipo == TipoStorage.LOCAL) {
            return new StorageLocal();
        }
        if (tipo == TipoStorage.AZURE) {
            return new StorageAzure();
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

