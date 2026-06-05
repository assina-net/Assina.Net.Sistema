package br.com.assinanet.service;

import br.com.assinanet.entity.enums.SistemaTipoAtributoEnum;
import br.com.assinanet.util.CommonsUtil;
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
import java.util.List;

@Service
public final class GeoLocalizationService {

    private final SistemaAtributoService sistemaAtributoService;

    // http://api.ipstack.com/177.194.5.47?access_key=831a25229740b2bdc1c79b652b9d1c4c

    public GeoLocalizationService(SistemaAtributoService sistemaAtributoService) {
        this.sistemaAtributoService = sistemaAtributoService;
    }

    public  GeoLocalzation GeoLocalization(String ip) {

        if (CommonsUtil.mesmoValor(ip, "0:0:0:0:0:0:0:1")) {
            ip = "177.194.5.47";
        }

        String chaveAcesso = sistemaAtributoService.getString(SistemaTipoAtributoEnum.CHAVE_ACESSO_GEOLOCALIZACAO_IPSTACK, null);

        HttpClient client = HttpClient.newHttpClient();


        HttpResponse<String> response = null;
        try {
            response = getStringHttpResponse("http://api.ipstack.com/", ip, chaveAcesso, client);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            GeoLocalzation geoLocalzation =  GsonUtil.fromJson(response.body(), GeoLocalzation.class);

            return geoLocalzation;
        } catch (Exception e) {
            return null;
        }

    }

    private static HttpResponse<String> getStringHttpResponse(String urlServidor, String ip,String chaveAcesso,  HttpClient client) throws IOException, InterruptedException {
        HttpRequest request;

        request = HttpRequest.newBuilder()
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf8")
                .uri(URI.create(urlServidor + ip+ "?access_key=" + chaveAcesso + "&language=pt-br"))
                .build();

        return client.send(request,
                HttpResponse.BodyHandlers.ofString());
    }

    @Getter
    @Setter
    public class GeoLocalzation {

        String ip;

        String type;

        String continent_code;

        String continent_name;

        String country_code;

        String country_name;

        String region_code;

        String region_name;

        String city;

        String zip;

        String latitude;

        String longitude;

        Location location;
    }

    @Getter
    @Setter
    public class Location {
        String geoname_id;

        String capital;

        List<Languages> languages;

        String country_flag;

        String country_flag_emoji;

        String country_flag_emoji_unicode;

        String calling_code;

        Boolean is_eu;
    }

    @Getter
    @Setter
    public class Languages {

        String code;

        String name;
    }

}
