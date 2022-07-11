package com.github.romanqed.futil;

import com.github.romanqed.futil.models.Text;
import com.github.romanqed.futil.models.TextBlock;
import com.github.romanqed.futil.parser.JsonTextFactory;
import com.github.romanqed.futil.parser.TextFactory;
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

public class FanfictionUtilApplication extends Application {
    private static final String TITLE = "Fanfiction Util";
    private final TextArea source;
    private final TextArea translate;
    private final Button next = new Button(">");
    private final Button prev = new Button("<");
    private final Button save = new Button("save");
    private final Button load = new Button("load");
    private final Button parse = new Button("parse");
    private final Button release = new Button("release");
    private final TextFactory factory = new JsonTextFactory("\n");
    private final Label label = new Label();
    private Stage stage;
    private Text text = null;
    private int index;

    public FanfictionUtilApplication() {
        source = new TextArea();
        source.setWrapText(true);
        translate = new TextArea();
        translate.setWrapText(true);
        next.setOnMouseClicked(this::next);
        prev.setOnMouseClicked(this::previous);
        save.setOnMouseClicked(this::save);
        load.setOnMouseClicked(this::load);
        parse.setOnMouseClicked(this::parse);
        release.setOnMouseClicked(this::release);
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

    private void next(MouseEvent event) {
        if (textIsNull() || indexNotInRange(index + 1)) {
            return;
        }
        updateBlock();
        TextBlock block = text.getBody().get(++index);
        source.setText(block.getSource());
        translate.setText(block.getTranslate());
        moveIndex();
    }

    private void updateBlock() {
        TextBlock block = text.getBody().get(index);
        block.setTranslate(translate.getText());
    }

    private void previous(MouseEvent event) {
        if (textIsNull() || indexNotInRange(index - 1)) {
            return;
        }
        TextBlock block = text.getBody().get(--index);
        source.setText(block.getSource());
        translate.setText(block.getTranslate());
        moveIndex();
    }

    private File chooseFile(Mode mode) {
        FileChooser chooser = new FileChooser();
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
        translate.setText(block.getTranslate());
    }

    private void save(MouseEvent event) {
        if (textIsNull()) {
            return;
        }
        updateBlock();
        try {
            File file = chooseFile(Mode.SAVE);
            if (file == null) {
                return;
            }
            factory.write(text, file);
        } catch (IOException e) {
            throw new IllegalStateException("Can't write file due to", e);
        }
    }

    private void load(MouseEvent event) {
        try {
            File file = chooseFile(Mode.OPEN);
            if (file == null) {
                return;
            }
            text = factory.read(file);
            reset();
        } catch (IOException e) {
            throw new IllegalStateException("Can't read file due to", e);
        }
    }

    private void parse(MouseEvent event) {
        try {
            File file = chooseFile(Mode.OPEN);
            if (file == null) {
                return;
            }
            text = factory.parsePage(file);
            reset();
        } catch (IOException e) {
            throw new IllegalStateException("Can't read file due to", e);
        }
    }

    private void release(MouseEvent event) {
        if (textIsNull()) {
            return;
        }
        updateBlock();
        try {
            File file = chooseFile(Mode.SAVE);
            if (file == null) {
                return;
            }
            FileOutputStream stream = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
            writer.write(text.getTranslate());
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Can't release text due to", e);
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
        grid.add(translate, 1, 0);
        grid.add(prev, 0, 1);
        grid.add(next, 1, 1);
        grid.add(save, 0, 2);
        grid.add(load, 1, 2);
        grid.add(parse, 0, 3);
        grid.add(release, 1, 3);
        grid.add(label, 1, 4);
    }

    private enum Mode {
        SAVE,
        OPEN
    }
}
