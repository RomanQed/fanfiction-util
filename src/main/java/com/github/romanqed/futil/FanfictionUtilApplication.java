package com.github.romanqed.futil;

import com.github.romanqed.futil.models.Text;
import com.github.romanqed.futil.models.TextBlock;
import com.github.romanqed.futil.parser.JsonTextFactory;
import com.github.romanqed.futil.parser.TextFactory;
import com.github.romanqed.futil.translator.Translator;
import com.github.romanqed.util.Checks;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FanfictionUtilApplication extends Application {
    private static final File CURRENT_WORKING_DIRECTORY = new File(".").getAbsoluteFile();
    private static final Locale RUSSIAN = Locale.forLanguageTag("ru");
    private static final File CONFIG = new File("config.json");
    private static final String TITLE = "Fanfiction Util";
    private static final List<FileChooser.ExtensionFilter> JSON_FILTERS = Collections.singletonList(
            new FileChooser.ExtensionFilter("JSON", "*.json")
    );

    private static final List<FileChooser.ExtensionFilter> HTML_FILTERS = Arrays.asList(
            new FileChooser.ExtensionFilter("HTML", "*.html"),
            new FileChooser.ExtensionFilter("HTM", "*.htm")
    );

    private static final List<FileChooser.ExtensionFilter> TEXT_FILTERS = Collections.singletonList(
            new FileChooser.ExtensionFilter("Text documents", "*.txt")
    );

    private final TextArea source;
    private final TextArea translated;
    private final Button next = new Button(">");
    private final Button prev = new Button("<");
    private final Button save = new Button("save");
    private final Button load = new Button("load");
    private final Button parse = new Button("parse");
    private final Button release = new Button("release");
    private final Button export = new Button("export");
    private final Button translate = new Button("translate");
    private final Button translateAll = new Button("translate all");
    private final Label label = new Label();
    private final Translator translator;
    private TextFactory factory;
    private File file;
    private Stage stage;
    private Text text = null;
    private int index;

    public FanfictionUtilApplication() {
        source = new TextArea();
        source.setWrapText(true);
        translated = new TextArea();
        translated.setWrapText(true);
        next.setOnMouseClicked(this::next);
        prev.setOnMouseClicked(this::previous);
        save.setOnMouseClicked(this::save);
        load.setOnMouseClicked(this::load);
        parse.setOnMouseClicked(this::parse);
        release.setOnMouseClicked(this::release);
        export.setOnMouseClicked(this::export);
        translate.setOnMouseClicked(this::translate);
        translateAll.setOnMouseClicked(this::translateAll);
        translator = Checks.safetyCall(() -> new CommonTranslatorFactory(CONFIG).create(), () -> null);
    }

    @Override
    public void init() {
        factory = new JsonTextFactory(getDelim());
    }

    @Override
    public void stop() throws Exception {
        if (translator != null) {
            translator.close();
        }
    }

    private String getDelim() {
        Map<String, String> parameters = getParameters().getNamed();
        String delim = parameters.getOrDefault("delim", "\n");
        return delim
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\f", "\f");
    }

    private void showError(String message) {
        Alert error = new Alert(Alert.AlertType.ERROR, message);
        error.show();
    }

    private void moveIndex() {
        String message = (index + 1) + "/" + text.getBody().size();
        label.setText(message);
    }

    private boolean textIsNull() {
        if (text == null) {
            showError("TEXT IS NULL");
            return true;
        }
        return false;
    }

    private boolean indexNotInRange(int index) {
        if (index < 0 || index >= text.getBody().size()) {
            showError("BAD INDEX");
            return true;
        }
        return false;
    }

    private void translate(MouseEvent event) {
        String translate;
        try {
            translate = translator.translate(source.getText(), RUSSIAN);
        } catch (Exception e) {
            showError("TRANSLATOR API ERROR");
            return;
        }
        translated.setText(translate);
    }

    private void translateAll(MouseEvent event) {
        if (textIsNull()) {
            return;
        }
        List<TextBlock> body = text.getBody();
        String[] sources = new String[body.size()];
        for (int i = 0; i < sources.length; ++i) {
            sources[i] = body.get(i).getSource();
        }
        String[] translated;
        try {
            translated = translator.translate(sources, RUSSIAN);
        } catch (Exception e) {
            showError("TRANSLATOR API ERROR");
            return;
        }
        for (int i = 0; i < translated.length; ++i) {
            body.get(i).setTranslate(translated[i]);
        }
        this.translated.setText(translated[index]);
    }

    private void next(MouseEvent event) {
        if (textIsNull() || indexNotInRange(index + 1)) {
            return;
        }
        updateBlock();
        TextBlock block = text.getBody().get(++index);
        source.setText(block.getSource());
        translated.setText(block.getTranslate());
        moveIndex();
    }

    private void updateBlock() {
        TextBlock block = text.getBody().get(index);
        block.setTranslate(translated.getText());
    }

    private void previous(MouseEvent event) {
        if (textIsNull() || indexNotInRange(index - 1)) {
            return;
        }
        TextBlock block = text.getBody().get(--index);
        source.setText(block.getSource());
        translated.setText(block.getTranslate());
        moveIndex();
    }

    private File chooseFile(Mode mode, List<FileChooser.ExtensionFilter> filters) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(CURRENT_WORKING_DIRECTORY);
        chooser.getExtensionFilters().addAll(filters);
        if (mode == Mode.OPEN) {
            chooser.setTitle("Open");
            return chooser.showOpenDialog(stage);
        }
        chooser.setTitle("Save");
        return chooser.showSaveDialog(stage);
    }

    private void reset() {
        index = 0;
        if (text.getBody().isEmpty()) {
            return;
        }
        TextBlock block = text.getBody().get(index);
        source.setText(block.getSource());
        translated.setText(block.getTranslate());
    }

    private void save(MouseEvent event) {
        if (textIsNull()) {
            return;
        }
        updateBlock();
        try {
            File file;
            if (this.file != null) {
                file = this.file;
            } else {
                file = chooseFile(Mode.SAVE, JSON_FILTERS);
                if (file == null) {
                    return;
                }
                this.file = file;
            }
            factory.write(text, file);
        } catch (IOException e) {
            throw new IllegalStateException("Can't write file due to", e);
        }
    }

    private void load(MouseEvent event) {
        try {
            File file = chooseFile(Mode.OPEN, JSON_FILTERS);
            if (file == null) {
                return;
            }
            text = factory.read(file);
            this.file = file;
            reset();
        } catch (IOException e) {
            throw new IllegalStateException("Can't read file due to", e);
        }
    }

    private void parse(MouseEvent event) {
        try {
            File file = chooseFile(Mode.OPEN, HTML_FILTERS);
            if (file == null) {
                return;
            }
            text = factory.parsePage(file);
            reset();
        } catch (IOException e) {
            throw new IllegalStateException("Can't read file due to", e);
        }
    }

    private BufferedWriter createWriter(File file) throws FileNotFoundException {
        FileOutputStream stream = new FileOutputStream(file);
        return new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
    }

    private void release(MouseEvent event) {
        if (textIsNull()) {
            return;
        }
        updateBlock();
        try {
            File file = chooseFile(Mode.SAVE, TEXT_FILTERS);
            if (file == null) {
                return;
            }
            BufferedWriter writer = createWriter(file);
            writer.write(text.getTranslate());
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Can't release text due to", e);
        }
    }

    private void export(MouseEvent event) {
        if (textIsNull()) {
            return;
        }
        try {
            File file = chooseFile(Mode.SAVE, TEXT_FILTERS);
            if (file == null) {
                return;
            }
            BufferedWriter writer = createWriter(file);
            writer.write(text.getSource());
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Can't export due to", e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(TITLE);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        addGridElements(grid);

        Scene primaryScene = new Scene(grid);
        primaryStage.setScene(primaryScene);

        primaryStage.show();
        stage = primaryStage;
    }

    private void addGridElements(GridPane grid) {
        grid.add(source, 0, 0);
        grid.add(translated, 1, 0);
        grid.add(prev, 0, 1);
        grid.add(next, 1, 1);
        grid.add(save, 0, 2);
        grid.add(load, 1, 2);
        grid.add(parse, 0, 3);
        grid.add(release, 1, 3);
        grid.add(export, 0, 4);
        grid.add(label, 1, 4);
        if (translator != null) {
            grid.add(translate, 0, 5);
            grid.add(translateAll, 1, 5);
        }
    }

    private enum Mode {
        SAVE,
        OPEN
    }
}
