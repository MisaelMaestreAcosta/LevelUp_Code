package com.mycompany.levelup_code.data.local;

import com.mycompany.levelup_code.data.dao.ProfessionalMissionRepository;
import com.mycompany.levelup_code.domain.model.ChallengeGuidance;
import com.mycompany.levelup_code.domain.model.DebuggingChallenge;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.MissionRequirement;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import com.mycompany.levelup_code.domain.model.TestCase;
import java.util.List;
import java.util.Optional;

/** Demo counterpart to the discount mission installed by migration 16. */
public final class DemoProfessionalMissionRepository implements ProfessionalMissionRepository {
    private static final int CHALLENGE_ID = 96001;
    private static final int FIRST_CASE_ID = 96101;
    private static final List<Example> EXAMPLES = List.of(
            new Example(1, "10000,10", "9000", false),
            new Example(1, "1999,25", "1499", false),
            new Example(1, "101,50", "51", true),
            new Example(1, "2147483647,1", "2126008811", true),
            new Example(2, "2500,0", "2500", false),
            new Example(2, "2500,100", "0", true),
            new Example(3, "-20,10", "-1", false),
            new Example(3, "500,-1", "-1", true),
            new Example(3, "500,101", "-1", true));
    private static final List<String> REQUIREMENTS = List.of(
            "Aplica el porcentaje al precio y redondea el resultado al centavo más cercano, con medio centavo hacia arriba.",
            "Un descuento del 0 % conserva el precio y uno del 100 % deja el precio en cero.",
            "Devuelve -1 si el precio es negativo o el porcentaje está fuera de 0 a 100.",
            "Conserva el método finalPrice(int priceCents, int percent) y revisa que el cambio sea fácil de leer.");
    private static final ProfessionalMission DISCOUNT = new ProfessionalMission(
            "descuentos-tienda", CHALLENGE_ID, "El descuento de la tienda está fallando",
            "Tienda Horizonte",
            "El equipo de ventas detectó que el sistema está restando el número del descuento como si fuera dinero. Corrige el cálculo antes de activar la próxima promoción.",
            requirements());

    private record Example(int requirement, String input, String expected, boolean hidden) { }

    @Override
    public List<ProfessionalMission> findPublished() { return List.of(DISCOUNT); }

    @Override
    public Optional<ProfessionalMission> findPublishedByKey(String key) {
        return DISCOUNT.key().equals(key) ? Optional.of(DISCOUNT) : Optional.empty();
    }

    public static DebuggingChallenge discountChallenge(Lesson lesson) {
        var challenge = new DebuggingChallenge(CHALLENGE_ID, DISCOUNT.title(),
                "Corrige finalPrice(int priceCents, int percent). Devuelve el precio final en centavos tras aplicar un porcentaje entero. Redondea al centavo más cercano y, si queda exactamente medio centavo, redondea hacia arriba. Devuelve -1 para un precio negativo o un porcentaje fuera de 0 a 100. Conserva la firma del método.",
                lesson, 80, """
                public class DiscountSolver {
                    public static int finalPrice(int priceCents, int percent) {
                        if (priceCents < 0 || percent < 0) {
                            return -1;
                        }
                        return priceCents - percent;
                    }
                }
                """, 2);
        challenge.setGuidance(new ChallengeGuidance(List.of(
                "El precio está en centavos y el descuento es un porcentaje. Identifica qué parte del precio debe pagar el cliente.",
                "Valida ambos extremos del porcentaje antes de calcular. Usa una operación intermedia con long para precios grandes.",
                "Calcula el precio multiplicado por el porcentaje restante. Si haces la división entera entre 100, piensa qué sumar antes para redondear medio centavo hacia arriba."),
                "El porcentaje restante determina el precio final. Multiplicar con long evita el desbordamiento y sumar 50 antes de dividir entre 100 aplica el redondeo al centavo.", List.of()));
        for (int i = 0; i < EXAMPLES.size(); i++) {
            Example example = EXAMPLES.get(i);
            challenge.addTestCase(new TestCase(FIRST_CASE_ID + i, challenge, example.input(), example.expected(), example.hidden()));
        }
        return challenge;
    }

    private static List<MissionRequirement> requirements() {
        return java.util.stream.IntStream.range(0, REQUIREMENTS.size()).mapToObj(index ->
                new MissionRequirement(REQUIREMENTS.get(index),
                        java.util.stream.IntStream.range(0, EXAMPLES.size())
                                .filter(i -> EXAMPLES.get(i).requirement() == index + 1)
                                .map(i -> FIRST_CASE_ID + i).boxed().toList())).toList();
    }
}
