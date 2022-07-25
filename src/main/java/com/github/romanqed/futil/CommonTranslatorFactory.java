package com.github.romanqed.futil;

import com.github.romanqed.futil.translator.MicrosoftTranslator;
import com.github.romanqed.futil.translator.Translator;
import com.github.romanqed.futil.translator.TranslatorFactory;
import com.github.romanqed.jeflect.DefineClassLoader;
import com.github.romanqed.jeflect.binding.BindingFactory;
import com.github.romanqed.jeflect.binding.InterfaceType;
import com.github.romanqed.util.Checks;
import com.google.gson.Gson;
import io.github.amayaframework.http.ContentType;
import io.github.amayaframework.http.HttpUtil;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;

import java.io.*;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

public class CommonTranslatorFactory implements TranslatorFactory {
    private static final InterfaceType<Translator> TRANSLATOR_TYPE = InterfaceType.fromClass(Translator.class);
    private static final String URL = "https://microsoft-translator-text.p.rapidapi.com/translate";
    private static final String TOKEN_HEADER = "X-RapidAPI-Key";
    private static final Gson GSON = new Gson();
    private final Config config;

    public CommonTranslatorFactory(File file) throws IOException {
        config = readConfig(file);
    }

    private Config readConfig(File file) throws IOException {
        FileInputStream fileStream = new FileInputStream(file.getAbsolutePath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream, StandardCharsets.UTF_8));
        String config = reader.lines().reduce("", (left, right) -> left + right + "\n");
        reader.close();
        return GSON.fromJson(config, Config.class);
    }

    private Translator createDefault() {
        UnirestInstance client = Unirest.spawnInstance();
        client
                .config()
                .setDefaultHeader(HttpUtil.CONTENT_HEADER, ContentType.JSON.getHeader())
                .setDefaultHeader(TOKEN_HEADER, config.getToken())
                .setDefaultHeader("Accept-Encoding", "utf-8");
        return new MicrosoftTranslator(client, URL);
    }

    private Translator createCustom() throws Exception {
        File jar = new File(config.getJar());
        URLClassLoader loader = JarUtil.loadJar(jar, ClassLoader.getSystemClassLoader());
        Class<?> clazz = loader.loadClass("com.futil.CustomTranslator");
        Object instance = clazz.getDeclaredConstructor().newInstance();
        BindingFactory factory = new BindingFactory(new DefineClassLoader(loader));
        Translator ret = factory.bind(TRANSLATOR_TYPE, instance);
        loader.close();
        return ret;
    }

    @Override
    public Translator create() {
        if (config.isUseCustomTranslator()) {
            return Checks.safetyCall(this::createCustom);
        }
        return createDefault();
    }
}
