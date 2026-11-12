package com.mycompany.levelup_code.presentation.component;

import java.util.List;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Curated conceptual activities. Answers are checked locally and do not grant XP. */
public final class ComparisonExercisePane extends VBox {
    private record Activity(String javaCode, String pythonCode, String cppCode,
            String answer, String explanation, String javaTerm, String pythonTerm, String cppTerm) { }

    private static final List<Activity> ACTIVITIES = List.of(
        new Activity("class Animal { String sonido() { return \"A\"; } }\nclass Perro extends Animal { String sonido() { return \"B\"; } }\n// En main:\nAnimal a = new Perro();\nSystem.out.println(a.sonido());",
            "class Animal:\n    def sonido(self): return 'A'\nclass Perro(Animal):\n    def sonido(self): return 'B'\na = Perro()\nprint(a.sonido())",
            "struct Animal { virtual char sonido() { return 'A'; } };\nstruct Perro : Animal { char sonido() override { return 'B'; } };\n// En main, con <iostream>:\nPerro p; Animal& a = p;\nstd::cout << a.sonido();",
            "B", "Se ejecuta el método de Perro mediante despacho dinámico.", "@Override", "def", "virtual"),
        new Activity("// En main:\nRunnable tarea = () -> System.out.print(\"OK\");\ntarea.run();",
            "tarea = lambda: print('OK')\ntarea()",
            "// En main, con <iostream>:\nauto tarea = []() { std::cout << \"OK\"; };\ntarea();",
            "OK", "Una interfaz funcional, una función invocable y una lambda pueden representar la misma acción.", "interface", "def", "virtual"),
        new Activity("int left = 2, right = 8;\nSystem.out.println(left + (right - left) / 2);",
            "left, right = 2, 8\nprint(left + (right - left) // 2)",
            "int left = 2, right = 8;\nstd::cout << left + (right - left) / 2;",
            "5", "El punto medio se calcula desde el límite izquierdo usando división entera.", "while", "while", "while"),
        new Activity("String s = \"casa\";\nSystem.out.println(new StringBuilder(s).reverse());",
            "s = 'casa'\nprint(s[::-1])",
            "std::string s = \"casa\";\nstd::reverse(s.begin(), s.end());\nstd::cout << s;",
            "asac", "Las tres versiones invierten los caracteres. C++ requiere <algorithm>, <string> e <iostream>.", "charAt", "[::-1]", "std::"),
        new Activity("var values = new java.util.ArrayList<Integer>();\nvalues.add(3); values.add(5);\nSystem.out.println(values.size());",
            "values = []\nvalues.append(3)\nvalues.append(5)\nprint(len(values))",
            "std::vector<int> values;\nvalues.push_back(3); values.push_back(5);\nstd::cout << values.size();",
            "2", "La colección contiene dos elementos. size y len miden elementos, no capacidad reservada.", "ArrayList", "list", "vector"),
        new Activity("var values = new java.util.HashSet<Integer>();\nvalues.add(4); values.add(4);\nSystem.out.println(values.size());",
            "values = set()\nvalues.add(4)\nvalues.add(4)\nprint(len(values))",
            "std::unordered_set<int> values;\nvalues.insert(4); values.insert(4);\nstd::cout << values.size();",
            "1", "Un conjunto conserva un solo ejemplar de cada valor.", "HashSet", "set", "unordered_set")
    );
    private final ComboBox<String> language = new ComboBox<>();
    private final TextArea snippet = new TextArea();
    private final TextField answer = new TextField();
    private final Label feedback = new Label();
    private final Label equivalent = new Label();
    private int index;

    public ComparisonExercisePane(Runnable highlight) {
        super(10);
        getStyleClass().add("surface-card");
        Label title = new Label("Predice la salida");
        title.getStyleClass().add("card-title");
        language.getItems().setAll("Java", "Python", "C++");
        language.getSelectionModel().selectFirst();
        language.setOnAction(event -> render());
        snippet.setEditable(false);
        snippet.setPrefRowCount(6);
        snippet.getStyleClass().add("code-editor");
        answer.setPromptText("¿Qué imprime este fragmento?");
        answer.setAccessibleText("Predicción de la salida");
        Button check = new Button("Comprobar respuesta");
        check.getStyleClass().add("btn-primary");
        check.setOnAction(event -> {
            Activity activity = ACTIVITIES.get(index);
            boolean correct = activity.answer().equals(answer.getText().strip());
            feedback.setText(correct ? "Correcto. " + activity.explanation() : "Revisa el fragmento y vuelve a intentarlo.");
            feedback.getStyleClass().setAll(correct ? "text-success" : "text-warning");
        });
        answer.setOnAction(event -> check.fire());
        Button show = new Button("Ver explicación");
        show.setOnAction(event -> feedback.setText("Salida esperada «" + ACTIVITIES.get(index).answer()
                + "». " + ACTIVITIES.get(index).explanation()));
        Button highlightButton = new Button("Localizar equivalencias");
        highlightButton.setOnAction(event -> highlight.run());
        feedback.setWrapText(true);
        equivalent.setWrapText(true);
        getChildren().addAll(title, language, snippet, answer, new FlowPane(8, 8, check, show, highlightButton), feedback, equivalent);
        render();
    }

    public void setTopic(int index) { this.index = index; render(); }

    public List<String> terms() {
        Activity activity = ACTIVITIES.get(index);
        return List.of(activity.javaTerm(), activity.pythonTerm(), activity.cppTerm());
    }

    private void render() {
        Activity activity = ACTIVITIES.get(index);
        snippet.setText(switch (language.getValue()) {
            case "Python" -> activity.pythonCode();
            case "C++" -> activity.cppCode();
            default -> activity.javaCode();
        });
        answer.clear();
        feedback.setText("");
        equivalent.setText("Referencias del ejemplo principal · Java «" + activity.javaTerm()
                + "» · Python «" + activity.pythonTerm() + "» · C++ «" + activity.cppTerm() + "»");
    }
}
