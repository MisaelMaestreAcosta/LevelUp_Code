package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.domain.enums.ChallengeProgress;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.PracticeSession;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

public final class PracticePlanner {
    public PracticeSession plan(List<Challenge> catalog, Map<Integer, ChallengeProgress> progress,
            Map<Integer, LocalDate> lastAttempts, LocalDate today, Predicate<Challenge> supported) {
        Map<Integer, Challenge> unique = new LinkedHashMap<>();
        catalog.stream().filter(supported).forEach(challenge -> unique.putIfAbsent(challenge.getId(), challenge));
        List<Challenge> candidates = new ArrayList<>(unique.values());
        candidates.sort(Comparator.comparingInt((Challenge challenge) -> priority(challenge, progress, lastAttempts, today))
                .thenComparing(challenge -> lastAttempts.getOrDefault(challenge.getId(), LocalDate.MIN)));
        List<Challenge> selected = new ArrayList<>();
        for (int category = 0; category <= 2; category++) {
            int desired = category;
            candidates.stream().filter(challenge -> priority(challenge, progress, lastAttempts, today) == desired)
                    .findFirst().ifPresent(selected::add);
        }
        for (Challenge candidate : candidates) {
            if (selected.size() == 5) break;
            if (!selected.contains(candidate)) selected.add(candidate);
        }
        return new PracticeSession(selected.stream().map(challenge -> new PracticeSession.Step(challenge,
                switch (priority(challenge, progress, lastAttempts, today)) {
                    case 0 -> "Retoma un reto que todavía necesita repaso";
                    case 1 -> "Avanza con un ejercicio pendiente";
                    case 2 -> "Recupera un concepto que practicaste hace varios días";
                    default -> "Refuerza un concepto ya practicado";
                })).toList());
    }

    private int priority(Challenge challenge, Map<Integer, ChallengeProgress> progress,
            Map<Integer, LocalDate> lastAttempts, LocalDate today) {
        ChallengeProgress state = progress.getOrDefault(challenge.getId(), ChallengeProgress.PENDING);
        if (state == ChallengeProgress.ATTEMPTED) return 0;
        if (state != ChallengeProgress.SOLVED) return 1;
        LocalDate last = lastAttempts.get(challenge.getId());
        return last != null && !last.isAfter(today.minusDays(3)) ? 2 : 3;
    }
}
