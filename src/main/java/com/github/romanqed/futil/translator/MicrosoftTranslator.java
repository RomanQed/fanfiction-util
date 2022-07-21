package com.github.romanqed.futil.translator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kong.unirest.HttpResponse;
import kong.unirest.RequestBodyEntity;
import kong.unirest.UnirestInstance;

import java.util.Locale;

public class MicrosoftTranslator implements Translator {
    private static final String API_QUERY = "api-version";
    private static final String API_VERSION = "3.0";
    private final UnirestInstance client;
    private final String url;

    MicrosoftTranslator(UnirestInstance client, String url) {
        this.client = client;
        this.url = url;
    }

    private RequestBodyEntity makeRequest(String[] sources, Locale to, Locale from) {
        JsonArray array = new JsonArray();
        for (String source : sources) {
            JsonObject toAdd = new JsonObject();
            toAdd.addProperty("Text", source);
            array.add(toAdd);
        }
        return client
                .post(url)
                .queryString(API_QUERY, API_VERSION)
                .queryString("to", to.getLanguage())
                .queryString("from", from.getLanguage())
                .queryString("profanityAction", "NoAction")
                .queryString("textType", "plain")
                .body(array.toString());
    }

    @Override
    public String[] translate(String[] sources, Locale to, Locale from) {
        RequestBodyEntity request = makeRequest(sources, to, from);
        HttpResponse<String> response;
        try {
            response = request.asStringAsync().get();
        } catch (Exception e) {
            throw new IllegalStateException("API request failed due to", e);
        }
        if (!response.isSuccess()) {
            throw new IllegalStateException("API request failed");
        }
        JsonArray array = JsonParser.parseString(response.getBody()).getAsJsonArray();
        String[] ret = new String[sources.length];
        int index = 0;
        for (JsonElement element : array) {
            JsonObject object = element
                    .getAsJsonObject()
                    .getAsJsonArray("translations")
                    .get(0)
                    .getAsJsonObject();
            ret[index++] = object.get("text").getAsString();
        }
        return ret;
    }

    @Override
    public void close() {
        client.close();
    }
}
