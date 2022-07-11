package com.github.romanqed.futil.parser;

import com.github.romanqed.futil.models.Text;

import java.io.File;
import java.io.IOException;

public interface TextFactory {
    Text parsePage(File page) throws IOException;

    Text read(File json) throws IOException;

    void write(Text text, File json) throws IOException;
}
