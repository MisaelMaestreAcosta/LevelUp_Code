package com.mycompany.levelup_code.domain.model;

import java.util.*;

/** One short practice session. Attempts keep using the normal evaluation and persistence flow. */
public final class PracticeSession {
    public record Step(Challenge challenge, String reason) { }
    public record Outcome(int challengeId, boolean correct, boolean saved, int earnedXp, boolean correctSaved) {
        public Outcome(int challengeId, boolean correct, boolean saved, int earnedXp) {
            this(challengeId, correct, saved, earnedXp, correct && saved);
        }
    }
    private final List<Step> steps;
    private final Map<Integer, Outcome> outcomes = new HashMap<>();
    private int position;

    public PracticeSession(List<Step> steps) {
        this.steps = List.copyOf(steps);
        if (steps.stream().map(step -> step.challenge().getId()).distinct().count() != steps.size()) {
            throw new IllegalArgumentException("Una sesión debe contener retos distintos.");
        }
    }

    public List<Step> steps() { return steps; }
    public int position() { return position; }
    public boolean finished() { return position >= steps.size(); }
    public Step current() { return steps.get(position); }
    public Outcome outcome(int challengeId) { return outcomes.get(challengeId); }
    public long correctCount() { return outcomes.values().stream().filter(Outcome::correct).count(); }
    public long savedCount() { return outcomes.values().stream().filter(Outcome::saved).count(); }
    public int earnedXp() { return outcomes.values().stream().mapToInt(Outcome::earnedXp).sum(); }

    public void record(Outcome outcome) {
        if (finished() || current().challenge().getId() != outcome.challengeId()) return;
        outcomes.merge(outcome.challengeId(), outcome, (previous, next) -> new Outcome(next.challengeId(),
                previous.correct() || next.correct(), previous.saved() || next.saved(), Math.max(previous.earnedXp(), next.earnedXp()),
                previous.correctSaved() || next.correctSaved()));
    }

    public void advance() { if (!finished()) position++; }
}
