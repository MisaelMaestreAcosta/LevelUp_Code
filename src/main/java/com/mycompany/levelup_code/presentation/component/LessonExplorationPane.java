package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.OutputPredictionChallenge;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Small local explorations and an optional question from the lesson's published content. */
final class LessonExplorationPane extends VBox {
    private final ComboBox<String> topic = new ComboBox<>();
    private final Spinner<Integer> target = new Spinner<>(0, 40, 12);
    private final ComboBox<String> word = new ComboBox<>();
    private final FlowPane cells = new FlowPane(8, 8);
    private final Label variables = WorkspaceUi.label("", "trace-variables");
    private final Label explanation = WorkspaceUi.label("", "body-text");
    private final Label counter = WorkspaceUi.label("", "muted-text");
    private final Button previous = WorkspaceUi.button("Anterior", "btn-secondary", null);
    private final Button next = WorkspaceUi.button("Siguiente paso", "btn-primary", Theme.IconKind.PLAY);
    private List<LessonTrace.Step> steps = List.of();
    private int position;

    LessonExplorationPane(Lesson lesson) {
        super(14);
        getStyleClass().add("learning-workspace");
        setMinSize(0, 0);
        topic.getItems().addAll("Búsqueda binaria", "Dos punteros", "Primer duplicado", "Suma de números pares");
        topic.setAccessibleText("Algoritmo para explorar");
        word.getItems().addAll("radar", "casa", "anilina", "reconocer");
        word.getSelectionModel().selectFirst();
        word.setAccessibleText("Palabra para comprobar");
        target.setAccessibleText("Valor que buscar en la lista");
        target.setPrefWidth(100);
        previous.setOnAction(event -> { position--; render(); });
        next.setOnAction(event -> { position++; render(); });
        Button reset = WorkspaceUi.button("Reiniciar", "btn-secondary", Theme.IconKind.REFRESH);
        reset.setOnAction(event -> reset());
        topic.setOnAction(event -> reset());
        word.setOnAction(event -> reset());
        target.valueProperty().addListener((observable, old, value) -> reset());
        VBox explorer = new VBox(12, WorkspaceUi.label("Explorador de algoritmos", "card-title"),
                WorkspaceUi.label("Avanza paso a paso y observa cómo cambian los valores.", "body-text"),
                new FlowPane(8, 8, topic, target, word), cells, variables, explanation,
                new FlowPane(8, 8, previous, next, reset), counter);
        explorer.getStyleClass().add("surface-subtle");
        getChildren().add(explorer);
        if (lesson != null) {
            lesson.getChallenges().stream().filter(challenge -> challenge instanceof QuizChallenge
                            && !(challenge instanceof OutputPredictionChallenge)).findFirst()
                    .ifPresent(challenge -> getChildren().add(review((QuizChallenge) challenge)));
        }
        String title = lesson == null || lesson.getTitle() == null ? "" : lesson.getTitle().toLowerCase(java.util.Locale.ROOT);
        topic.getSelectionModel().select(initialTopic(title));
        reset();
    }

    private static int initialTopic(String title) {
        if (title.contains("binaria")) return 0;
        if ((title.contains("punteros") && !title.contains("inteligentes"))
                || title.contains("palíndromo") || title.contains("slicing")) return 1;
        if (title.contains("hash") || title.contains("dict") || title.contains("conjuntos")) return 2;
        return 3;
    }

    private void reset() {
        int selected = topic.getSelectionModel().getSelectedIndex();
        target.setVisible(selected == 0); target.setManaged(selected == 0);
        word.setVisible(selected == 1); word.setManaged(selected == 1);
        steps = switch (selected) {
            case 0 -> LessonTrace.binarySearch(target.getValue());
            case 1 -> LessonTrace.palindrome(word.getValue());
            case 2 -> LessonTrace.firstDuplicate();
            default -> LessonTrace.sumEven();
        };
        position = 0;
        render();
    }

    private void render() {
        LessonTrace.Step step = steps.get(position);
        cells.getChildren().clear();
        for (int index = 0; index < step.cells().size(); index++) {
            Label value = WorkspaceUi.label(step.cells().get(index), "trace-cell");
            if (step.highlighted().contains(index)) value.getStyleClass().add("trace-cell-active");
            value.setAccessibleText("Índice " + index + ", valor " + step.cells().get(index));
            cells.getChildren().add(value);
        }
        variables.setText(step.variables());
        explanation.setText(step.explanation());
        counter.setText("Paso " + (position + 1) + " de " + steps.size()
                + (step.result() == null ? "" : " · Ejemplo finalizado"));
        previous.setDisable(position == 0);
        next.setDisable(position == steps.size() - 1);
    }

    private VBox review(QuizChallenge quiz) {
        Label feedback = WorkspaceUi.label("", "body-text");
        ToggleGroup choices = new ToggleGroup();
        VBox pane = new VBox(10, WorkspaceUi.label("Comprueba lo aprendido", "card-title"),
                WorkspaceUi.label("Repaso libre de esta lección · sin XP", "muted-text"),
                WorkspaceUi.label(quiz.getDescription(), "body-text"));
        for (String option : quiz.getOptions()) {
            RadioButton choice = new RadioButton(option);
            choice.setWrapText(true);
            choice.setToggleGroup(choices);
            choice.setUserData(option);
            choice.getStyleClass().add("quiz-option-card");
            choice.setMaxWidth(Double.MAX_VALUE);
            pane.getChildren().add(choice);
        }
        Button check = WorkspaceUi.button("Comprobar", "btn-secondary", null);
        check.setOnAction(event -> {
            if (choices.getSelectedToggle() == null) feedback.setText("Selecciona una respuesta para comprobarla.");
            else {
                boolean correct = quiz.isCorrectOption((String) choices.getSelectedToggle().getUserData());
                feedback.setText(correct ? "¡Correcto! " + quiz.getCorrectAnswer() : "Revisa la explicación de la lección y prueba de nuevo.");
                feedback.getStyleClass().setAll(correct ? "text-success" : "text-warning");
            }
        });
        choices.selectedToggleProperty().addListener((observable, old, value) -> feedback.setText(""));
        pane.getChildren().addAll(check, feedback);
        pane.getStyleClass().add("surface-subtle");
        return pane;
    }
}
