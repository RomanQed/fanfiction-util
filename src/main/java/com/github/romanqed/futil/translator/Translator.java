package com.github.romanqed.futil.translator;

import java.util.Locale;

public interface Translator extends AutoCloseable {
    String[] translate(String[] sources, Locale to, Locale from);

    default String[] translate(String[] sources, Locale to) {
        return translate(sources, to, Locale.ENGLISH);
    }

    default String translate(String source, Locale to, Locale from) {
        return translate(new String[]{source}, to, from)[0];
    }

    default String translate(String source, Locale to) {
        return translate(source, to, Locale.ENGLISH);
    }
}
