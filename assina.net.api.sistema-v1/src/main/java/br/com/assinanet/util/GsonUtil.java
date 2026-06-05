package br.com.assinanet.util;

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

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

}
