package br.com.assinanet.Whastapp.Sms;

import java.io.IOException;

public interface IWhatsApp {

    String versaoAPI = "V2";

    boolean enviaWhatsApp(WhatsAppConteudo whatsAppConteudo) throws IOException, InterruptedException;
}
