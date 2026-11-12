package com.mycompany.levelup_code.domain.model;

import java.time.LocalDate;

public record LearningGoal(int id, Type type, int target, LocalDate start, LocalDate end, int current) {
    public enum Type {
        RETOS("Retos superados"), LECCIONES("Lecturas completadas"), XP("XP obtenidos"), DIAS("Días con XP");
        private final String label;
        Type(String label) { this.label=label; }
        @Override public String toString() { return label; }
    }
    public boolean completed() { return current>=target; }
    public double fraction() { return Math.min(1.0, Math.max(0.0, (double)current/target)); }

    public static void validate(Type type, int target, int days) {
        if (type==null || target<1 || target>10000 || (days!=1 && days!=7)
                || (type==Type.DIAS && target>days)) {
            throw new IllegalArgumentException("Usa un objetivo entre 1 y 10000. Los días activos no pueden superar la duración de la meta.");
        }
    }
}
