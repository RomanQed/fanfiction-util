package com.github.romanqed.futil;

import com.github.romanqed.futil.translator.MicrosoftTranslator;
import com.github.romanqed.futil.translator.Translator;
import com.github.romanqed.futil.translator.TranslatorFactory;
import com.github.romanqed.jeflect.ReflectUtil;
import com.github.romanqed.jeflect.lambdas.Lambda;
import com.github.romanqed.jeflect.lambdas.LambdaFactory;
import com.github.romanqed.util.Checks;
import com.google.gson.Gson;
import io.github.amayaframework.http.ContentType;
import io.github.amayaframework.http.HttpUtil;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.sql.Ref;
import java.util.Locale;

public class CommonTranslatorFactory implements TranslatorFactory {
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
        Method translate = clazz.getDeclaredMethod("translate", String[].class, Locale.class, Locale.class);
        if (translate.getReturnType() != String[].class) {
            throw new IllegalStateException("Invalid translate method");
        }
        Method close = clazz.getDeclaredMethod("close");
        Translator ret;
        LambdaFactory factory = new LambdaFactory(ReflectUtil.wrapClassLoader(loader));
        try {
            Object instance = clazz.getConstructor().newInstance();
            Lambda translateLambda = factory.packMethod(translate, instance);
            Lambda closeLambda = factory.packMethod(close, instance);
            ret = new TranslatorWrapper(translateLambda, closeLambda);
        } catch (Throwable e) {
            throw new IllegalStateException("Can't pack translate method due to", e);
        }
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

    private static class TranslatorWrapper implements Translator {
        private final Lambda translate;
        private final Lambda close;

        private TranslatorWrapper(Lambda translate, Lambda close) {
            this.translate = translate;
            this.close = close;
        }

        @Override
        public String[] translate(String[] sources, Locale to, Locale from) {
            try {
                return (String[]) translate.call(new Object[]{sources, to, from});
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
            try {
                close.call();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
