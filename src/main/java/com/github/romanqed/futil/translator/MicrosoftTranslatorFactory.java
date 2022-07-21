package com.github.romanqed.futil.translator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.amayaframework.http.ContentType;
import io.github.amayaframework.http.HttpUtil;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MicrosoftTranslatorFactory implements TranslatorFactory {
    private static final String URL = "https://microsoft-translator-text.p.rapidapi.com/translate";
    private static final String TOKEN_HEADER = "X-RapidAPI-Key";
    private final String token;

    public MicrosoftTranslatorFactory(File file) throws IOException {
        token = readTokenFromConfig(file);
    }

    private String readTokenFromConfig(File file) throws IOException {
        FileInputStream fileStream = new FileInputStream(file.getAbsolutePath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream, StandardCharsets.UTF_8));
        String config = reader.lines().reduce("", (left, right) -> left + right + "\n");
        reader.close();
        JsonObject json = JsonParser.parseString(config).getAsJsonObject();
        return json.get("translate-token").getAsString();
    }

    @Override
    public Translator create() {
        UnirestInstance client = Unirest.spawnInstance();
        client
                .config()
                .setDefaultHeader(HttpUtil.CONTENT_HEADER, ContentType.JSON.getHeader())
                .setDefaultHeader(TOKEN_HEADER, token)
                .setDefaultHeader("Accept-Encoding", "utf-8");
        return new MicrosoftTranslator(client, URL);
    }
}
