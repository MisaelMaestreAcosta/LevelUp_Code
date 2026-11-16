package com.mycompany.levelup_code.presentation.component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Deterministic, inspectable examples used by the lesson explorer. */
final class LessonTrace {
    record Step(List<String> cells, List<Integer> highlighted, String variables, String explanation, Integer result) {
        Step { cells = List.copyOf(cells); highlighted = List.copyOf(highlighted); }
    }
    private LessonTrace() { }

    static List<Step> binarySearch(int target) {
        List<Integer> values = List.of(2, 5, 8, 12, 16, 23, 38);
        List<String> cells = values.stream().map(String::valueOf).toList();
        List<Step> steps = new ArrayList<>();
        int left = 0;
        int right = values.size() - 1;
        steps.add(new Step(cells, List.of(), "objetivo = " + target,
                "La lista está ordenada. En cada comparación podemos descartar una mitad.", null));
        while (left <= right) {
            int middle = left + (right - left) / 2;
            String variables = "izquierda = " + left + " · derecha = " + right + " · medio = " + middle;
            if (values.get(middle) == target) {
                steps.add(new Step(cells, List.of(middle), variables, "Encontramos " + target + " en el índice " + middle + ".", middle));
                return steps;
            }
            boolean smaller = values.get(middle) < target;
            steps.add(new Step(cells, List.of(middle), variables,
                    values.get(middle) + (smaller ? " es menor que " : " es mayor que ") + target
                            + (smaller ? ". Seguimos en la mitad derecha." : ". Seguimos en la mitad izquierda."), null));
            if (smaller) left = middle + 1;
            else right = middle - 1;
        }
        steps.add(new Step(cells, List.of(), "izquierda = " + left + " · derecha = " + right,
                "Se agotó el intervalo. El valor no está en la lista; devolvemos -1.", -1));
        return steps;
    }

    static List<Step> palindrome(String word) {
        List<String> cells = word.codePoints().mapToObj(Character::toString).toList();
        List<Step> steps = new ArrayList<>();
        int left = 0;
        int right = cells.size() - 1;
        steps.add(new Step(cells, List.of(), "izquierda = 0 · derecha = " + right,
                "Comparamos los extremos y avanzamos hacia el centro.", null));
        while (left < right) {
            boolean equal = cells.get(left).equals(cells.get(right));
            steps.add(new Step(cells, List.of(left, right), "izquierda = " + left + " · derecha = " + right,
                    equal ? "Los caracteres coinciden. Movemos ambos punteros hacia el centro."
                            : "Los caracteres son distintos. Esta palabra no es un palíndromo.", equal ? null : 0));
            if (!equal) return steps;
            left++;
            right--;
        }
        steps.add(new Step(cells, List.of(), "izquierda = " + left + " · derecha = " + right,
                "Los punteros llegaron al centro. La palabra es un palíndromo.", 1));
        return steps;
    }

    static List<Step> firstDuplicate() {
        List<Integer> values = List.of(3, 5, 2, 5, 8);
        List<String> cells = values.stream().map(String::valueOf).toList();
        LinkedHashSet<Integer> seen = new LinkedHashSet<>();
        List<Step> steps = new ArrayList<>();
        steps.add(new Step(cells, List.of(), "vistos = []", "Guardaremos los valores que ya recorrimos.", null));
        for (int index = 0; index < values.size(); index++) {
            int value = values.get(index);
            if (!seen.add(value)) {
                steps.add(new Step(cells, List.of(index), "vistos = " + seen,
                        value + " ya estaba en el conjunto. Es el primer valor repetido.", value));
                return steps;
            }
            steps.add(new Step(cells, List.of(index), "vistos = " + seen,
                    value + " es nuevo. Lo añadimos al conjunto.", null));
        }
        return steps;
    }

    static List<Step> sumEven() {
        List<Integer> values = List.of(4, 7, 2, 9, 6);
        List<String> cells = values.stream().map(String::valueOf).toList();
        List<Step> steps = new ArrayList<>();
        int total = 0;
        steps.add(new Step(cells, List.of(), "total = 0", "Sumaremos solamente los números pares de la lista.", null));
        for (int index = 0; index < values.size(); index++) {
            int value = values.get(index);
            boolean even = value % 2 == 0;
            if (even) total += value;
            steps.add(new Step(cells, List.of(index), "valor = " + value + " · total = " + total,
                    even ? "Es par. Lo añadimos al total." : "Es impar. Continuamos sin sumarlo.", null));
        }
        steps.add(new Step(cells, List.of(), "total = " + total, "Terminamos de recorrer la lista. La suma es " + total + ".", total));
        return steps;
    }
}
