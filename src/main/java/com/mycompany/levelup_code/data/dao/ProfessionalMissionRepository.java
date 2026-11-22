package com.mycompany.levelup_code.data.dao;

import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import java.util.List;
import java.util.Optional;

public interface ProfessionalMissionRepository {
    List<ProfessionalMission> findPublished();
    Optional<ProfessionalMission> findPublishedByKey(String key);
}
