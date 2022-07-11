package com.github.romanqed.futil.models;

import java.util.List;

public class Text implements Translatable {
    private final List<TextBlock> body;
    private final String delim;

    public Text(List<TextBlock> body, String delim) {
        this.body = body;
        this.delim = delim;
    }

    @Override
    public String getSource() {
        StringBuilder ret = new StringBuilder();
        for (TextBlock block : body) {
            ret.append(block.getSource()).append(delim);
        }
        return ret.toString();
    }

    @Override
    public String getTranslate() {
        StringBuilder ret = new StringBuilder();
        for (TextBlock block : body) {
            ret.append(block.getTranslate()).append(delim);
        }
        return ret.toString();
    }

    public List<TextBlock> getBody() {
        return body;
    }
}
