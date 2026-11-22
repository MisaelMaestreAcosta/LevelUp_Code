package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.ProfessionalMissionRepository;
import com.mycompany.levelup_code.domain.model.MissionRequirement;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import com.mycompany.levelup_code.exception.BusinessException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class OracleProfessionalMissionRepository implements ProfessionalMissionRepository {
    @Override
    public List<ProfessionalMission> findPublished() { return query(null); }

    @Override
    public Optional<ProfessionalMission> findPublishedByKey(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        return query(key).stream().findFirst();
    }

    private List<ProfessionalMission> query(String key) {
        Map<String, MissionRow> missions = new LinkedHashMap<>();
        try (var connection = DatabaseConnection.getConnection();
             var statement = OracleCalls.cursor(connection, "PKG_RETOS.misiones_publicadas", key)) {
            try (var rows = OracleCalls.rows(statement)) {
                while (rows.next()) {
                    String code = rows.getString(1);
                    MissionRow mission = missions.get(code);
                    if (mission == null) {
                        mission = new MissionRow(code, rows.getInt(2), rows.getString(3),
                                rows.getString(4), rows.getString(5), new LinkedHashMap<>());
                        missions.put(code, mission);
                    }
                    Integer order = rows.getObject(6, Integer.class);
                    if (order == null) continue;
                    RequirementRow requirement = mission.requirements().get(order);
                    if (requirement == null) {
                        requirement = new RequirementRow(rows.getString(7), new ArrayList<>());
                        mission.requirements().put(order, requirement);
                    }
                    Integer caseId = rows.getObject(8, Integer.class);
                    if (caseId != null) requirement.cases().add(caseId);
                }
            }
        } catch (SQLException ex) {
            throw new BusinessException("No pudimos cargar las misiones. Comprueba Oracle y la migración 15.", ex);
        }
        return missions.values().stream().map(MissionRow::toMission).toList();
    }

    private record RequirementRow(String text, List<Integer> cases) { }

    private record MissionRow(String key, int challengeId, String title, String client, String brief,
            Map<Integer, RequirementRow> requirements) {
        ProfessionalMission toMission() {
            return new ProfessionalMission(key, challengeId, title, client, brief, requirements.values().stream()
                    .map(row -> new MissionRequirement(row.text(), row.cases())).toList());
        }
    }
}
