package com.mycompany.levelup_code.presentation.component;

import javafx.animation.PauseTransition;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import java.util.List;

/** Shared source editor preserving plain text, caret and draft scroll coordinates. */
public final class CodeEditor extends StackPane {
    private final CodeArea area = new CodeArea();
    private final PauseTransition highlighting = new PauseTransition(Duration.millis(120));
    private String language = "JAVA";
    private int matchedStart = -1;
    private int matchedEnd = -1;
    private String highlightedText = "";

    public CodeEditor() {
        getStyleClass().add("syntax-editor");
        setMinSize(0, 80);
        setPrefHeight(260);
        area.getStyleClass().add("source-code");
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.setLineHighlighterFill(Color.web("#EFF6FF"));
        area.setLineHighlighterOn(true);
        area.focusedProperty().addListener((observable, old, focused) ->
                pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("editor-focused"), focused));
        area.caretPositionProperty().addListener((observable, old, value) -> markMatchingBrackets());
        area.accessibleTextProperty().bind(accessibleTextProperty());
        area.accessibleHelpProperty().bind(accessibleHelpProperty());
        styleProperty().addListener((observable, old, value) -> area.setStyle(value));
        highlighting.setOnFinished(event -> highlight());
        area.textProperty().addListener((observable, old, value) -> highlighting.playFromStart());
        sceneProperty().addListener((observable, old, value) -> {
            if (value == null) highlighting.stop();
            else highlight();
        });
        getChildren().add(new VirtualizedScrollPane<>(area));
        MenuItem undo = item("Deshacer", area::undo);
        MenuItem redo = item("Rehacer", area::redo);
        MenuItem cut = item("Cortar", area::cut);
        MenuItem copy = item("Copiar", area::copy);
        MenuItem paste = item("Pegar", area::paste);
        cut.disableProperty().bind(area.editableProperty().not());
        paste.disableProperty().bind(area.editableProperty().not());
        undo.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
                () -> !area.isEditable() || !area.undoAvailableProperty().getValue(),
                area.editableProperty(), area.undoAvailableProperty()));
        redo.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(
                () -> !area.isEditable() || !area.redoAvailableProperty().getValue(),
                area.editableProperty(), area.redoAvailableProperty()));
        area.setContextMenu(new ContextMenu(undo, redo, cut, copy, paste, item("Seleccionar todo", area::selectAll)));
    }

    private static MenuItem item(String title, Runnable action) {
        MenuItem item = new MenuItem(title);
        item.setOnAction(event -> action.run());
        return item;
    }

    private void highlight() {
        area.setStyleSpans(0, CodeSyntax.highlight(area.getText(), language));
        matchedStart = -1;
        matchedEnd = -1;
        highlightedText = area.getText();
        markMatchingBrackets();
    }

    private void markMatchingBrackets() {
        String text = area.getText();
        if (!text.equals(highlightedText)) return;
        for (int position : new int[] { matchedStart, matchedEnd }) {
            if (position >= 0 && position < text.length()) area.setStyle(position, position + 1, List.of("syntax-bracket"));
        }
        matchedStart = -1;
        matchedEnd = -1;
        int caret = area.getCaretPosition();
        int start = caret > 0 && isBracket(text, caret - 1) ? caret - 1 : caret;
        if (!isBracket(text, start)) return;
        String opening = "({[";
        String closing = ")}]";
        char bracket = text.charAt(start);
        boolean forward = opening.indexOf(bracket) >= 0;
        char opposite = forward ? closing.charAt(opening.indexOf(bracket)) : opening.charAt(closing.indexOf(bracket));
        int direction = forward ? 1 : -1;
        int depth = 1;
        for (int index = start + direction; index >= 0 && index < text.length(); index += direction) {
            if (!isBracket(text, index)) continue;
            char current = text.charAt(index);
            if (current == bracket) depth++;
            if (current == opposite) depth--;
            if (depth == 0) {
                matchedStart = start;
                matchedEnd = index;
                area.setStyle(start, start + 1, List.of("syntax-bracket", "matching-bracket"));
                area.setStyle(index, index + 1, List.of("syntax-bracket", "matching-bracket"));
                return;
            }
        }
    }

    private boolean isBracket(String text, int position) {
        return position >= 0 && position < text.length() && "(){}[]".indexOf(text.charAt(position)) >= 0
                && area.getStyleOfChar(position).contains("syntax-bracket");
    }

    public void setLanguage(String value) {
        language = value;
        highlighting.stop();
        highlight();
    }

    public String getText() { return area.getText(); }
    public ObservableValue<String> textProperty() { return area.textProperty(); }
    public void setText(String value) {
        area.replaceText(value == null ? "" : value);
        area.getUndoManager().forgetHistory();
        highlighting.stop();
        highlight();
    }
    public void setEditable(boolean value) { area.setEditable(value); }
    public void setWrapText(boolean value) { area.setWrapText(value); }
    public void replaceSelection(String value) { area.replaceSelection(value); }
    public void selectRange(int start, int end) { area.selectRange(start, end); area.requestFollowCaret(); }
    public int getLength() { return area.getLength(); }
    public int getCaretPosition() { return area.getCaretPosition(); }
    public void positionCaret(int position) { area.moveTo(position); }
    public double getScrollTop() { return area.estimatedScrollYProperty().getValue(); }
    public void setScrollTop(double value) { area.scrollYToPixel(value); }
    @Override public void requestFocus() { area.requestFocus(); }
}
