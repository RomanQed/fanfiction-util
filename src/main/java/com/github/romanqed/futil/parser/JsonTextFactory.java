package com.github.romanqed.futil.parser;

import com.github.romanqed.futil.models.Text;
import com.github.romanqed.futil.models.TextBlock;
import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonTextFactory implements TextFactory {
    private static final Gson GSON = new Gson();
    private static final String SELECTOR = "div.storytext p";
    private final String delim;

    public JsonTextFactory(String delim) {
        this.delim = Objects.requireNonNull(delim);
    }

    public Text parsePage(File page) throws IOException {
        Document document = Jsoup.parse(page);
        Elements found = document.select(SELECTOR);
        List<TextBlock> blocks = new ArrayList<>();
        for (Element element : found) {
            blocks.add(new TextBlock(element.text()));
        }
        return new Text(blocks, delim);
    }

    public Text read(File json) throws IOException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(json), StandardCharsets.UTF_8);
        Text ret = GSON.fromJson(reader, Text.class);
        reader.close();
        return ret;
    }

    public void write(Text text, File json) throws IOException {
        String data = GSON.toJson(text);
        OutputStreamWriter streamWriter = new OutputStreamWriter(new FileOutputStream(json), StandardCharsets.UTF_8);
        BufferedWriter writer = new BufferedWriter(streamWriter);
        writer.write(data);
        writer.close();
    }
}
