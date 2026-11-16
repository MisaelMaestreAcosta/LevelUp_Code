package com.mycompany.levelup_code.data.dao;

import com.mycompany.levelup_code.domain.model.DailyActivity;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Contrato de persistencia para la actividad diaria de los usuarios. */
public interface ActivityRepository {

    Optional<DailyActivity> findByUserAndDate(int userId, LocalDate date);

    DailyActivity saveOrUpdate(DailyActivity activity);

    default List<DailyActivity> findRecentByUser(int userId, int days) {
        return Collections.emptyList();
    }
}
