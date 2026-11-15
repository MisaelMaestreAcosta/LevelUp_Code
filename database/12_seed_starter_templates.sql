-- Apply after 10_challenge_types.sql as the application schema owner.
-- Re-runnable data migration for the 18 original code exercises.
-- Updates only untouched revision-1 seed templates. Custom content is skipped.
-- Preserves attempts and earlier versions, and creates a new content revision.
SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
    v_updated NUMBER := 0;

    PROCEDURE separate_template(p_id NUMBER, p_original CLOB, p_starter CLOB) IS
        v_revision NUMBER;
        v_source CLOB;
        v_version NUMBER;
    BEGIN
        BEGIN
            SELECT r.revision,c.codigo_inicial INTO v_revision,v_source
            FROM Retos r JOIN Reto_Codigo c ON c.id_reto=r.id_reto
            WHERE r.id_reto=p_id AND r.tipo_reto='CODIGO' AND r.estado='PUBLICADO'
            FOR UPDATE OF r.revision,c.codigo_inicial;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN RETURN;
        END;

        IF v_revision <> 1 OR v_source IS NULL THEN RETURN; END IF;
        IF DBMS_LOB.COMPARE(v_source,p_original) <> 0
           AND DBMS_LOB.COMPARE(v_source,p_starter) <> 0 THEN RETURN; END IF;

        INSERT INTO Solucion_Referencia(id_reto,codigo_solucion)
        SELECT p_id,p_original FROM dual
        WHERE NOT EXISTS (SELECT 1 FROM Solucion_Referencia WHERE id_reto=p_id);

        UPDATE Reto_Codigo SET codigo_inicial=p_starter WHERE id_reto=p_id;
        UPDATE Retos SET revision=revision+1,fecha_actualizacion=SYSTIMESTAMP WHERE id_reto=p_id;

        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,
            exp_recompensa,codigo_inicial,limite_tiempo_ms,solucion_referencia,estado)
        SELECT r.id_reto,r.revision,r.titulo_reto,r.enunciado,r.tipo_reto,r.dificultad,
            r.exp_recompensa,c.codigo_inicial,c.limite_tiempo_ms,s.codigo_solucion,r.estado
        FROM Retos r JOIN Reto_Codigo c ON c.id_reto=r.id_reto
        JOIN Solucion_Referencia s ON s.id_reto=r.id_reto WHERE r.id_reto=p_id;

        SELECT id_version INTO v_version FROM Version_Reto WHERE id_reto=p_id AND revision=v_revision+1;
        INSERT INTO Version_Caso_Prueba(id_version,numero,entrada,salida_esperada,es_oculto)
        SELECT v_version,ROW_NUMBER() OVER(ORDER BY id_caso),entrada,salida_esperada,es_oculto
        FROM Casos_Prueba WHERE id_reto=p_id;
        v_updated := v_updated+1;
    END;
BEGIN
    SAVEPOINT starter_migration;
    separate_template(101,
        'public class AnimalSoundSolver {' || CHR(10) ||
        '    public static String emitirSonido(String tipo) {' || CHR(10) ||
        '        if ("Perro".equalsIgnoreCase(tipo)) {' || CHR(10) ||
        '            return "Guau";' || CHR(10) ||
        '        }' || CHR(10) ||
        '        if ("Gato".equalsIgnoreCase(tipo)) {' || CHR(10) ||
        '            return "Miau";' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return "Desconocido";' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class AnimalSoundSolver {' || CHR(10) ||
        '    public static String emitirSonido(String tipo) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return "";' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(106,
        'public class AccessPolicySolver {' || CHR(10) ||
        '    public static String nivelAcceso(String rol) {' || CHR(10) ||
        '        if ("ADMIN".equalsIgnoreCase(rol)) {' || CHR(10) ||
        '            return "TOTAL";' || CHR(10) ||
        '        }' || CHR(10) ||
        '        if ("ESTUDIANTE".equalsIgnoreCase(rol)) {' || CHR(10) ||
        '            return "LECTURA";' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return "DENEGADO";' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class AccessPolicySolver {' || CHR(10) ||
        '    public static String nivelAcceso(String rol) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return "";' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(103,
        'public class BinarySearchSolver {' || CHR(10) ||
        '    public static int search(int[] a, int target) {' || CHR(10) ||
        '        int low = 0, high = a.length - 1;' || CHR(10) ||
        '        while (low <= high) {' || CHR(10) ||
        '            int mid = low + (high - low) / 2;' || CHR(10) ||
        '            if (a[mid] == target) return mid;' || CHR(10) ||
        '            if (a[mid] < target) low = mid + 1;' || CHR(10) ||
        '            else high = mid - 1;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return -1;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class BinarySearchSolver {' || CHR(10) ||
        '    public static int search(int[] a, int target) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return 0;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(109,
        'public class PalindromeSolver {' || CHR(10) ||
        '    public static boolean esPalindromo(String texto) {' || CHR(10) ||
        '        String s = texto.toLowerCase();' || CHR(10) ||
        '        int left = 0, right = s.length() - 1;' || CHR(10) ||
        '        while (left < right) {' || CHR(10) ||
        '            if (s.charAt(left) != s.charAt(right)) {' || CHR(10) ||
        '                return false;' || CHR(10) ||
        '            }' || CHR(10) ||
        '            left++;' || CHR(10) ||
        '            right--;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return true;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class PalindromeSolver {' || CHR(10) ||
        '    public static boolean esPalindromo(String texto) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return false;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(111,
        'public class EvenSumSolver {' || CHR(10) ||
        '    public static int sumarPares(int[] nums) {' || CHR(10) ||
        '        int total = 0;' || CHR(10) ||
        '        for (int n : nums) {' || CHR(10) ||
        '            if (n % 2 == 0) {' || CHR(10) ||
        '                total += n;' || CHR(10) ||
        '            }' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return total;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class EvenSumSolver {' || CHR(10) ||
        '    public static int sumarPares(int[] nums) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return 0;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(113,
        'import java.util.HashSet;' || CHR(10) ||
        'import java.util.Set;' || CHR(10) ||
        '' || CHR(10) ||
        'public class DuplicateFinderSolver {' || CHR(10) ||
        '    public static int primerDuplicado(int[] nums) {' || CHR(10) ||
        '        Set<Integer> vistos = new HashSet<>();' || CHR(10) ||
        '        for (int n : nums) {' || CHR(10) ||
        '            if (!vistos.add(n)) {' || CHR(10) ||
        '                return n;' || CHR(10) ||
        '            }' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return -1;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'import java.util.HashSet;' || CHR(10) ||
        'import java.util.Set;' || CHR(10) ||
        '' || CHR(10) ||
        'public class DuplicateFinderSolver {' || CHR(10) ||
        '    public static int primerDuplicado(int[] nums) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return 0;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(104,
        'def emitir_sonido(tipo: str) -> str:' || CHR(10) ||
        '    if tipo.lower() == "perro":' || CHR(10) ||
        '        return "Guau"' || CHR(10) ||
        '    elif tipo.lower() == "gato":' || CHR(10) ||
        '        return "Miau"' || CHR(10) ||
        '    return "Desconocido"',
        'def emitir_sonido(tipo: str) -> str:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return ""' || CHR(10) ||
        ''
    );

    separate_template(203,
        'def nivel_acceso(rol: str) -> str:' || CHR(10) ||
        '    if rol.lower() == "admin":' || CHR(10) ||
        '        return "TOTAL"' || CHR(10) ||
        '    elif rol.lower() == "estudiante":' || CHR(10) ||
        '        return "LECTURA"' || CHR(10) ||
        '    return "DENEGADO"',
        'def nivel_acceso(rol: str) -> str:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return ""' || CHR(10) ||
        ''
    );

    separate_template(205,
        'def search(a: list, target: int) -> int:' || CHR(10) ||
        '    low = 0' || CHR(10) ||
        '    high = len(a) - 1' || CHR(10) ||
        '    while low <= high:' || CHR(10) ||
        '        mid = (low + high) // 2' || CHR(10) ||
        '        if a[mid] == target:' || CHR(10) ||
        '            return mid' || CHR(10) ||
        '        elif a[mid] < target:' || CHR(10) ||
        '            low = mid + 1' || CHR(10) ||
        '        else:' || CHR(10) ||
        '            high = mid - 1' || CHR(10) ||
        '    return -1',
        'def search(a: list, target: int) -> int:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0' || CHR(10) ||
        ''
    );

    separate_template(207,
        'def es_palindromo(texto: str) -> bool:' || CHR(10) ||
        '    limpio = texto.lower()' || CHR(10) ||
        '    return limpio == limpio[::-1]',
        'def es_palindromo(texto: str) -> bool:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return False' || CHR(10) ||
        ''
    );

    separate_template(209,
        'def sumar_pares(nums: list) -> int:' || CHR(10) ||
        '    total = 0' || CHR(10) ||
        '    for n in nums:' || CHR(10) ||
        '        if n % 2 == 0:' || CHR(10) ||
        '            total += n' || CHR(10) ||
        '    return total',
        'def sumar_pares(nums: list) -> int:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0' || CHR(10) ||
        ''
    );

    separate_template(211,
        'def primer_duplicado(nums: list) -> int:' || CHR(10) ||
        '    vistos = set()' || CHR(10) ||
        '    for n in nums:' || CHR(10) ||
        '        if n in vistos:' || CHR(10) ||
        '            return n' || CHR(10) ||
        '        vistos.add(n)' || CHR(10) ||
        '    return -1',
        'def primer_duplicado(nums: list) -> int:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0' || CHR(10) ||
        ''
    );

    separate_template(105,
        '#include <string>' || CHR(10) ||
        '' || CHR(10) ||
        'std::string emitirSonido(std::string tipo) {' || CHR(10) ||
        '    if (tipo == "Perro" || tipo == "PERRO" || tipo == "perro") {' || CHR(10) ||
        '        return "Guau";' || CHR(10) ||
        '    }' || CHR(10) ||
        '    if (tipo == "Gato" || tipo == "GATO" || tipo == "gato") {' || CHR(10) ||
        '        return "Miau";' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return "Desconocido";' || CHR(10) ||
        '}',
        '#include <string>' || CHR(10) ||
        '' || CHR(10) ||
        'std::string emitirSonido(std::string tipo) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return "";' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(303,
        '#include <string>' || CHR(10) ||
        '' || CHR(10) ||
        'std::string nivelAcceso(std::string rol) {' || CHR(10) ||
        '    if (rol == "ADMIN" || rol == "admin") {' || CHR(10) ||
        '        return "TOTAL";' || CHR(10) ||
        '    }' || CHR(10) ||
        '    if (rol == "ESTUDIANTE" || rol == "estudiante") {' || CHR(10) ||
        '        return "LECTURA";' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return "DENEGADO";' || CHR(10) ||
        '}',
        '#include <string>' || CHR(10) ||
        '' || CHR(10) ||
        'std::string nivelAcceso(std::string rol) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return "";' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(305,
        '#include <vector>' || CHR(10) ||
        '' || CHR(10) ||
        'int search(const std::vector<int>& a, int target) {' || CHR(10) ||
        '    int low = 0;' || CHR(10) ||
        '    int high = static_cast<int>(a.size()) - 1;' || CHR(10) ||
        '    while (low <= high) {' || CHR(10) ||
        '        int mid = low + (high - low) / 2;' || CHR(10) ||
        '        if (a[mid] == target) return mid;' || CHR(10) ||
        '        if (a[mid] < target) low = mid + 1;' || CHR(10) ||
        '        else high = mid - 1;' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return -1;' || CHR(10) ||
        '}',
        '#include <vector>' || CHR(10) ||
        '' || CHR(10) ||
        'int search(const std::vector<int>& a, int target) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0;' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(307,
        '#include <string>' || CHR(10) ||
        '#include <cctype>' || CHR(10) ||
        '' || CHR(10) ||
        'bool esPalindromo(std::string texto) {' || CHR(10) ||
        '    int left = 0;' || CHR(10) ||
        '    int right = static_cast<int>(texto.size()) - 1;' || CHR(10) ||
        '    while (left < right) {' || CHR(10) ||
        '        if (std::tolower(texto[left]) != std::tolower(texto[right])) {' || CHR(10) ||
        '            return false;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        left++;' || CHR(10) ||
        '        right--;' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return true;' || CHR(10) ||
        '}',
        '#include <string>' || CHR(10) ||
        '#include <cctype>' || CHR(10) ||
        '' || CHR(10) ||
        'bool esPalindromo(std::string texto) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return false;' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(309,
        '#include <vector>' || CHR(10) ||
        '' || CHR(10) ||
        'int sumarPares(const std::vector<int>& nums) {' || CHR(10) ||
        '    int total = 0;' || CHR(10) ||
        '    for (int n : nums) {' || CHR(10) ||
        '        if (n % 2 == 0) {' || CHR(10) ||
        '            total += n;' || CHR(10) ||
        '        }' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return total;' || CHR(10) ||
        '}',
        '#include <vector>' || CHR(10) ||
        '' || CHR(10) ||
        'int sumarPares(const std::vector<int>& nums) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0;' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(311,
        '#include <vector>' || CHR(10) ||
        '#include <unordered_set>' || CHR(10) ||
        '' || CHR(10) ||
        'int primerDuplicado(const std::vector<int>& nums) {' || CHR(10) ||
        '    std::unordered_set<int> vistos;' || CHR(10) ||
        '    for (int n : nums) {' || CHR(10) ||
        '        if (vistos.count(n) > 0) {' || CHR(10) ||
        '            return n;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        vistos.insert(n);' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return -1;' || CHR(10) ||
        '}',
        '#include <vector>' || CHR(10) ||
        '#include <unordered_set>' || CHR(10) ||
        '' || CHR(10) ||
        'int primerDuplicado(const std::vector<int>& nums) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0;' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );
    DBMS_OUTPUT.PUT_LINE('Retos actualizados con plantilla y referencia separadas: ' || v_updated);
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO starter_migration;
        RAISE;
END;
/

