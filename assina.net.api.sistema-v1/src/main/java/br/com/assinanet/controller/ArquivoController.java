package br.com.assinanet.controller;

import br.com.assinanet.storage.StorageFactory;
import br.com.assinanet.storage.Storage;
import br.com.assinanet.storage.TipoStorage;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class ArquivoController {
    @PostMapping("/upload")
    public String uploadFileRest(@RequestParam String fileName, @RequestBody byte[] fileBytes) {
        Storage storage = StorageFactory.createStorage(TipoStorage.AZURE);
        return storage.uploadFile(fileName, fileBytes);
    }
    @GetMapping("/download")
    public @ResponseBody byte[] downloadFileRest(@RequestParam String filePath) {
        Storage storage = StorageFactory.createStorage(TipoStorage.AZURE);
        return storage.downloadFile(filePath);
    }
}
