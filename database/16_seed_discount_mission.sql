-- Apply after 15_professional_missions.sql as the application schema owner.
-- Adds the first mission and its challenge revision in one transaction.
SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
    v_key CONSTANT VARCHAR2(60) := 'descuentos-tienda';
    v_title CONSTANT VARCHAR2(100) := 'El descuento de la tienda está fallando';
    v_client CONSTANT VARCHAR2(120) := 'Tienda Horizonte';
    v_brief CLOB := TO_CLOB('El equipo de ventas detectó que el sistema está restando el número del descuento como si fuera dinero. Corrige el cálculo antes de activar la próxima promoción.');
    v_statement CLOB := TO_CLOB('Corrige finalPrice(int priceCents, int percent). Devuelve el precio final en centavos tras aplicar un porcentaje entero. Redondea al centavo más cercano y, si queda exactamente medio centavo, redondea hacia arriba. Devuelve -1 para un precio negativo o un porcentaje fuera de 0 a 100. Conserva la firma del método.');
    v_starter CLOB :=
        TO_CLOB('public class DiscountSolver {') || CHR(10) ||
        '    public static int finalPrice(int priceCents, int percent) {' || CHR(10) ||
        '        if (priceCents < 0 || percent < 0) {' || CHR(10) ||
        '            return -1;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return priceCents - percent;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        '';
    v_reference CLOB :=
        TO_CLOB('public class DiscountSolver {') || CHR(10) ||
        '    public static int finalPrice(int priceCents, int percent) {' || CHR(10) ||
        '        if (priceCents < 0 || percent < 0 || percent > 100) {' || CHR(10) ||
        '            return -1;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return (int) ((priceCents * (100L - percent) + 50) / 100);' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        '';
    v_guidance CLOB :=
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgcHJlY2lvIGVzdMOhIGVuIGNlbnRhdm9zIHkgZWwgZGVzY3VlbnRvIGVzIHVuIHBvcmNlbnRhamUuIElkZW50aWZpY2EgcXXDqSBwYXJ0ZSBkZWwgcHJlY2lvIGRlYmUgcGFnYXIgZWwgY2xpZW50ZS4=' || CHR(10) ||
        'VmFsaWRhIGFtYm9zIGV4dHJlbW9zIGRlbCBwb3JjZW50YWplIGFudGVzIGRlIGNhbGN1bGFyLiBVc2EgdW5hIG9wZXJhY2nDs24gaW50ZXJtZWRpYSBjb24gbG9uZyBwYXJhIHByZWNpb3MgZ3JhbmRlcy4=' || CHR(10) ||
        'Q2FsY3VsYSBlbCBwcmVjaW8gbXVsdGlwbGljYWRvIHBvciBlbCBwb3JjZW50YWplIHJlc3RhbnRlLiBTaSBoYWNlcyBsYSBkaXZpc2nDs24gZW50ZXJhIGVudHJlIDEwMCwgcGllbnNhIHF1w6kgc3VtYXIgYW50ZXMgcGFyYSByZWRvbmRlYXIgbWVkaW8gY2VudGF2byBoYWNpYSBhcnJpYmEu' || CHR(10) ||
        'RWwgcG9yY2VudGFqZSByZXN0YW50ZSBkZXRlcm1pbmEgZWwgcHJlY2lvIGZpbmFsLiBNdWx0aXBsaWNhciBjb24gbG9uZyBldml0YSBlbCBkZXNib3JkYW1pZW50byB5IHN1bWFyIDUwIGFudGVzIGRlIGRpdmlkaXIgZW50cmUgMTAwIGFwbGljYSBlbCByZWRvbmRlbyBhbCBjZW50YXZvLg==';
    v_lesson NUMBER;
    v_challenge NUMBER;
    v_version NUMBER;
    v_order NUMBER;
    v_exists NUMBER;
    v_case_order NUMBER := 0;

    PROCEDURE add_case(p_requirement NUMBER, p_input VARCHAR2, p_output VARCHAR2, p_hidden NUMBER) IS
        v_case NUMBER;
    BEGIN
        INSERT INTO Casos_Prueba(id_reto,entrada,salida_esperada,es_oculto)
        VALUES(v_challenge,p_input,p_output,p_hidden) RETURNING id_caso INTO v_case;
        INSERT INTO Requisito_Caso_Prueba(codigo_mision,orden_requisito,id_reto,id_caso)
        VALUES(v_key,p_requirement,v_challenge,v_case);
        v_case_order := v_case_order+1;
        INSERT INTO Version_Caso_Prueba(id_version,numero,entrada,salida_esperada,es_oculto)
        VALUES(v_version,v_case_order,p_input,p_output,p_hidden);
    END;
BEGIN
    SAVEPOINT discount_mission;
    -- Lock the same lesson used by challenge authoring before allocating its next order.
    BEGIN
        SELECT l.id_leccion INTO v_lesson FROM Lecciones l
        JOIN Modulos m ON m.id_modulo=l.id_modulo
        JOIN Lenguajes z ON z.id_lenguaje=m.id_lenguaje
        WHERE l.id_leccion=10 AND z.nombre='Java'
        FOR UPDATE OF l.id_leccion;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20092,'La mision requiere la leccion Java original con ID 10.');
    END;
    SELECT COUNT(*) INTO v_exists FROM Mision_Profesional WHERE codigo=v_key;
    IF v_exists=0 THEN
        SELECT NVL(MAX(orden_secuencia),0)+1 INTO v_order FROM Retos WHERE id_leccion=v_lesson;
        INSERT INTO Retos(id_leccion,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
                          orden_secuencia,estado,revision,guia_didactica)
        VALUES(v_lesson,v_title,v_statement,'DEPURACION','MEDIO',80,v_order,'PUBLICADO',1,v_guidance)
        RETURNING id_reto INTO v_challenge;
        INSERT INTO Reto_Codigo(id_reto,codigo_inicial,limite_tiempo_ms)
        VALUES(v_challenge,v_starter,2000);
        INSERT INTO Solucion_Referencia(id_reto,codigo_solucion) VALUES(v_challenge,v_reference);
        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
                                 codigo_inicial,limite_tiempo_ms,solucion_referencia,estado,guia_didactica)
        VALUES(v_challenge,1,v_title,v_statement,'DEPURACION','MEDIO',80,v_starter,2000,v_reference,'PUBLICADO',v_guidance)
        RETURNING id_version INTO v_version;
        INSERT INTO Mision_Profesional(codigo,id_reto,cliente,encargo) VALUES(v_key,v_challenge,v_client,v_brief);
        INSERT INTO Requisito_Mision(codigo_mision,orden,descripcion) VALUES(v_key,1,'Aplica el porcentaje al precio y redondea el resultado al centavo más cercano, con medio centavo hacia arriba.');
        INSERT INTO Requisito_Mision(codigo_mision,orden,descripcion) VALUES(v_key,2,'Un descuento del 0 % conserva el precio y uno del 100 % deja el precio en cero.');
        INSERT INTO Requisito_Mision(codigo_mision,orden,descripcion) VALUES(v_key,3,'Devuelve -1 si el precio es negativo o el porcentaje está fuera de 0 a 100.');
        INSERT INTO Requisito_Mision(codigo_mision,orden,descripcion) VALUES(v_key,4,'Conserva el método finalPrice(int priceCents, int percent) y revisa que el cambio sea fácil de leer.');
        add_case(1,'10000,10','9000',0);
        add_case(1,'1999,25','1499',0);
        add_case(1,'101,50','51',1);
        add_case(1,'2147483647,1','2126008811',1);
        add_case(2,'2500,0','2500',0);
        add_case(2,'2500,100','0',1);
        add_case(3,'-20,10','-1',0);
        add_case(3,'500,-1','-1',1);
        add_case(3,'500,101','-1',1);
        DBMS_OUTPUT.PUT_LINE('Mision instalada: ' || v_key);
    ELSE
        DBMS_OUTPUT.PUT_LINE('La mision ya existe: ' || v_key);
    END IF;
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK TO discount_mission;
    RAISE;
END;
/
