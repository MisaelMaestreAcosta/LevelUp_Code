package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.domain.model.*;
import java.util.List;
import java.util.Map;

/** Teaching content for the original lessons, matched by stable seed identifiers. */
public final class SeedChallengeGuidance {
    private static final Map<Integer, Integer> TOPICS = Map.ofEntries(
            Map.entry(1, 0), Map.entry(4, 0), Map.entry(7, 0),
            Map.entry(10, 1), Map.entry(13, 1), Map.entry(16, 1),
            Map.entry(2, 2), Map.entry(5, 2), Map.entry(8, 2),
            Map.entry(11, 3), Map.entry(14, 3), Map.entry(17, 3),
            Map.entry(3, 4), Map.entry(6, 4), Map.entry(9, 4),
            Map.entry(12, 5), Map.entry(15, 5), Map.entry(18, 5));

    private static final List<List<String>> CODE_HINTS = List.of(
            List.of("Identifica qué sonido corresponde a cada especie.", "Compara los nombres ignorando las diferencias entre mayúsculas y minúsculas.", "Comprueba Perro y Gato antes de devolver el valor para una especie desconocida."),
            List.of("Separa los roles que tienen acceso de los que deben ser rechazados.", "Normaliza el rol antes de compararlo y conserva el texto de salida del enunciado.", "Comprueba ADMIN y ESTUDIANTE. Incluye un resultado para cualquier otro rol."),
            List.of("La secuencia está ordenada. Usa ese orden para descartar una parte de la búsqueda.", "Mantén los límites del intervalo y calcula su punto medio. En Python necesitas división entera.", "Si el valor central es menor que el objetivo, avanza el límite izquierdo. Si es mayor, retrocede el derecho. Comprueba también un valor ausente."),
            List.of("Un palíndromo conserva sus caracteres al leerlo en sentido contrario.", "Normaliza las mayúsculas y compara los dos extremos de la secuencia.", "Avanza hacia el centro mientras coincidan los caracteres. Una sola diferencia permite detener la comprobación."),
            List.of("Solo deben contribuir al resultado los números que cumplen la condición de paridad.", "Prepara un acumulador y recorre cada elemento de la colección.", "Comprueba el resto de la división entre dos antes de sumar. Una colección sin números pares debe conservar el acumulador inicial."),
            List.of("Necesitas recordar los valores que ya aparecieron durante el recorrido.", "Un conjunto permite comprobar si un elemento ya se ha visitado.", "Comprueba la pertenencia antes de insertar cada valor. Devuelve el primer repetido encontrado durante el recorrido y contempla que no haya ninguno."));

    private static final Map<Integer, String> QUIZ_EXPLANATIONS = Map.ofEntries(
            Map.entry(102, "El despacho dinámico selecciona en ejecución la implementación del objeto real. La sobrecarga se resuelve usando las firmas de los métodos."),
            Map.entry(107, "El principio de sustitución de Liskov exige que el subtipo pueda reemplazar al supertipo respetando su contrato y sin introducir efectos inesperados para quien lo usa."),
            Map.entry(108, "low + (high - low) / 2 evita sumar directamente dos índices grandes que podrían desbordar un int."),
            Map.entry(110, "Los dos punteros recorren como máximo la longitud de la cadena y conservan solo unos pocos índices, por eso el tiempo es O(n) y el espacio adicional O(1)."),
            Map.entry(112, "ArrayList usa un arreglo que puede crecer. El acceso por índice es O(1) y añadir al final tiene coste amortizado O(1)."),
            Map.entry(114, "Dos objetos iguales según equals deben producir el mismo hashCode. Un hash coincidente por sí solo no garantiza igualdad."),
            Map.entry(202, "abc.ABC impide instanciar una subclase que conserva métodos abstractos sin implementar y Python lanza TypeError."),
            Map.entry(204, "__str__ busca una representación legible para el usuario y __repr__ una representación técnica del objeto."),
            Map.entry(206, "El operador / produce un resultado de punto flotante. Los índices de una lista requieren enteros, por eso se utiliza //."),
            Map.entry(208, "El slicing con paso -1 crea una secuencia invertida. Esa nueva secuencia ocupa espacio adicional proporcional a su longitud."),
            Map.entry(210, "CPython reserva capacidad adicional al hacer crecer una lista. Así distribuye el coste de las realocaciones entre muchas llamadas a append y consigue O(1) amortizado."),
            Map.entry(212, "Una tupla de enteros es inmutable y tiene un hash estable. Las listas son mutables y no son hashables, por eso no pueden ser claves de un dict ni elementos de un set."),
            Map.entry(302, "Eliminar un objeto derivado mediante un puntero base requiere un destructor virtual para que se ejecute correctamente la destrucción de la jerarquía."),
            Map.entry(304, "RAII vincula la vida del recurso a la de su objeto. unique_ptr expresa propiedad exclusiva y libera el recurso al destruirse."),
            Map.entry(306, "Una referencia constante evita copiar el vector y permite leerlo sin modificarlo a través de esa referencia."),
            Map.entry(308, "vector, deque y string ofrecen iteradores de acceso aleatorio con saltos it + n en O(1). Los iteradores de list deben avanzar enlace por enlace."),
            Map.entry(310, "reserve(n) garantiza capacidad para al menos n elementos y reduce realocaciones posteriores. La capacidad puede aumentar, pero el tamaño y los elementos existentes se conservan."),
            Map.entry(312, "unordered_map usa una tabla hash con búsqueda promedio O(1). map conserva las claves ordenadas y ofrece búsqueda O(log n), normalmente mediante un árbol balanceado."));

    private SeedChallengeGuidance() { }

    public static ChallengeGuidance forChallenge(Challenge challenge) {
        Integer topic = TOPICS.get(challenge.getLessonId());
        if (topic == null) return ChallengeGuidance.EMPTY;
        if (challenge instanceof QuizChallenge quiz && QUIZ_EXPLANATIONS.containsKey(challenge.getId())) {
            String explanation = QUIZ_EXPLANATIONS.get(challenge.getId());
            List<String> feedback = quiz.getOptions().stream().map(option -> option.equals(quiz.getCorrectAnswer())
                    ? explanation : "La afirmación «" + option + "» no describe el concepto evaluado. " + explanation).toList();
            return new ChallengeGuidance(List.of("Identifica qué propiedad del lenguaje está evaluando la pregunta.",
                    "Contrasta cada afirmación con la teoría de esta lección.", explanation), explanation, feedback);
        }
        if (challenge instanceof CodeChallenge && challenge.getId() < 90000) {
            List<String> hints = CODE_HINTS.get(topic);
            return new ChallengeGuidance(hints, hints.get(2), List.of());
        }
        return ChallengeGuidance.EMPTY;
    }
}
