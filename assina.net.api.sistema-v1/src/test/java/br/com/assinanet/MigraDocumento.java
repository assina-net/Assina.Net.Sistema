package br.com.assinanet;

import br.com.assinanet.Sms.ISms;
import br.com.assinanet.Sms.SmsComtele;
import br.com.assinanet.Sms.SmsConteudo;
import br.com.assinanet.service.ContratoService;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.transaction.Transactional;
import java.io.IOException;

public class MigraDocumento {

    //@Autowired
    private static ContratoService contratoService;

    public MigraDocumento(ContratoService contratoService) {
        contratoService = contratoService;
    }
    public static void main(String[] args) {

        //ContratoService cs = new ContratoService();
        //cs.MigrarDocumentoParaStorage();


    }

}

class Teste {
    private ContratoService contratoService;

    public Teste(ContratoService contratoService) {
        this.contratoService = contratoService;
    }
}
