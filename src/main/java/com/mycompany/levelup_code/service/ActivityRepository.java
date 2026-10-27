package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.DailyActivity;
import java.time.LocalDate;
import java.util.Optional;

/** Contrato de persistencia para la actividad diaria de los usuarios. */
public interface ActivityRepository {

    Optional<DailyActivity> findByUserAndDate(int userId, LocalDate date);

    DailyActivity saveOrUpdate(DailyActivity activity);
}
