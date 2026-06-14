package br.com.assinanet.util;

import br.com.assinanet.entity.Contrato;
import br.com.assinanet.entity.ContratoDocumento;
import br.com.assinanet.entity.ContratoLog;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;

public class GsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeHierarchyAdapter(byte[].class,
                    new ByteArrayToBase64TypeAdapter())
            .create();

    private static class ByteArrayToBase64TypeAdapter implements JsonDeserializer<byte[]> { // JsonSerializer<byte[]>
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return org.apache.commons.codec.binary.Base64.decodeBase64(json.getAsString());
        }
    }

        public static <T> Type getColletionType(Object object) {
        Type listType =
                new TypeToken<Collection<T>>() {
                }.getType();

        return listType;
    }


    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static String toJsonContratoLog(ContratoLog contratoLog) {
        if (contratoLog == null) {
            return gson.toJson(null);
        }

        ContratoLog logSerializavel = new ContratoLog();
        logSerializavel.setId(contratoLog.getId());
        logSerializavel.setLog(contratoLog.getLog());
        logSerializavel.setDataLog(contratoLog.getDataLog());
        logSerializavel.setGmtLog(contratoLog.getGmtLog());
        logSerializavel.setCarimboTempoLog(contratoLog.getCarimboTempoLog());
        logSerializavel.setLogSistema(contratoLog.getLogSistema());

        if (contratoLog.getContrato() != null) {
            Contrato contrato = new Contrato();
            contrato.setId(contratoLog.getContrato().getId());
            logSerializavel.setContrato(contrato);
        }

        if (contratoLog.getContratoDocumento() != null) {
            ContratoDocumento documento = new ContratoDocumento();
            documento.setId(contratoLog.getContratoDocumento().getId());
            logSerializavel.setContratoDocumento(documento);
        }

        return gson.toJson(logSerializavel);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

}
