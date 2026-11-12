package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.business.service.AuthoringService;
import com.mycompany.levelup_code.data.oracle.OracleAuthoringRepository;
import com.mycompany.levelup_code.data.oracle.OracleChallengeVersionRepository;
import com.mycompany.levelup_code.domain.enums.*;
import com.mycompany.levelup_code.domain.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

/** Editing fields are captured on the FX thread before any database or compilation work. */
public final class ChallengeAuthoringPane extends VBox {
    private record CaseFields(TextArea input, TextArea output, CheckBox hidden, VBox row) { }
    private record OptionFields(TextField text, RadioButton correct, HBox row) { }
    private final User administrator;
    private final List<Lesson> lessons;
    private final Consumer<AuthoredChallenge> savedCallback;
    private final OracleAuthoringRepository repository = new OracleAuthoringRepository();
    private final ComboBox<OracleAuthoringRepository.Entry> entries = new ComboBox<>();
    private final ComboBox<Lesson> lesson = new ComboBox<>();
    private final ComboBox<ChallengeDifficulty> difficulty = new ComboBox<>();
    private final ComboBox<String> kind = new ComboBox<>();
    private final VBox codePanel = new VBox(10);
    private final VBox quizPanel = new VBox(10);
    private final VBox options = new VBox(8);
    private final ToggleGroup correctOption = new ToggleGroup();
    private final List<OptionFields> optionFields = new ArrayList<>();
    private final TextField title = new TextField();
    private final TextArea statement = area(3);
    private final TextArea template = area(7);
    private final TextArea reference = area(7);
    private final TextArea predictionSnippet = area(6);
    private final TextArea fragments = area(7);
    private final TextField correctOrder = new TextField();
    private final VBox orderingPanel = new VBox(10);
    private final VBox predictionPanel;
    private final Spinner<Integer> reward = new Spinner<>(0, 10000, 80);
    private final Spinner<Integer> timeout = new Spinner<>(1, 60, 2);
    private final VBox cases = new VBox(8);
    private final List<CaseFields> caseFields = new ArrayList<>();
    private final VBox form = new VBox(12);
    private final Label status = new Label();
    private int editingId;
    private int revision;

    public ChallengeAuthoringPane(User administrator, List<Lesson> lessons, Consumer<AuthoredChallenge> savedCallback) {
        super(12);
        this.administrator = administrator;
        this.lessons = List.copyOf(lessons);
        this.savedCallback = savedCallback;
        Label heading = new Label("Administrar retos");
        heading.getStyleClass().add("page-title");
        status.setWrapText(true);
        entries.setMaxWidth(Double.MAX_VALUE);
        Button load = button("Cargar reto", () -> {
            var selected = entries.getValue();
            if (selected != null && confirmReplacement()) {
                run(() -> repository.load(administrator.getId(), selected.id()), this::show);
            }
        });
        Button fresh = button("Nuevo reto", () -> { if (confirmReplacement()) reset(); });
        Button duplicate = button("Duplicar", () -> {
            editingId = 0;
            revision = 0;
            lesson.setDisable(false);
            kind.setDisable(false);
            status.setText("Copia preparada. Guarda como borrador o publica cuando esté validada.");
        });
        Button refresh = button("Actualizar lista", this::refresh);
        lesson.getItems().setAll(lessons);
        lesson.setMaxWidth(Double.MAX_VALUE);
        lesson.setConverter(new StringConverter<>() {
            public String toString(Lesson value) {
                return value == null ? "" : value.getModule().getLanguage().getName() + " · " + value.getTitle();
            }
            public Lesson fromString(String value) { return null; }
        });
        lesson.getSelectionModel().selectFirst();
        difficulty.getItems().setAll(ChallengeDifficulty.values());
        difficulty.setValue(ChallengeDifficulty.MEDIO);
        kind.getItems().setAll("Código", "Quiz", "Predicción de salida", "Depuración", "Ordenar fragmentos");
        kind.setValue("Código");
        kind.setOnAction(event -> updateKind());
        Button addCase = button("Añadir caso", () -> addCase("", "", false));
        Button addOption = button("Añadir opción", () -> addOption("", false));
        codePanel.getChildren().addAll(field("Plantilla para el estudiante", template),
                field("Solución de referencia para validar", reference), cases, addCase);
        predictionPanel = field("Código para predecir la salida", predictionSnippet);
        quizPanel.getChildren().addAll(predictionPanel, new Label("Selecciona la opción correcta con el círculo."), options, addOption);
        correctOrder.setPromptText("Ejemplo 2,1,3");
        orderingPanel.getChildren().addAll(field("Fragmentos, uno por línea", fragments),
                field("Orden correcto de los números de línea", correctOrder));
        Button versions = button("Consultar versiones", this::showVersions);
        Button draft = button("Guardar borrador", () -> save(PublicationState.BORRADOR));
        Button publish = button("Validar y publicar", () -> save(PublicationState.PUBLICADO));
        publish.getStyleClass().add("btn-primary");
        Button archive = button("Archivar", () -> {
            if (editingId == 0) { status.setText("Carga primero un reto guardado."); return; }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Archivar este reto? Su historial se conservará.", ButtonType.OK, ButtonType.CANCEL);
            alert.initOwner(getScene().getWindow());
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) save(PublicationState.ARCHIVADO);
        });
        Button preview = button("Vista previa", () -> {
            try {
                Challenge challenge = capture(PublicationState.BORRADOR).challenge();
                ChallengeFrame.open(administrator, challenge, null, true);
            } catch (RuntimeException ex) { status.setText(ex.getMessage()); }
        });
        form.getChildren().addAll(entries, new FlowPane(8, 8, load, fresh, duplicate, refresh),
                field("Lección", lesson), field("Tipo", kind), field("Título", title), field("Enunciado", statement),
                new FlowPane(12, 8, field("Dificultad", difficulty), field("XP", reward), field("Límite en segundos", timeout)),
                codePanel, quizPanel, orderingPanel, new FlowPane(8, 8, draft, publish, archive, preview, versions));
        getChildren().addAll(heading, status, form);
        reset();
        refresh();
    }

    private static TextArea area(int rows) {
        TextArea area = new TextArea();
        area.setPrefRowCount(rows);
        area.getStyleClass().add("code-editor");
        return area;
    }

    private static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("btn-secondary");
        button.setOnAction(event -> action.run());
        return button;
    }

    private static VBox field(String name, Node control) {
        Label label = new Label(name);
        label.setLabelFor(control);
        return new VBox(5, label, control);
    }

    private void addCase(String input, String output, boolean hidden) {
        TextArea inputField = area(2);
        inputField.setText(input);
        TextArea outputField = area(2);
        outputField.setText(output);
        CheckBox hiddenField = new CheckBox("Caso oculto");
        hiddenField.setSelected(hidden);
        VBox row = new VBox(6);
        CaseFields fields = new CaseFields(inputField, outputField, hiddenField, row);
        Button remove = button("Quitar caso", () -> { caseFields.remove(fields); cases.getChildren().remove(row); });
        row.getStyleClass().add("surface-subtle");
        row.getChildren().addAll(field("Entrada", inputField), field("Salida esperada", outputField),
                new FlowPane(8, 8, hiddenField, remove));
        caseFields.add(fields);
        cases.getChildren().add(row);
    }

    private AuthoredChallenge capture(PublicationState state) {
        Challenge challenge;
        if ("Ordenar fragmentos".equals(kind.getValue())) {
            challenge = new CodeOrderingChallenge(editingId, title.getText().strip(), statement.getText(), lesson.getValue(),
                    reward.getValue(), fragments.getText().lines().filter(line -> !line.isBlank()).toList(),
                    CodeOrderingChallenge.parseOrder(correctOrder.getText()));
        } else if ("Quiz".equals(kind.getValue()) || "Predicción de salida".equals(kind.getValue())) {
            String answer = optionFields.stream().filter(value -> value.correct().isSelected())
                    .map(value -> value.text().getText()).findFirst().orElse("");
            List<String> values = optionFields.stream().map(value -> value.text().getText()).toList();
            challenge = "Predicción de salida".equals(kind.getValue())
                    ? new OutputPredictionChallenge(editingId, title.getText().strip(), statement.getText(), lesson.getValue(),
                            reward.getValue(), values, answer, predictionSnippet.getText())
                    : new QuizChallenge(editingId, title.getText().strip(), statement.getText(), lesson.getValue(), reward.getValue(), values, answer);
        } else {
            CodeChallenge code = "Depuración".equals(kind.getValue())
                    ? new DebuggingChallenge(editingId, title.getText().strip(), statement.getText(), lesson.getValue(),
                            reward.getValue(), template.getText(), timeout.getValue())
                    : new CodeChallenge(editingId, title.getText().strip(), statement.getText(),
                            lesson.getValue(), reward.getValue(), template.getText(), timeout.getValue());
            for (CaseFields fields : caseFields) {
                code.addTestCase(new TestCase(0, code, fields.input().getText(), fields.output().getText(), fields.hidden().isSelected()));
            }
            challenge = code;
        }
        challenge.setDifficulty(difficulty.getValue());
        challenge.setRevision(revision);
        AuthoredChallenge authored = new AuthoredChallenge(challenge, reference.getText(), state, revision);
        AuthoringService.validate(authored);
        return authored;
    }

    private void save(PublicationState state) {
        try {
            AuthoredChallenge captured = capture(state);
            run(() -> new AuthoringService().save(administrator.getId(), captured), saved -> {
                show(saved);
                savedCallback.accept(saved);
                status.setText("Guardado como " + saved.state() + ". Actualiza la lista para consultar los cambios.");
            });
        } catch (RuntimeException ex) { status.setText(ex.getMessage()); }
    }

    private void show(AuthoredChallenge authored) {
        Challenge challenge = authored.challenge();
        editingId = challenge.getId();
        revision = authored.revision();
        lesson.setValue(lessons.stream().filter(value -> value.getId() == challenge.getLessonId()).findFirst().orElse(null));
        lesson.setDisable(true);
        kind.setValue(challenge.getType().label());
        kind.setDisable(true);
        title.setText(challenge.getTitle());
        statement.setText(challenge.getDescription());
        difficulty.setValue(challenge.getDifficulty());
        reward.getValueFactory().setValue(challenge.getExpReward());
        reference.setText(authored.referenceSolution());
        cases.getChildren().clear();
        caseFields.clear();
        options.getChildren().clear(); optionFields.clear(); correctOption.getToggles().clear();
        if (challenge instanceof CodeChallenge code) {
            timeout.getValueFactory().setValue(code.getTimeLimitSeconds());
            template.setText(code.getInitialTemplate());
            for (TestCase test : code.getTestCases()) addCase(test.getInput(), test.getExpectedOutput(), test.isHidden());
        } else if (challenge instanceof QuizChallenge quiz) {
            for (String option : quiz.getOptions()) addOption(option, option.equals(quiz.getCorrectAnswer()));
            if (quiz instanceof OutputPredictionChallenge prediction) predictionSnippet.setText(prediction.getCodeSnippet());
        } else if (challenge instanceof CodeOrderingChallenge ordering) {
            fragments.setText(String.join("\n", ordering.getFragments()));
            correctOrder.setText(CodeOrderingChallenge.formatOrder(ordering.getCorrectOrder()));
        }
        updateKind();
        status.setText("Editando " + authored.state() + " · revisión " + revision);
    }

    private void reset() {
        editingId = 0;
        revision = 0;
        lesson.setDisable(false);
        kind.setDisable(false);
        kind.setValue("Código");
        title.clear(); statement.clear(); template.clear(); reference.clear();
        predictionSnippet.clear(); fragments.clear(); correctOrder.clear();
        cases.getChildren().clear(); caseFields.clear();
        addCase("", "", false);
        options.getChildren().clear(); optionFields.clear(); correctOption.getToggles().clear();
        addOption("", true); addOption("", false);
        updateKind();
        status.setText("Completa la plantilla y la solución de referencia en sus campos respectivos.");
    }

    private boolean confirmReplacement() {
        if (title.getText().isBlank() && statement.getText().isBlank()) return true;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Reemplazar el formulario actual? Guarda los cambios que quieras conservar.", ButtonType.OK, ButtonType.CANCEL);
        alert.initOwner(getScene().getWindow());
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void refresh() { run(() -> repository.list(administrator.getId()), values -> entries.getItems().setAll(values)); }

    private void updateKind() {
        boolean code = "Código".equals(kind.getValue()) || "Depuración".equals(kind.getValue());
        boolean ordering = "Ordenar fragmentos".equals(kind.getValue());
        codePanel.setVisible(code); codePanel.setManaged(code);
        quizPanel.setVisible(!code && !ordering); quizPanel.setManaged(!code && !ordering);
        orderingPanel.setVisible(ordering); orderingPanel.setManaged(ordering);
        boolean prediction = "Predicción de salida".equals(kind.getValue());
        predictionPanel.setVisible(prediction); predictionPanel.setManaged(prediction);
    }

    private void addOption(String text, boolean selected) {
        TextField value = new TextField(text);
        value.setAccessibleText("Texto de la opción");
        HBox.setHgrow(value, Priority.ALWAYS);
        RadioButton correct = new RadioButton("Correcta");
        correct.setToggleGroup(correctOption);
        correct.setSelected(selected);
        HBox row = new HBox(8);
        OptionFields fields = new OptionFields(value, correct, row);
        Button remove = button("Quitar", () -> {
            correctOption.getToggles().remove(correct);
            optionFields.remove(fields);
            options.getChildren().remove(row);
        });
        row.getChildren().addAll(value, correct, remove);
        optionFields.add(fields); options.getChildren().add(row);
    }

    private void showVersions() {
        if (editingId <= 0) { status.setText("Carga un reto guardado para consultar sus versiones."); return; }
        int id = editingId;
        run(() -> new OracleChallengeVersionRepository().list(administrator.getId(), id), versions -> {
            ComboBox<OracleChallengeVersionRepository.Version> select = new ComboBox<>();
            select.getItems().setAll(versions);
            TextArea details = area(18);
            details.setEditable(false);
            select.setOnAction(event -> {
                if (select.getValue() != null) details.setText(select.getValue().details());
            });
            select.getSelectionModel().selectFirst();
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.initOwner(getScene().getWindow());
            dialog.setTitle("Versiones del reto");
            dialog.setHeaderText("Contenido guardado en cada revisión");
            dialog.getDialogPane().setContent(new VBox(10, select, details));
            dialog.setResizable(true);
            dialog.show();
        });
    }

    private <T> void run(Callable<T> operation, Consumer<T> success) {
        form.setDisable(true);
        status.setText("Procesando el contenido…");
        Task<T> task = new Task<>() { protected T call() throws Exception { return operation.call(); } };
        task.setOnSucceeded(event -> { form.setDisable(false); status.setText("Lista actualizada."); success.accept(task.getValue()); });
        task.setOnFailed(event -> {
            form.setDisable(false);
            Throwable failure = task.getException();
            status.setText(failure instanceof IllegalArgumentException || failure instanceof IllegalStateException || failure instanceof SecurityException
                    ? failure.getMessage() : "No se pudo guardar o cargar. Comprueba la conexión y las migraciones de contenido. El formulario se conserva.");
        });
        Thread worker = new Thread(task, "challenge-authoring");
        worker.setDaemon(true);
        worker.start();
    }
}
