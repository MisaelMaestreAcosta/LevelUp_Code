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
            Map.entry(12, 5), Map.entry(15, 5), Map.entry(18, 5),
            Map.entry(19, 6), Map.entry(25, 6), Map.entry(31, 6),
            Map.entry(20, 7), Map.entry(26, 7), Map.entry(32, 7),
            Map.entry(21, 8), Map.entry(27, 8), Map.entry(33, 8),
            Map.entry(22, 9), Map.entry(28, 9), Map.entry(34, 9),
            Map.entry(23, 10), Map.entry(29, 10), Map.entry(35, 10),
            Map.entry(24, 11), Map.entry(30, 11), Map.entry(36, 11));

    private static final List<List<String>> CODE_HINTS = List.of(
            List.of("Identifica qué sonido corresponde a cada especie.", "Compara los nombres ignorando las diferencias entre mayúsculas y minúsculas.", "Comprueba Perro y Gato antes de devolver el valor para una especie desconocida."),
            List.of("Separa los roles que tienen acceso de los que deben ser rechazados.", "Normaliza el rol antes de compararlo y conserva el texto de salida del enunciado.", "Comprueba ADMIN y ESTUDIANTE. Incluye un resultado para cualquier otro rol."),
            List.of("La secuencia está ordenada. Usa ese orden para descartar una parte de la búsqueda.", "Mantén los límites del intervalo y calcula su punto medio. En Python necesitas división entera.", "Si el valor central es menor que el objetivo, avanza el límite izquierdo. Si es mayor, retrocede el derecho. Comprueba también un valor ausente."),
            List.of("Un palíndromo conserva sus caracteres al leerlo en sentido contrario.", "Normaliza las mayúsculas y compara los dos extremos de la secuencia.", "Avanza hacia el centro mientras coincidan los caracteres. Una sola diferencia permite detener la comprobación."),
            List.of("Solo deben contribuir al resultado los números que cumplen la condición de paridad.", "Prepara un acumulador y recorre cada elemento de la colección.", "Comprueba el resto de la división entre dos antes de sumar. Una colección sin números pares debe conservar el acumulador inicial."),
            List.of("Necesitas recordar los valores que ya aparecieron durante el recorrido.", "Un conjunto permite comprobar si un elemento ya se ha visitado.", "Comprueba la pertenencia antes de insertar cada valor. Devuelve el primer repetido encontrado durante el recorrido y contempla que no haya ninguno."),
            List.of("Define primero el resultado para 0 y 1.", "Cada llamada debe trabajar con un valor menor que la anterior.", "Retorna n multiplicado por el factorial de n - 1 y deja que el caso base detenga la recursión."),
            List.of("Divide la colección hasta obtener segmentos de un elemento.", "Ordena recursivamente las dos mitades.", "Combina las mitades comparando sus primeros elementos pendientes hasta producir una colección ordenada."),
            List.of("Guarda cada delimitador de apertura en una pila.", "Ante un cierre, comprueba el elemento superior antes de retirarlo.", "La expresión es válida solo si todos los cierres coinciden y la pila queda vacía."),
            List.of("El primer turno no espera; los siguientes esperan por los anteriores.", "Conserva un acumulador del tiempo transcurrido.", "Antes de añadir cada duración al tiempo transcurrido, suma ese tiempo al total de espera."),
            List.of("Inserta cada valor comparándolo con el nodo actual.", "Los menores avanzan a la izquierda y los mayores a la derecha.", "La altura de un nodo es uno más que la mayor altura de sus dos subárboles."),
            List.of("Construye una lista de vecinos para cada vértice.", "Inicia una cola con el origen y márcalo como visitado.", "Extrae un vértice, agrega sus vecinos no visitados y al final retorna el tamaño del conjunto de visitados."));

    private static final List<String> EXPANDED_QUIZ_EXPLANATIONS = List.of(
            "Un caso base alcanzable detiene la cadena de llamadas; sin él la recursión continúa hasta agotar la pila.",
            "Merge Sort divide y combina en log n niveles, procesando n elementos por nivel, por eso su tiempo es O(n log n).",
            "LIFO significa último en entrar, primero en salir, por lo que se retira el elemento insertado más recientemente.",
            "FIFO conserva el orden de llegada: el primer elemento insertado es el primero que se atiende.",
            "El recorrido inorden visita primero el subárbol izquierdo, luego la raíz y finalmente el subárbol derecho.",
            "BFS utiliza una cola para explorar por niveles y un conjunto de visitados para no procesar vértices repetidos."
    );

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
        if (challenge instanceof QuizChallenge quiz) {
            String storedExplanation = QUIZ_EXPLANATIONS.get(challenge.getId());
            String explanation = storedExplanation != null ? storedExplanation
                    : topic >= 6 ? EXPANDED_QUIZ_EXPLANATIONS.get(topic - 6) : null;
            if (explanation == null) return ChallengeGuidance.EMPTY;
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
