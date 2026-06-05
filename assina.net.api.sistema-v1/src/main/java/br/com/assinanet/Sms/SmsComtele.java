package br.com.assinanet.Sms;

import br.com.assinanet.util.CommonsUtil;
import br.com.assinanet.util.GsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;

public class SmsComtele implements ISms{


    @Override
    public boolean enviaSMS(SmsConteudo smsConteudo) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;

        SendTextMessageResource sendTextMessageResource = new SendTextMessageResource();
        sendTextMessageResource.Sender = smsConteudo.getRemetente();
        sendTextMessageResource.Content = smsConteudo.getConteudo();
        sendTextMessageResource.Receivers = String.join(",", smsConteudo.getDestinatarios());

        String apiSMS =  "https://sms.comtele.com.br/api/v2/send";

        response = getStringHttpResponse(apiSMS, smsConteudo.getApi(), client, sendTextMessageResource);

        DefaultServiceResult result = GsonUtil.fromJson( response.body(), DefaultServiceResult.class);

        return result.getSuccess() ;

    }


    private static HttpResponse<String> getStringHttpResponse(String urlServidor, String apiKey,
                                                              HttpClient client, SendTextMessageResource sendTextMessageResource) throws IOException, InterruptedException {
        HttpRequest request;

        request = HttpRequest.newBuilder()
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf8")
                .setHeader("auth-key", apiKey)
                .uri(URI.create(urlServidor))
                .POST(HttpRequest.BodyPublishers.ofString( GsonUtil.toJson(sendTextMessageResource)) )
                .build();

        return client.send(request,
                HttpResponse.BodyHandlers.ofString());
    }

    @Getter
    @Setter
    private class SendTextMessageResource {
         String Sender;
         String Content;
         String Receivers;
    }

    @Getter
    @Setter
    private class DefaultServiceResult {
         Object Object;
         String Message;
         Boolean Success;
    }
}
