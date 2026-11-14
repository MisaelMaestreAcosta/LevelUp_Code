package com.mycompany.levelup_code.domain.model;

import com.mycompany.levelup_code.domain.enums.ChallengeType;
import java.util.List;
import java.util.Arrays;
import java.util.stream.IntStream;

public final class CodeOrderingChallenge extends Challenge {
    private final List<String> fragments;
    private final List<Integer> correctOrder;

    public CodeOrderingChallenge(int id, String title, String statement, Lesson lesson, int reward,
            List<String> fragments, List<Integer> correctOrder) {
        super(id, title, statement, lesson, reward);
        if (fragments.size() < 2 || fragments.size() > 50 || fragments.stream().anyMatch(String::isBlank)
                || !correctOrder.stream().sorted().toList().equals(IntStream.rangeClosed(1, fragments.size()).boxed().toList())) {
            throw new IllegalArgumentException("Añade de 2 a 50 fragmentos y un orden que incluya cada número una vez.");
        }
        this.fragments = List.copyOf(fragments);
        this.correctOrder = List.copyOf(correctOrder);
    }
    public List<String> getFragments() { return fragments; }
    public List<Integer> getCorrectOrder() { return correctOrder; }
    public static List<Integer> parseOrder(String value) {
        try { return Arrays.stream(value.strip().split("\\s*,\\s*")).map(Integer::parseInt).toList(); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("Escribe el orden con números separados por comas, por ejemplo 2,1,3."); }
    }
    public static String formatOrder(List<Integer> order) {
        return order.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
    }
    @Override public ChallengeType getType() { return ChallengeType.CODE_ORDERING; }
    @Override public String getSummary() { return "Ordena " + fragments.size() + " fragmentos de código."; }
}
