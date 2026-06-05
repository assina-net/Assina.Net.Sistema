package br.com.assinanet.service;

import br.com.assinanet.util.GsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;

@Service
public final class CepService {

    private final SistemaAtributoService sistemaAtributoService;

    // http://api.ipstack.com/177.194.5.47?access_key=831a25229740b2bdc1c79b652b9d1c4c

    public CepService(SistemaAtributoService sistemaAtributoService) {
        this.sistemaAtributoService = sistemaAtributoService;
    }

    public ViaCep buscaEndereco(String cep) {

        cep = cep.replace("-", "");

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;


        String apiCep =  MessageFormat.format("https://viacep.com.br/ws/{0}/json",  cep);

        try {
            response = getStringHttpResponse(apiCep, client);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            ViaCep viaCep = GsonUtil.fromJson(response.body(), ViaCep.class);

            return viaCep;
        } catch (Exception e) {
            return null;
        }

    }

    private static HttpResponse<String> getStringHttpResponse(String urlServidor, HttpClient client) throws IOException, InterruptedException {
        HttpRequest request;

        request = HttpRequest.newBuilder()
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf8")
                .uri(URI.create(urlServidor))
                .build();

        return client.send(request,
                HttpResponse.BodyHandlers.ofString());
    }

    @Getter
    @Setter
    public class ViaCep {

        String cep;

        String logradouro;

        String complemento;

        String localidade;

        String bairro;

        String uf;

        String unidade;

        String ibge;

        String gia;
    }

}
