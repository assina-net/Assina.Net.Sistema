package br.com.assinanet.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;


public class ArquivoDownloadModel {

    @Getter
    @Setter
    String arquivoNome;

    @Getter
    @Setter
    byte[] arquivoByte;

    @Getter
    @Setter
    MediaType mediaType;

}
