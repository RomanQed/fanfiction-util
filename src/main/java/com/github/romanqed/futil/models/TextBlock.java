package com.github.romanqed.futil.models;

public final class TextBlock implements Translatable {
    private final String source;
    private String translate;

    public TextBlock(String source) {
        this.source = source;
        this.translate = "";
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }
}
