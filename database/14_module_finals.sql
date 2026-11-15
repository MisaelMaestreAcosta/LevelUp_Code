-- Apply after 13_challenge_guidance.sql as the schema owner.
-- Adds one final code-reading assessment to each original module.
-- Re-runnable. Existing final assignments and student attempts are preserved.
SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_tables WHERE table_name='FINAL_MODULO';
    IF v_count=0 THEN
        EXECUTE IMMEDIATE 'CREATE TABLE Final_Modulo ('
            || 'id_modulo NUMBER PRIMARY KEY,id_reto NUMBER NOT NULL UNIQUE,'
            || 'CONSTRAINT fk_final_modulo FOREIGN KEY(id_modulo) REFERENCES Modulos(id_modulo) ON DELETE CASCADE,'
            || 'CONSTRAINT fk_final_reto FOREIGN KEY(id_reto) REFERENCES Retos(id_reto) ON DELETE CASCADE)';
    END IF;
END;
/

DECLARE
    v_created NUMBER := 0;
    PROCEDURE create_final(p_module NUMBER,p_lesson NUMBER,p_lesson_title VARCHAR2,p_title VARCHAR2,
            p_statement CLOB,p_code CLOB,p_options CLOB,p_answer VARCHAR2,p_guidance CLOB) IS
        v_id NUMBER;
        v_module NUMBER;
        v_lesson NUMBER;
        v_count NUMBER;
        v_order NUMBER;
    BEGIN
        BEGIN
            SELECT id_modulo INTO v_module FROM Modulos WHERE id_modulo=p_module FOR UPDATE;
            SELECT id_leccion INTO v_lesson FROM Lecciones
            WHERE id_leccion=p_lesson AND id_modulo=p_module AND titulo=p_lesson_title FOR UPDATE;
        EXCEPTION WHEN NO_DATA_FOUND THEN RETURN;
        END;
        SELECT COUNT(*) INTO v_count FROM Final_Modulo WHERE id_modulo=p_module;
        IF v_count>0 THEN RETURN; END IF;
        SELECT NVL(MAX(orden_secuencia),0)+1 INTO v_order FROM Retos WHERE id_leccion=p_lesson;
        INSERT INTO Retos(id_leccion,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,orden_secuencia,estado,revision,guia_didactica)
        VALUES(p_lesson,p_title,p_statement,'PREDICCION','MEDIO',100,v_order,'PUBLICADO',1,p_guidance)
        RETURNING id_reto INTO v_id;
        INSERT INTO Reto_Quiz(id_reto,opciones,respuesta_correcta) VALUES(v_id,p_options,p_answer);
        INSERT INTO Reto_Prediccion(id_reto,codigo_ejemplo) VALUES(v_id,p_code);
        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            codigo_inicial,opciones,respuesta_correcta,estado,guia_didactica)
        VALUES(v_id,1,p_title,p_statement,'PREDICCION','MEDIO',100,p_code,p_options,p_answer,'PUBLICADO',p_guidance);
        INSERT INTO Final_Modulo(id_modulo,id_reto) VALUES(p_module,v_id);
        v_created := v_created+1;
    END;
BEGIN
    SAVEPOINT module_finals;
    create_final(1, 10, 'Interfaces, Contratos Múltiples y Sustitución de Liskov (SOLID)',
        'Reto final · Servicio de mensajes', TO_CLOB('Un servicio recibe un objeto a través de su contrato y transforma su mensaje. Identifica el resultado. Evalúa solve("5") en la clase mostrada.'),
        TO_CLOB('public class FinalModuleSolver {') || CHR(10) ||
        '    public static String solve(String input) {' || CHR(10) ||
        '        Message message = new Greeting();' || CHR(10) ||
        '        return message.text().toUpperCase();' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        'interface Message { String text(); }' || CHR(10) ||
        'class Greeting implements Message {' || CHR(10) ||
        '    public String text() { return "Hola"; }' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SG9sYQ==' || CHR(10) ||
        'SE9MQQ==' || CHR(10) ||
        'RXJyb3IgZGUgdGlwbw==' || CHR(10) ||
        'TWVuc2FqZQ==', 'HOLA',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBlbCB0aXBvIGNvbmNyZXRvIGRlbCBvYmpldG8gcXVlIHJlY2liZSBsYSBsbGFtYWRhLg==' || CHR(10) ||
        'U2lndWUgZWwgbcOpdG9kbyBxdWUgZ2VuZXJhIGVsIG1lbnNhamUgYW50ZXMgZGUgYXBsaWNhciBsYSB0cmFuc2Zvcm1hY2nDs24u' || CHR(10) ||
        'RGlzdGluZ3VlIGVsIG1lbnNhamUgb3JpZ2luYWwgZGVsIHJlc3VsdGFkbyBkZSBjb252ZXJ0aXJsbyBhIG1hecO6c2N1bGFzLg==' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=');

    create_final(4, 11, 'Técnica de Dos Punteros (Two Pointers) sobre Secuencias',
        'Reto final · Buscador de coincidencias', TO_CLOB('Un buscador debe encontrar la primera aparición de 5 en [2, 5, 5, 9, 12]. Sigue los límites del algoritmo e identifica el índice devuelto. Evalúa solve("5") en la clase mostrada.'),
        TO_CLOB('public class FinalModuleSolver {') || CHR(10) ||
        '    public static String solve(String input) {' || CHR(10) ||
        '        int[] values = {2, 5, 5, 9, 12};' || CHR(10) ||
        '        int target = Integer.parseInt(input);' || CHR(10) ||
        '        int low = 0, high = values.length - 1, found = -1;' || CHR(10) ||
        '        while (low <= high) {' || CHR(10) ||
        '            int mid = low + (high - low) / 2;' || CHR(10) ||
        '            if (values[mid] >= target) {' || CHR(10) ||
        '                if (values[mid] == target) found = mid;' || CHR(10) ||
        '                high = mid - 1;' || CHR(10) ||
        '            } else {' || CHR(10) ||
        '                low = mid + 1;' || CHR(10) ||
        '            }' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return Integer.toString(found);' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'Mg==' || CHR(10) ||
        'NQ==' || CHR(10) ||
        'MQ==' || CHR(10) ||
        'LTE=', '1',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgYXJyZWdsbyBjb250aWVuZSBkb3MgYXBhcmljaW9uZXMgZGVsIG9iamV0aXZvLg==' || CHR(10) ||
        'QW5vdGEgbG93LCBoaWdoLCBtaWQgeSBlbCBjYW5kaWRhdG8gZW5jb250cmFkbyBkZXNwdcOpcyBkZSBjYWRhIGl0ZXJhY2nDs24u' || CHR(10) ||
        'T2JzZXJ2YSBxdWUgZW5jb250cmFyIGVsIG9iamV0aXZvIG11ZXZlIGhpZ2ggaGFjaWEgbGEgaXpxdWllcmRhIHkgY29uc2VydmEgZWwgY2FuZGlkYXRvLg==' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu');

    create_final(5, 12, 'Tablas Hash (HashSet / HashMap) y Detección en O(n)',
        'Reto final · Informe de valores únicos', TO_CLOB('Un informe suma los números pares sin contar valores repetidos. Sigue el conjunto y el acumulador para identificar el total. Evalúa solve("5") en la clase mostrada.'),
        TO_CLOB('import java.util.HashSet;') || CHR(10) ||
        'public class FinalModuleSolver {' || CHR(10) ||
        '    public static String solve(String input) {' || CHR(10) ||
        '        int[] values = {2, 2, 3, 4, 4, 6};' || CHR(10) ||
        '        var seen = new HashSet<Integer>();' || CHR(10) ||
        '        int total = 0;' || CHR(10) ||
        '        for (int value : values) {' || CHR(10) ||
        '            if (seen.add(value) && value % 2 == 0) total += value;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return Integer.toString(total);' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'MTg=' || CHR(10) ||
        'Mw==' || CHR(10) ||
        'MjE=' || CHR(10) ||
        'MTI=', '12',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgY29uanVudG8gcmVjdWVyZGEgbG9zIHZhbG9yZXMgeWEgcHJvY2VzYWRvcy4=' || CHR(10) ||
        'VW4gdmFsb3IgcmVwZXRpZG8gbm8gZGViZSB2b2x2ZXIgYSBlbnRyYXIgZW4gZWwgYWN1bXVsYWRvci4=' || CHR(10) ||
        'U2VwYXJhIGxvcyBwYXJlcyBkaXN0aW50b3MgeSBjYWxjdWxhIHN1IHN1bWEu' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=');

    create_final(2, 13, 'Modelo de Datos de Python y Métodos Especiales (Dunder)',
        'Reto final · Servicio de mensajes', TO_CLOB('Un servicio recibe un objeto a través de su contrato y transforma su mensaje. Identifica el resultado. Selecciona lo que imprime el programa.'),
        TO_CLOB('from abc import ABC, abstractmethod') || CHR(10) ||
        'class Message(ABC):' || CHR(10) ||
        '    @abstractmethod' || CHR(10) ||
        '    def text(self): pass' || CHR(10) ||
        '    def __str__(self): return self.text()' || CHR(10) ||
        'class Greeting(Message):' || CHR(10) ||
        '    def text(self): return "Hola"' || CHR(10) ||
        'message = Greeting()' || CHR(10) ||
        'print(str(message).upper())' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SG9sYQ==' || CHR(10) ||
        'SE9MQQ==' || CHR(10) ||
        'RXJyb3IgZGUgdGlwbw==' || CHR(10) ||
        'TWVuc2FqZQ==', 'HOLA',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBlbCB0aXBvIGNvbmNyZXRvIGRlbCBvYmpldG8gcXVlIHJlY2liZSBsYSBsbGFtYWRhLg==' || CHR(10) ||
        'U2lndWUgZWwgbcOpdG9kbyBxdWUgZ2VuZXJhIGVsIG1lbnNhamUgYW50ZXMgZGUgYXBsaWNhciBsYSB0cmFuc2Zvcm1hY2nDs24u' || CHR(10) ||
        'RGlzdGluZ3VlIGVsIG1lbnNhamUgb3JpZ2luYWwgZGVsIHJlc3VsdGFkbyBkZSBjb252ZXJ0aXJsbyBhIG1hecO6c2N1bGFzLg==' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=');

    create_final(6, 14, 'Slicing Avanzado e Inversión de Secuencias en Python',
        'Reto final · Buscador de coincidencias', TO_CLOB('Un buscador debe encontrar la primera aparición de 5 en [2, 5, 5, 9, 12]. Sigue los límites del algoritmo e identifica el índice devuelto. Selecciona lo que imprime el programa.'),
        TO_CLOB('values = [2, 5, 5, 9, 12]') || CHR(10) ||
        'target = 5' || CHR(10) ||
        'low, high, found = 0, len(values) - 1, -1' || CHR(10) ||
        'while low <= high:' || CHR(10) ||
        '    mid = (low + high) // 2' || CHR(10) ||
        '    if values[mid] >= target:' || CHR(10) ||
        '        if values[mid] == target: found = mid' || CHR(10) ||
        '        high = mid - 1' || CHR(10) ||
        '    else:' || CHR(10) ||
        '        low = mid + 1' || CHR(10) ||
        'print(found)' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'Mg==' || CHR(10) ||
        'NQ==' || CHR(10) ||
        'MQ==' || CHR(10) ||
        'LTE=', '1',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgYXJyZWdsbyBjb250aWVuZSBkb3MgYXBhcmljaW9uZXMgZGVsIG9iamV0aXZvLg==' || CHR(10) ||
        'QW5vdGEgbG93LCBoaWdoLCBtaWQgeSBlbCBjYW5kaWRhdG8gZW5jb250cmFkbyBkZXNwdcOpcyBkZSBjYWRhIGl0ZXJhY2nDs24u' || CHR(10) ||
        'T2JzZXJ2YSBxdWUgZW5jb250cmFyIGVsIG9iamV0aXZvIG11ZXZlIGhpZ2ggaGFjaWEgbGEgaXpxdWllcmRhIHkgY29uc2VydmEgZWwgY2FuZGlkYXRvLg==' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu');

    create_final(7, 15, 'Conjuntos (set) y Diccionarios (dict) en Tiempo O(1)',
        'Reto final · Informe de valores únicos', TO_CLOB('Un informe suma los números pares sin contar valores repetidos. Sigue el conjunto y el acumulador para identificar el total. Selecciona lo que imprime el programa.'),
        TO_CLOB('values = [2, 2, 3, 4, 4, 6]') || CHR(10) ||
        'seen = set()' || CHR(10) ||
        'total = 0' || CHR(10) ||
        'for value in values:' || CHR(10) ||
        '    if value not in seen:' || CHR(10) ||
        '        seen.add(value)' || CHR(10) ||
        '        if value % 2 == 0: total += value' || CHR(10) ||
        'print(total)' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'MTg=' || CHR(10) ||
        'Mw==' || CHR(10) ||
        'MjE=' || CHR(10) ||
        'MTI=', '12',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgY29uanVudG8gcmVjdWVyZGEgbG9zIHZhbG9yZXMgeWEgcHJvY2VzYWRvcy4=' || CHR(10) ||
        'VW4gdmFsb3IgcmVwZXRpZG8gbm8gZGViZSB2b2x2ZXIgYSBlbnRyYXIgZW4gZWwgYWN1bXVsYWRvci4=' || CHR(10) ||
        'U2VwYXJhIGxvcyBwYXJlcyBkaXN0aW50b3MgeSBjYWxjdWxhIHN1IHN1bWEu' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=');

    create_final(3, 16, 'Determinismo RAII y Punteros Inteligentes (std::unique_ptr)',
        'Reto final · Servicio de mensajes', TO_CLOB('Un servicio recibe un objeto a través de su contrato y transforma su mensaje. Identifica el resultado. Selecciona lo que imprime el programa.'),
        TO_CLOB('#include <iostream>') || CHR(10) ||
        '#include <memory>' || CHR(10) ||
        '#include <string>' || CHR(10) ||
        '#include <cctype>' || CHR(10) ||
        'struct Message {' || CHR(10) ||
        '    virtual std::string text() const = 0;' || CHR(10) ||
        '    virtual ~Message() = default;' || CHR(10) ||
        '};' || CHR(10) ||
        'struct Greeting : Message {' || CHR(10) ||
        '    std::string text() const override { return "Hola"; }' || CHR(10) ||
        '};' || CHR(10) ||
        'int main() {' || CHR(10) ||
        '    std::unique_ptr<Message> message = std::make_unique<Greeting>();' || CHR(10) ||
        '    std::string text = message->text();' || CHR(10) ||
        '    for (char& value : text) value = std::toupper(static_cast<unsigned char>(value));' || CHR(10) ||
        '    std::cout << text;' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SG9sYQ==' || CHR(10) ||
        'SE9MQQ==' || CHR(10) ||
        'RXJyb3IgZGUgdGlwbw==' || CHR(10) ||
        'TWVuc2FqZQ==', 'HOLA',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBlbCB0aXBvIGNvbmNyZXRvIGRlbCBvYmpldG8gcXVlIHJlY2liZSBsYSBsbGFtYWRhLg==' || CHR(10) ||
        'U2lndWUgZWwgbcOpdG9kbyBxdWUgZ2VuZXJhIGVsIG1lbnNhamUgYW50ZXMgZGUgYXBsaWNhciBsYSB0cmFuc2Zvcm1hY2nDs24u' || CHR(10) ||
        'RGlzdGluZ3VlIGVsIG1lbnNhamUgb3JpZ2luYWwgZGVsIHJlc3VsdGFkbyBkZSBjb252ZXJ0aXJsbyBhIG1hecO6c2N1bGFzLg==' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=');

    create_final(8, 17, 'Dos Punteros e Iteradores Bidireccionales en std::string',
        'Reto final · Buscador de coincidencias', TO_CLOB('Un buscador debe encontrar la primera aparición de 5 en [2, 5, 5, 9, 12]. Sigue los límites del algoritmo e identifica el índice devuelto. Selecciona lo que imprime el programa.'),
        TO_CLOB('#include <iostream>') || CHR(10) ||
        '#include <vector>' || CHR(10) ||
        'int firstMatch(const std::vector<int>& values, int target) {' || CHR(10) ||
        '    int low = 0, high = static_cast<int>(values.size()) - 1, found = -1;' || CHR(10) ||
        '    while (low <= high) {' || CHR(10) ||
        '        int mid = low + (high - low) / 2;' || CHR(10) ||
        '        if (values[mid] >= target) {' || CHR(10) ||
        '            if (values[mid] == target) found = mid;' || CHR(10) ||
        '            high = mid - 1;' || CHR(10) ||
        '        } else low = mid + 1;' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return found;' || CHR(10) ||
        '}' || CHR(10) ||
        'int main() { std::cout << firstMatch({2, 5, 5, 9, 12}, 5); }' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'Mg==' || CHR(10) ||
        'NQ==' || CHR(10) ||
        'MQ==' || CHR(10) ||
        'LTE=', '1',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgYXJyZWdsbyBjb250aWVuZSBkb3MgYXBhcmljaW9uZXMgZGVsIG9iamV0aXZvLg==' || CHR(10) ||
        'QW5vdGEgbG93LCBoaWdoLCBtaWQgeSBlbCBjYW5kaWRhdG8gZW5jb250cmFkbyBkZXNwdcOpcyBkZSBjYWRhIGl0ZXJhY2nDs24u' || CHR(10) ||
        'T2JzZXJ2YSBxdWUgZW5jb250cmFyIGVsIG9iamV0aXZvIG11ZXZlIGhpZ2ggaGFjaWEgbGEgaXpxdWllcmRhIHkgY29uc2VydmEgZWwgY2FuZGlkYXRvLg==' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu');

    create_final(9, 18, 'Tablas Hash en STL (std::unordered_set) en O(1) Promedio',
        'Reto final · Informe de valores únicos', TO_CLOB('Un informe suma los números pares sin contar valores repetidos. Sigue el conjunto y el acumulador para identificar el total. Selecciona lo que imprime el programa.'),
        TO_CLOB('#include <iostream>') || CHR(10) ||
        '#include <vector>' || CHR(10) ||
        '#include <unordered_set>' || CHR(10) ||
        'int main() {' || CHR(10) ||
        '    std::vector<int> values = {2, 2, 3, 4, 4, 6};' || CHR(10) ||
        '    std::unordered_set<int> seen;' || CHR(10) ||
        '    int total = 0;' || CHR(10) ||
        '    for (int value : values) {' || CHR(10) ||
        '        if (seen.insert(value).second && value % 2 == 0) total += value;' || CHR(10) ||
        '    }' || CHR(10) ||
        '    std::cout << total;' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'MTg=' || CHR(10) ||
        'Mw==' || CHR(10) ||
        'MjE=' || CHR(10) ||
        'MTI=', '12',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgY29uanVudG8gcmVjdWVyZGEgbG9zIHZhbG9yZXMgeWEgcHJvY2VzYWRvcy4=' || CHR(10) ||
        'VW4gdmFsb3IgcmVwZXRpZG8gbm8gZGViZSB2b2x2ZXIgYSBlbnRyYXIgZW4gZWwgYWN1bXVsYWRvci4=' || CHR(10) ||
        'U2VwYXJhIGxvcyBwYXJlcyBkaXN0aW50b3MgeSBjYWxjdWxhIHN1IHN1bWEu' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=');
    DBMS_OUTPUT.PUT_LINE('Retos finales creados: ' || v_created);
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK TO module_finals;
    RAISE;
END;
/
