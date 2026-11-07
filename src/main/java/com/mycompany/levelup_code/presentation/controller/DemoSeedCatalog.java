package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.ChallengeCountAchievement;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.ExperienceAchievement;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.StreakAchievement;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Catálogo aislado de datos semilla y fragmentos comparativos de demostración.
 * Se utiliza exclusivamente cuando la aplicación opera en modo local sin conexión a Oracle,
 * manteniendo la vista {@code DashboardFrame} libre de datos de ejemplo incrustados.
 */
public final class DemoSeedCatalog {

    private DemoSeedCatalog() {
    }

    public static User createDefaultStudent() {
        User u = new User();
        u.setId(1);
        u.setUsername("Estudiante");
        u.setEmail("estudiante@levelup.edu.co");
        u.setExperience(1450);
        u.setLevel(5);
        u.setCurrentStreak(7);
        u.setMaxStreak(12);
        return u;
    }

    public static List<Language> createSeedLanguages() {
        Language javaLang = new Language(1, "Java", "Java 21 LTS — POO, Interfaces, Colecciones y Concurrencia.", true);
        Language pythonLang = new Language(2, "Python", "Python 3.12 — Tipado dinámico, ABC y estructuras expresivas.", true);
        Language cppLang = new Language(3, "C++", "ISO C++ 20 — Control determinista de memoria, punteros y STL.", true);

        CourseModule javaMod = new CourseModule(1, "Arquitectura Orientada a Objetos y Colecciones",
                "Abstracción, herencia y estructuras lineales", 1, javaLang);
        Lesson jLesson1 = new Lesson(1, "Herencia, Clases Abstractas y Despacho Polimórfico",
                "En Java, una clase abstracta define el contrato común para sus subclases y delega la implementación mediante métodos abstractos.",
                1, 60, javaMod);
        CodeChallenge jCode = new CodeChallenge(101, "Polimorfismo: Despacho Dinámico de Sonidos",
                "Implementa emitirSonido(String tipo) para que retorne \"Guau\" cuando reciba \"Perro\" y \"Miau\" cuando reciba \"Gato\".",
                jLesson1, 60,
                "public class AnimalSoundSolver {\n    public static String emitirSonido(String tipo) {\n        if (\"Perro\".equalsIgnoreCase(tipo)) {\n            return \"Guau\";\n        }\n        return \"\";\n    }\n}",
                2);
        jCode.addTestCase(new TestCase(1, jCode, "Perro", "Guau", false));
        jCode.addTestCase(new TestCase(2, jCode, "Gato", "Miau", false));
        jLesson1.addChallenge(jCode);

        QuizChallenge jQuiz = new QuizChallenge(102, "Quiz Conceptual: Enlace Dinámico en POO",
                "¿Qué mecanismo permite que una referencia de tipo superclase invoque la implementación sobrescrita (@Override) en tiempo de ejecución?",
                jLesson1, 40,
                Arrays.asList(
                        "Sobrecarga estática en tiempo de compilación (Overloading)",
                        "Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)",
                        "Conversión implícita de tipos primitivos en la pila",
                        "Duplicación de constructores en la clase base"
                ),
                "Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)"
        );
        jLesson1.addChallenge(jQuiz);

        Lesson jLesson2 = new Lesson(2, "Búsqueda Binaria O(log n) libre de desbordamiento",
                "El cálculo seguro del punto medio utiliza low + (high - low) / 2 para evitar desbordamiento aritmético.",
                2, 75, javaMod);
        jLesson2.addChallenge(jCode);

        Lesson jLesson3 = new Lesson(3, "Estructuras Dinámicas en el Heap: ArrayList<T>",
                "ArrayList redimensiona su arreglo interno con costo amortizado O(1) al insertar al final.",
                3, 80, javaMod);
        jLesson3.addChallenge(jQuiz);

        javaMod.addLesson(jLesson1);
        javaMod.addLesson(jLesson2);
        javaMod.addLesson(jLesson3);
        javaLang.addModule(javaMod);

        CourseModule pyMod = new CourseModule(2, "Protocolos, Listas y Expresividad Algorítmica",
                "Duck Typing, abc.ABC y búsqueda eficiente", 1, pythonLang);
        Lesson pyLesson1 = new Lesson(4, "Protocolos Abstractos (abc.ABC) y Duck Typing",
                "En Python, las clases base abstractas se definen heredando de abc.ABC y decorando métodos con @abstractmethod.",
                1, 60, pyMod);
        CodeChallenge pyCode = new CodeChallenge(201, "Protocolo Animal en Python",
                "Implementa emitir_sonido(tipo: str) -> str para que retorne \"Guau\" o \"Miau\".",
                pyLesson1, 60,
                "def emitir_sonido(tipo: str) -> str:\n    if tipo.lower() == \"perro\":\n        return \"Guau\"\n    return \"\"\n",
                2);
        pyCode.addTestCase(new TestCase(10, pyCode, "Perro", "Guau", false));
        pyCode.addTestCase(new TestCase(11, pyCode, "Gato", "Miau", false));
        pyLesson1.addChallenge(pyCode);
        pyMod.addLesson(pyLesson1);
        pythonLang.addModule(pyMod);

        CourseModule cppMod = new CourseModule(3, "Memoria, Referencias y Contenedores STL",
                "RAII y std::vector", 1, cppLang);
        Lesson cppLesson1 = new Lesson(7, "Funciones Virtuales Puras (= 0) y Tabla Virtual (vtable)",
                "En C++, el polimorfismo dinámico se activa declarando el método base como virtual.",
                1, 60, cppMod);
        CodeChallenge cppCode = new CodeChallenge(301, "Despacho Virtual en C++",
                "Implementa emitirSonido(const std::string& input) para retornar \"Guau\" o \"Miau\".",
                cppLesson1, 60,
                "#include <string>\n\nstd::string emitirSonido(const std::string& input) {\n    if (input == \"Perro\") return \"Guau\";\n    return \"\";\n}\n",
                2);
        cppCode.addTestCase(new TestCase(20, cppCode, "Perro", "Guau", false));
        cppCode.addTestCase(new TestCase(21, cppCode, "Gato", "Miau", false));
        cppLesson1.addChallenge(cppCode);
        cppMod.addLesson(cppLesson1);
        cppLang.addModule(cppMod);

        return Arrays.asList(javaLang, pythonLang, cppLang);
    }

    public static List<Achievement> createSeedAchievements() {
        List<Achievement> list = new ArrayList<>();
        list.add(new StreakAchievement(1, "Constancia de Acero", "Mantén una racha activa de 3 días seguidos practicando.", "FLAME", 3));
        list.add(new StreakAchievement(2, "Hábito Algorítmico", "Alcanza una racha continua de 7 días de estudio.", "FLAME", 7));
        list.add(new ExperienceAchievement(3, "Explorador Políglota", "Acumula tus primeros 1,000 puntos de experiencia (XP).", "BOLT", 1000));
        list.add(new ExperienceAchievement(4, "Arquitecto Senior", "Supera la barrera de los 1,500 XP resolviendo lecciones y retos.", "TROPHY", 1500));
        list.add(new ChallengeCountAchievement(5, "Domador de Casos de Prueba", "Resuelve exitosamente 5 retos de código o quiz.", "CODE", 5));
        list.add(new StreakAchievement(6, "Leyenda del Semestre", "Alcanza una racha histórica de 14 días consecutivos.", "SHIELD", 14));
        return list;
    }

    public static Map<LocalDate, Integer> createSampleActivityHistory() {
        Map<LocalDate, Integer> map = new HashMap<>();
        LocalDate today = LocalDate.now();
        int[] sampleXp = {60, 0, 120, 0, 80, 0, 0, 120, 180, 90, 210, 150, 240, 160};
        for (int i = 0; i < sampleXp.length; i++) {
            LocalDate d = today.minusDays(sampleXp.length - 1 - i);
            if (sampleXp[i] > 0) {
                map.put(d, sampleXp[i]);
            }
        }
        return map;
    }
}

