package br.com.assinanet.Whastapp.Sms;

import br.com.assinanet.util.GsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class WhatsAppApi implements IWhatsApp {


    //-------------------- mutilizado pela API myZap
    private static HttpResponse<String> getStringHttpResponse(String urlServidor,
                                                              HttpClient client,
                                                              SendTextMessageResource sendTextMessageResource,
                                                              WhatsAppConteudo whatsAppConteudo) throws IOException, InterruptedException {
        HttpRequest request;

        request = HttpRequest.newBuilder()
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setHeader("sessionkey", whatsAppConteudo.remetente)
                .setHeader("apitoken", whatsAppConteudo.token)
                .uri(URI.create(urlServidor))
                .POST(HttpRequest.BodyPublishers.ofString(GsonUtil.toJson(sendTextMessageResource)))
                .build();

        return client.send(request,
                HttpResponse.BodyHandlers.ofString());
    }

    //----------------------- utilizado pela API baileys
    private static HttpResponse<String> getStringHttpResponse(String urlServidor,
                                                              HttpClient client,
                                                              SendTextMessageResourceBaileys sendTextMessageResource,
                                                              WhatsAppConteudo whatsAppConteudo) throws IOException, InterruptedException {
        HttpRequest request;

        request = HttpRequest.newBuilder()
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .uri(URI.create(urlServidor))
                .POST(HttpRequest.BodyPublishers.ofString(GsonUtil.toJson(sendTextMessageResource)))
                .build();

        return client.send(request,
                HttpResponse.BodyHandlers.ofString());
    }

    @Getter
    @Setter
    private class SendTextMessageResource {
        String session;
        String number;
        String text;


    }

    @Override
    public boolean enviaWhatsApp(WhatsAppConteudo whatsAppConteudo) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        boolean retorno = true;

        //versao1 da API do whatsapp - Myzap
        if (versaoAPI.equals("V1")) {
            SendTextMessageResource sendTextMessageResource = new SendTextMessageResource();
            sendTextMessageResource.session = whatsAppConteudo.getRemetente();
            sendTextMessageResource.text = whatsAppConteudo.getConteudo().replaceAll("<.*?>", "");
            sendTextMessageResource.number = whatsAppConteudo.getDestinatarios();
            response = getStringHttpResponse(whatsAppConteudo.getApi(), client, sendTextMessageResource, whatsAppConteudo);
            DefaultServiceResult result = GsonUtil.fromJson(response.body(), DefaultServiceResult.class);
            retorno = result.getResult().equals("success");
        }
        //versao1 da API do whatsapp - Baileys
        if (versaoAPI.equals("V2")) {
            SendTextMessageResourceBaileys sendTextMessageResource = new SendTextMessageResourceBaileys();
            sendTextMessageResource.setNumber(whatsAppConteudo.getRemetente());
            sendTextMessageResource.message = whatsAppConteudo.getConteudo().replaceAll("<.*?>", "");
            sendTextMessageResource.getTo().add(whatsAppConteudo.getDestinatarios());
            response = getStringHttpResponse(whatsAppConteudo.getApi(), client, sendTextMessageResource, whatsAppConteudo);
            DefaultServiceResultBaileys result = GsonUtil.fromJson(response.body(), DefaultServiceResultBaileys.class);
            retorno = result.getStatus();
        }


        return retorno;

    }

    @Getter
    @Setter
    private class DefaultServiceResult {
        String result;
    }

    @Getter
    @Setter
    private class SendTextMessageResourceBaileys {
        String number; //sender number
        String message; //text to send
        String type = "chat"; //type delivery
        List<String> to = new ArrayList<>(); //number receiver

        public void setNumber(String number) {
            if ( number.length() <= 11)
                number = "55" + number;
            this.number = number;
            return;
        }

        public String getNumber() {
            return this.number;
        }
    }


    @Getter
    @Setter
    private class DefaultServiceResultBaileys {
        Boolean status;
        String message;
    }

}
