package br.com.assinanet;

import br.com.assinanet.Sms.ISms;
import br.com.assinanet.Sms.SmsComtele;
import br.com.assinanet.Sms.SmsConteudo;

import java.io.IOException;

public class smsTest {

    public static void main(String[] args) {
        ISms sms = new SmsComtele();

        SmsConteudo smsConteudo = new SmsConteudo();
        smsConteudo.setApi("d6d06f36-0e2f-4911-8c8d-56a85087c86b");
        smsConteudo.setRemetente("Assina.Net");
        smsConteudo.setConteudo("Bonatte teste Assina.Net");
        smsConteudo.setDestinatarios("19997981209, 19981297880");

        try {
            sms.enviaSMS(smsConteudo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
