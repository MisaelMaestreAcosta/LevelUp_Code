-- Apply after 12_seed_starter_templates.sql as the schema owner.
-- Optional teaching content for older installations, versioned with each challenge.
SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_tab_columns WHERE table_name='RETOS' AND column_name='GUIA_DIDACTICA';
    IF v_count=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE Retos ADD guia_didactica CLOB'; END IF;
    SELECT COUNT(*) INTO v_count FROM user_tab_columns WHERE table_name='VERSION_RETO' AND column_name='GUIA_DIDACTICA';
    IF v_count=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE Version_Reto ADD guia_didactica CLOB'; END IF;
END;
/

DECLARE
    v_updated NUMBER := 0;
    PROCEDURE add_guidance(p_id NUMBER,p_title VARCHAR2,p_statement CLOB,p_guidance CLOB) IS
        v_revision NUMBER;
        v_version NUMBER;
    BEGIN
        BEGIN
            SELECT revision INTO v_revision FROM Retos
            WHERE id_reto=p_id AND titulo_reto=p_title AND DBMS_LOB.COMPARE(enunciado,p_statement)=0
                AND revision<=2 AND guia_didactica IS NULL
            FOR UPDATE;
        EXCEPTION WHEN NO_DATA_FOUND THEN RETURN;
        END;

        UPDATE Retos SET guia_didactica=p_guidance,revision=revision+1,fecha_actualizacion=SYSTIMESTAMP WHERE id_reto=p_id;
        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            codigo_inicial,limite_tiempo_ms,opciones,respuesta_correcta,solucion_referencia,estado,guia_didactica)
        SELECT id_reto,v_revision+1,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            codigo_inicial,limite_tiempo_ms,opciones,respuesta_correcta,solucion_referencia,estado,p_guidance
        FROM Version_Reto WHERE id_reto=p_id AND revision=v_revision;
        IF SQL%ROWCOUNT<>1 THEN RAISE_APPLICATION_ERROR(-20091,'Falta la version actual del reto. Revisa 09_content_versions.sql.'); END IF;
        SELECT id_version INTO v_version FROM Version_Reto WHERE id_reto=p_id AND revision=v_revision+1;
        INSERT INTO Version_Caso_Prueba(id_version,numero,entrada,salida_esperada,es_oculto)
        SELECT v_version,c.numero,c.entrada,c.salida_esperada,c.es_oculto
        FROM Version_Caso_Prueba c JOIN Version_Reto v ON v.id_version=c.id_version WHERE v.id_reto=p_id AND v.revision=v_revision;
        v_updated := v_updated+1;
    END;
BEGIN
    SAVEPOINT guidance_seed;
    add_guidance(101, 'Polimorfismo: Despacho Dinámico de Sonidos', 'Implementa el método emitirSonido(String tipo) para que retorne "Guau" cuando reciba "Perro" y "Miau" cuando reciba "Gato" (sin importar mayúsculas o minúsculas).',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHNvbmlkbyBjb3JyZXNwb25kZSBhIGNhZGEgZXNwZWNpZS4=' || CHR(10) ||
        'Q29tcGFyYSBsb3Mgbm9tYnJlcyBpZ25vcmFuZG8gbGFzIGRpZmVyZW5jaWFzIGVudHJlIG1hecO6c2N1bGFzIHkgbWluw7pzY3VsYXMu' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==');
    add_guidance(102, 'Quiz Conceptual: Enlace Dinámico en POO', '¿Qué mecanismo permite que una referencia de tipo superclase invoque la implementación sobrescrita (@Override) de la subclase en tiempo de ejecución?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWwgZGVzcGFjaG8gZGluw6FtaWNvIHNlbGVjY2lvbmEgZW4gZWplY3VjacOzbiBsYSBpbXBsZW1lbnRhY2nDs24gZGVsIG9iamV0byByZWFsLiBMYSBzb2JyZWNhcmdhIHNlIHJlc3VlbHZlIHVzYW5kbyBsYXMgZmlybWFzIGRlIGxvcyBtw6l0b2Rvcy4=' || CHR(10) ||
        'RWwgZGVzcGFjaG8gZGluw6FtaWNvIHNlbGVjY2lvbmEgZW4gZWplY3VjacOzbiBsYSBpbXBsZW1lbnRhY2nDs24gZGVsIG9iamV0byByZWFsLiBMYSBzb2JyZWNhcmdhIHNlIHJlc3VlbHZlIHVzYW5kbyBsYXMgZmlybWFzIGRlIGxvcyBtw6l0b2Rvcy4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtTb2JyZWNhcmdhIGVzdMOhdGljYSBlbiB0aWVtcG8gZGUgY29tcGlsYWNpw7NuIChPdmVybG9hZGluZynCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRWwgZGVzcGFjaG8gZGluw6FtaWNvIHNlbGVjY2lvbmEgZW4gZWplY3VjacOzbiBsYSBpbXBsZW1lbnRhY2nDs24gZGVsIG9iamV0byByZWFsLiBMYSBzb2JyZWNhcmdhIHNlIHJlc3VlbHZlIHVzYW5kbyBsYXMgZmlybWFzIGRlIGxvcyBtw6l0b2Rvcy4=' || CHR(10) ||
        'RWwgZGVzcGFjaG8gZGluw6FtaWNvIHNlbGVjY2lvbmEgZW4gZWplY3VjacOzbiBsYSBpbXBsZW1lbnRhY2nDs24gZGVsIG9iamV0byByZWFsLiBMYSBzb2JyZWNhcmdhIHNlIHJlc3VlbHZlIHVzYW5kbyBsYXMgZmlybWFzIGRlIGxvcyBtw6l0b2Rvcy4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtDb252ZXJzacOzbiBpbXBsw61jaXRhIGRlIHRpcG9zIHByaW1pdGl2b3MgZW4gbGEgcGlsYcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBkZXNwYWNobyBkaW7DoW1pY28gc2VsZWNjaW9uYSBlbiBlamVjdWNpw7NuIGxhIGltcGxlbWVudGFjacOzbiBkZWwgb2JqZXRvIHJlYWwuIExhIHNvYnJlY2FyZ2Egc2UgcmVzdWVsdmUgdXNhbmRvIGxhcyBmaXJtYXMgZGUgbG9zIG3DqXRvZG9zLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtEdXBsaWNhY2nDs24gZGUgY29uc3RydWN0b3JlcyBlbiBsYSBjbGFzZSBiYXNlwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsIGRlc3BhY2hvIGRpbsOhbWljbyBzZWxlY2Npb25hIGVuIGVqZWN1Y2nDs24gbGEgaW1wbGVtZW50YWNpw7NuIGRlbCBvYmpldG8gcmVhbC4gTGEgc29icmVjYXJnYSBzZSByZXN1ZWx2ZSB1c2FuZG8gbGFzIGZpcm1hcyBkZSBsb3MgbcOpdG9kb3Mu');
    add_guidance(106, 'Contratos SOLID: Clasificador de Nivel de Acceso', 'Implementa nivelAcceso(String rol) para que retorne "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE" (ignorando mayúsculas/minúsculas).',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U2VwYXJhIGxvcyByb2xlcyBxdWUgdGllbmVuIGFjY2VzbyBkZSBsb3MgcXVlIGRlYmVuIHNlciByZWNoYXphZG9zLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGVsIHJvbCBhbnRlcyBkZSBjb21wYXJhcmxvIHkgY29uc2VydmEgZWwgdGV4dG8gZGUgc2FsaWRhIGRlbCBlbnVuY2lhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu');
    add_guidance(107, 'Quiz Conceptual: Principio de Sustitución de Liskov', 'Según el Principio de Sustitución de Liskov (LSP) en Java, ¿qué debe cumplirse cuando una clase implementa una interfaz o extiende una clase base?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWwgcHJpbmNpcGlvIGRlIHN1c3RpdHVjacOzbiBkZSBMaXNrb3YgZXhpZ2UgcXVlIGVsIHN1YnRpcG8gcHVlZGEgcmVlbXBsYXphciBhbCBzdXBlcnRpcG8gcmVzcGV0YW5kbyBzdSBjb250cmF0byB5IHNpbiBpbnRyb2R1Y2lyIGVmZWN0b3MgaW5lc3BlcmFkb3MgcGFyYSBxdWllbiBsbyB1c2Eu' || CHR(10) ||
        'RWwgcHJpbmNpcGlvIGRlIHN1c3RpdHVjacOzbiBkZSBMaXNrb3YgZXhpZ2UgcXVlIGVsIHN1YnRpcG8gcHVlZGEgcmVlbXBsYXphciBhbCBzdXBlcnRpcG8gcmVzcGV0YW5kbyBzdSBjb250cmF0byB5IHNpbiBpbnRyb2R1Y2lyIGVmZWN0b3MgaW5lc3BlcmFkb3MgcGFyYSBxdWllbiBsbyB1c2Eu' || CHR(10) ||
        'RWwgcHJpbmNpcGlvIGRlIHN1c3RpdHVjacOzbiBkZSBMaXNrb3YgZXhpZ2UgcXVlIGVsIHN1YnRpcG8gcHVlZGEgcmVlbXBsYXphciBhbCBzdXBlcnRpcG8gcmVzcGV0YW5kbyBzdSBjb250cmF0byB5IHNpbiBpbnRyb2R1Y2lyIGVmZWN0b3MgaW5lc3BlcmFkb3MgcGFyYSBxdWllbiBsbyB1c2Eu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtMYSBzdWJjbGFzZSBkZWJlIGRlY2xhcmFyIHRvZG9zIHN1cyBhdHJpYnV0b3MgY29tbyBww7pibGljb3PCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRWwgcHJpbmNpcGlvIGRlIHN1c3RpdHVjacOzbiBkZSBMaXNrb3YgZXhpZ2UgcXVlIGVsIHN1YnRpcG8gcHVlZGEgcmVlbXBsYXphciBhbCBzdXBlcnRpcG8gcmVzcGV0YW5kbyBzdSBjb250cmF0byB5IHNpbiBpbnRyb2R1Y2lyIGVmZWN0b3MgaW5lc3BlcmFkb3MgcGFyYSBxdWllbiBsbyB1c2Eu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtVbmEgaW50ZXJmYXogc29sbyBwdWVkZSBzZXIgaW1wbGVtZW50YWRhIHBvciB1bmEgw7puaWNhIGNsYXNlIGVuIHRvZG8gZWwgcGFxdWV0ZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBwcmluY2lwaW8gZGUgc3VzdGl0dWNpw7NuIGRlIExpc2tvdiBleGlnZSBxdWUgZWwgc3VidGlwbyBwdWVkYSByZWVtcGxhemFyIGFsIHN1cGVydGlwbyByZXNwZXRhbmRvIHN1IGNvbnRyYXRvIHkgc2luIGludHJvZHVjaXIgZWZlY3RvcyBpbmVzcGVyYWRvcyBwYXJhIHF1aWVuIGxvIHVzYS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBjb21waWxhZG9yIHByb2jDrWJlIHVzYXIgcG9saW1vcmZpc21vIGNvbiBpbnRlcmZhY2VzIGZ1bmNpb25hbGVzwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsIHByaW5jaXBpbyBkZSBzdXN0aXR1Y2nDs24gZGUgTGlza292IGV4aWdlIHF1ZSBlbCBzdWJ0aXBvIHB1ZWRhIHJlZW1wbGF6YXIgYWwgc3VwZXJ0aXBvIHJlc3BldGFuZG8gc3UgY29udHJhdG8geSBzaW4gaW50cm9kdWNpciBlZmVjdG9zIGluZXNwZXJhZG9zIHBhcmEgcXVpZW4gbG8gdXNhLg==');
    add_guidance(103, 'Búsqueda Binaria Segura O(log n)', 'Implementa search(int[] a, int target) retornando el índice (0-based) del elemento buscado o -1 si no se encuentra en el arreglo ordenado.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TGEgc2VjdWVuY2lhIGVzdMOhIG9yZGVuYWRhLiBVc2EgZXNlIG9yZGVuIHBhcmEgZGVzY2FydGFyIHVuYSBwYXJ0ZSBkZSBsYSBiw7pzcXVlZGEu' || CHR(10) ||
        'TWFudMOpbiBsb3MgbMOtbWl0ZXMgZGVsIGludGVydmFsbyB5IGNhbGN1bGEgc3UgcHVudG8gbWVkaW8uIEVuIFB5dGhvbiBuZWNlc2l0YXMgZGl2aXNpw7NuIGVudGVyYS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=');
    add_guidance(108, 'Quiz Conceptual: Prevención de Overflow en Búsqueda Binaria', '¿Por qué se prefiere la expresión mid = low + (high - low) / 2 en lugar de (low + high) / 2 en arreglos grandes en Java?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'bG93ICsgKGhpZ2ggLSBsb3cpIC8gMiBldml0YSBzdW1hciBkaXJlY3RhbWVudGUgZG9zIMOtbmRpY2VzIGdyYW5kZXMgcXVlIHBvZHLDrWFuIGRlc2JvcmRhciB1biBpbnQu' || CHR(10) ||
        'bG93ICsgKGhpZ2ggLSBsb3cpIC8gMiBldml0YSBzdW1hciBkaXJlY3RhbWVudGUgZG9zIMOtbmRpY2VzIGdyYW5kZXMgcXVlIHBvZHLDrWFuIGRlc2JvcmRhciB1biBpbnQu' || CHR(10) ||
        'bG93ICsgKGhpZ2ggLSBsb3cpIC8gMiBldml0YSBzdW1hciBkaXJlY3RhbWVudGUgZG9zIMOtbmRpY2VzIGdyYW5kZXMgcXVlIHBvZHLDrWFuIGRlc2JvcmRhciB1biBpbnQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgZWwgb3BlcmFkb3IgKyBlc3TDoSBkZXNhY29uc2VqYWRvIGVuIHZlcnNpb25lcyByZWNpZW50ZXMgZGUgSmF2YcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBsb3cgKyAoaGlnaCAtIGxvdykgLyAyIGV2aXRhIHN1bWFyIGRpcmVjdGFtZW50ZSBkb3Mgw61uZGljZXMgZ3JhbmRlcyBxdWUgcG9kcsOtYW4gZGVzYm9yZGFyIHVuIGludC4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgY29udmllcnRlIGF1dG9tw6F0aWNhbWVudGUgZWwgYXJyZWdsbyBhIG7Dum1lcm9zIGRlIHB1bnRvIGZsb3RhbnRlwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIGxvdyArIChoaWdoIC0gbG93KSAvIDIgZXZpdGEgc3VtYXIgZGlyZWN0YW1lbnRlIGRvcyDDrW5kaWNlcyBncmFuZGVzIHF1ZSBwb2Ryw61hbiBkZXNib3JkYXIgdW4gaW50Lg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgcmVkdWNlIGxhIGNvbXBsZWppZGFkIGRlIE8obG9nIG4pIGEgTygxKSBjb25zdGFudGXCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gbG93ICsgKGhpZ2ggLSBsb3cpIC8gMiBldml0YSBzdW1hciBkaXJlY3RhbWVudGUgZG9zIMOtbmRpY2VzIGdyYW5kZXMgcXVlIHBvZHLDrWFuIGRlc2JvcmRhciB1biBpbnQu');
    add_guidance(109, 'Dos Punteros: Verificador de Palíndromos', 'Implementa esPalindromo(String texto) usando dos índices para retornar true si la palabra se lee igual de izquierda a derecha que de derecha a izquierda (ignorando mayúsculas), o false en caso contrario.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'VW4gcGFsw61uZHJvbW8gY29uc2VydmEgc3VzIGNhcmFjdGVyZXMgYWwgbGVlcmxvIGVuIHNlbnRpZG8gY29udHJhcmlvLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGxhcyBtYXnDunNjdWxhcyB5IGNvbXBhcmEgbG9zIGRvcyBleHRyZW1vcyBkZSBsYSBzZWN1ZW5jaWEu' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u');
    add_guidance(110, 'Quiz Conceptual: Complejidad de Dos Punteros', '¿Cuál es la complejidad temporal y espacial auxiliar de verificar un palíndromo in-place usando dos índices extremos (left y right)?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'TG9zIGRvcyBwdW50ZXJvcyByZWNvcnJlbiBjb21vIG3DoXhpbW8gbGEgbG9uZ2l0dWQgZGUgbGEgY2FkZW5hIHkgY29uc2VydmFuIHNvbG8gdW5vcyBwb2NvcyDDrW5kaWNlcywgcG9yIGVzbyBlbCB0aWVtcG8gZXMgTyhuKSB5IGVsIGVzcGFjaW8gYWRpY2lvbmFsIE8oMSku' || CHR(10) ||
        'TG9zIGRvcyBwdW50ZXJvcyByZWNvcnJlbiBjb21vIG3DoXhpbW8gbGEgbG9uZ2l0dWQgZGUgbGEgY2FkZW5hIHkgY29uc2VydmFuIHNvbG8gdW5vcyBwb2NvcyDDrW5kaWNlcywgcG9yIGVzbyBlbCB0aWVtcG8gZXMgTyhuKSB5IGVsIGVzcGFjaW8gYWRpY2lvbmFsIE8oMSku' || CHR(10) ||
        'TG9zIGRvcyBwdW50ZXJvcyByZWNvcnJlbiBjb21vIG3DoXhpbW8gbGEgbG9uZ2l0dWQgZGUgbGEgY2FkZW5hIHkgY29uc2VydmFuIHNvbG8gdW5vcyBwb2NvcyDDrW5kaWNlcywgcG9yIGVzbyBlbCB0aWVtcG8gZXMgTyhuKSB5IGVsIGVzcGFjaW8gYWRpY2lvbmFsIE8oMSku' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtUaWVtcG8gTyhuwrIpIHkgZXNwYWNpbyBhdXhpbGlhciBPKG4pIHBvciBjYWRhIGNhcsOhY3RlciBldmFsdWFkb8K7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBMb3MgZG9zIHB1bnRlcm9zIHJlY29ycmVuIGNvbW8gbcOheGltbyBsYSBsb25naXR1ZCBkZSBsYSBjYWRlbmEgeSBjb25zZXJ2YW4gc29sbyB1bm9zIHBvY29zIMOtbmRpY2VzLCBwb3IgZXNvIGVsIHRpZW1wbyBlcyBPKG4pIHkgZWwgZXNwYWNpbyBhZGljaW9uYWwgTygxKS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtUaWVtcG8gTyhsb2cgbikgcG9ycXVlIGRlc2NhcnRhIGxhIG1pdGFkIGRlbCB0ZXh0byBlbiBjYWRhIHBhc2/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gTG9zIGRvcyBwdW50ZXJvcyByZWNvcnJlbiBjb21vIG3DoXhpbW8gbGEgbG9uZ2l0dWQgZGUgbGEgY2FkZW5hIHkgY29uc2VydmFuIHNvbG8gdW5vcyBwb2NvcyDDrW5kaWNlcywgcG9yIGVzbyBlbCB0aWVtcG8gZXMgTyhuKSB5IGVsIGVzcGFjaW8gYWRpY2lvbmFsIE8oMSku' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtUaWVtcG8gTygxKSBpbmRlcGVuZGllbnRlIGRlIGxhIGxvbmdpdHVkIGRlbCB0ZXh0b8K7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBMb3MgZG9zIHB1bnRlcm9zIHJlY29ycmVuIGNvbW8gbcOheGltbyBsYSBsb25naXR1ZCBkZSBsYSBjYWRlbmEgeSBjb25zZXJ2YW4gc29sbyB1bm9zIHBvY29zIMOtbmRpY2VzLCBwb3IgZXNvIGVsIHRpZW1wbyBlcyBPKG4pIHkgZWwgZXNwYWNpbyBhZGljaW9uYWwgTygxKS4=');
    add_guidance(111, 'Colecciones: Suma de Números Pares', 'Implementa sumarPares(int[] nums) para recorrer la secuencia y retornar la suma de todos los números pares contenidos en el arreglo.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U29sbyBkZWJlbiBjb250cmlidWlyIGFsIHJlc3VsdGFkbyBsb3MgbsO6bWVyb3MgcXVlIGN1bXBsZW4gbGEgY29uZGljacOzbiBkZSBwYXJpZGFkLg==' || CHR(10) ||
        'UHJlcGFyYSB1biBhY3VtdWxhZG9yIHkgcmVjb3JyZSBjYWRhIGVsZW1lbnRvIGRlIGxhIGNvbGVjY2nDs24u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu');
    add_guidance(112, 'Quiz Conceptual: Complejidad Amortizada en ArrayList', '¿Cuál es el costo temporal de insertar un elemento al final de un ArrayList<T> mediante add(elemento) en análisis amortizado?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'QXJyYXlMaXN0IHVzYSB1biBhcnJlZ2xvIHF1ZSBwdWVkZSBjcmVjZXIuIEVsIGFjY2VzbyBwb3Igw61uZGljZSBlcyBPKDEpIHkgYcOxYWRpciBhbCBmaW5hbCB0aWVuZSBjb3N0ZSBhbW9ydGl6YWRvIE8oMSku' || CHR(10) ||
        'QXJyYXlMaXN0IHVzYSB1biBhcnJlZ2xvIHF1ZSBwdWVkZSBjcmVjZXIuIEVsIGFjY2VzbyBwb3Igw61uZGljZSBlcyBPKDEpIHkgYcOxYWRpciBhbCBmaW5hbCB0aWVuZSBjb3N0ZSBhbW9ydGl6YWRvIE8oMSku' || CHR(10) ||
        'QXJyYXlMaXN0IHVzYSB1biBhcnJlZ2xvIHF1ZSBwdWVkZSBjcmVjZXIuIEVsIGFjY2VzbyBwb3Igw61uZGljZSBlcyBPKDEpIHkgYcOxYWRpciBhbCBmaW5hbCB0aWVuZSBjb3N0ZSBhbW9ydGl6YWRvIE8oMSku' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPKG7CsikgcG9ycXVlIG9yZGVuYSBhdXRvbcOhdGljYW1lbnRlIHRvZG9zIGxvcyBlbGVtZW50b3MgZW4gY2FkYSBpbnNlcmNpw7Nuwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEFycmF5TGlzdCB1c2EgdW4gYXJyZWdsbyBxdWUgcHVlZGUgY3JlY2VyLiBFbCBhY2Nlc28gcG9yIMOtbmRpY2UgZXMgTygxKSB5IGHDsWFkaXIgYWwgZmluYWwgdGllbmUgY29zdGUgYW1vcnRpemFkbyBPKDEpLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPKGxvZyBuKSBwb3JxdWUgaW50ZXJuYW1lbnRlIGVzIHVuIMOhcmJvbCBiaW5hcmlvIGJhbGFuY2VhZG/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gQXJyYXlMaXN0IHVzYSB1biBhcnJlZ2xvIHF1ZSBwdWVkZSBjcmVjZXIuIEVsIGFjY2VzbyBwb3Igw61uZGljZSBlcyBPKDEpIHkgYcOxYWRpciBhbCBmaW5hbCB0aWVuZSBjb3N0ZSBhbW9ydGl6YWRvIE8oMSku' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPKG4pIGVzdHJpY3RvIHBvcnF1ZSBjb3BpYSBlbCBhcnJlZ2xvIGNvbXBsZXRvIGVuIGNhZGEgbGxhbWFkYSBhIGFkZCgpwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEFycmF5TGlzdCB1c2EgdW4gYXJyZWdsbyBxdWUgcHVlZGUgY3JlY2VyLiBFbCBhY2Nlc28gcG9yIMOtbmRpY2UgZXMgTygxKSB5IGHDsWFkaXIgYWwgZmluYWwgdGllbmUgY29zdGUgYW1vcnRpemFkbyBPKDEpLg==');
    add_guidance(113, 'Tablas Hash: Primer Elemento Duplicado en O(n)', 'Implementa primerDuplicado(int[] nums) usando un HashSet para retornar el primer número que aparece repetido al recorrer el arreglo, o -1 si todos son únicos.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TmVjZXNpdGFzIHJlY29yZGFyIGxvcyB2YWxvcmVzIHF1ZSB5YSBhcGFyZWNpZXJvbiBkdXJhbnRlIGVsIHJlY29ycmlkby4=' || CHR(10) ||
        'VW4gY29uanVudG8gcGVybWl0ZSBjb21wcm9iYXIgc2kgdW4gZWxlbWVudG8geWEgc2UgaGEgdmlzaXRhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u');
    add_guidance(114, 'Quiz Conceptual: Contrato equals() y hashCode()', '¿Qué ocurre si una clase sobrescribe equals() pero NO sobrescribe hashCode() al almacenarse dentro de un HashSet o HashMap en Java?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'RG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'RG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBjw7NkaWdvIG5vIGNvbXBpbGEgcG9ycXVlIE9iamVjdCBubyBwcm92ZWUgdW5hIGltcGxlbWVudGFjacOzbiBiYXNlIGRlIGhhc2hDb2RlKCnCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBIYXNoU2V0IGNvbnZpZXJ0ZSBhdXRvbcOhdGljYW1lbnRlIHRvZG9zIGxvcyBvYmpldG9zIGEgU3RyaW5nIGFudGVzIGRlIGd1YXJkYXJsb3PCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtMYSBKVk0gbGFuemEgTnVsbFBvaW50ZXJFeGNlcHRpb24gYWwgaW52b2NhciBhZGQoKcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBEb3Mgb2JqZXRvcyBpZ3VhbGVzIHNlZ8O6biBlcXVhbHMgZGViZW4gcHJvZHVjaXIgZWwgbWlzbW8gaGFzaENvZGUuIFVuIGhhc2ggY29pbmNpZGVudGUgcG9yIHPDrSBzb2xvIG5vIGdhcmFudGl6YSBpZ3VhbGRhZC4=');
    add_guidance(104, 'Polimorfismo en Python: Sonidos por Especie', 'Completa la función emitir_sonido(tipo: str) -> str para retornar "Guau" con "Perro" y "Miau" con "Gato" (ignorando mayúsculas/minúsculas).',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHNvbmlkbyBjb3JyZXNwb25kZSBhIGNhZGEgZXNwZWNpZS4=' || CHR(10) ||
        'Q29tcGFyYSBsb3Mgbm9tYnJlcyBpZ25vcmFuZG8gbGFzIGRpZmVyZW5jaWFzIGVudHJlIG1hecO6c2N1bGFzIHkgbWluw7pzY3VsYXMu' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==');
    add_guidance(202, 'Quiz Conceptual: Clases Abstractas abc.ABC en Python', '¿Qué sucede en Python cuando intentas instanciar una subclase de abc.ABC que no ha implementado todos los métodos decorados con @abstractmethod?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'YWJjLkFCQyBpbXBpZGUgaW5zdGFuY2lhciB1bmEgc3ViY2xhc2UgcXVlIGNvbnNlcnZhIG3DqXRvZG9zIGFic3RyYWN0b3Mgc2luIGltcGxlbWVudGFyIHkgUHl0aG9uIGxhbnphIFR5cGVFcnJvci4=' || CHR(10) ||
        'YWJjLkFCQyBpbXBpZGUgaW5zdGFuY2lhciB1bmEgc3ViY2xhc2UgcXVlIGNvbnNlcnZhIG3DqXRvZG9zIGFic3RyYWN0b3Mgc2luIGltcGxlbWVudGFyIHkgUHl0aG9uIGxhbnphIFR5cGVFcnJvci4=' || CHR(10) ||
        'YWJjLkFCQyBpbXBpZGUgaW5zdGFuY2lhciB1bmEgc3ViY2xhc2UgcXVlIGNvbnNlcnZhIG3DqXRvZG9zIGFic3RyYWN0b3Mgc2luIGltcGxlbWVudGFyIHkgUHl0aG9uIGxhbnphIFR5cGVFcnJvci4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQeXRob24gZ2VuZXJhIGF1dG9tw6F0aWNhbWVudGUgbcOpdG9kb3MgdmFjw61vcyBxdWUgcmV0b3JuYW4gTm9uZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBhYmMuQUJDIGltcGlkZSBpbnN0YW5jaWFyIHVuYSBzdWJjbGFzZSBxdWUgY29uc2VydmEgbcOpdG9kb3MgYWJzdHJhY3RvcyBzaW4gaW1wbGVtZW50YXIgeSBQeXRob24gbGFuemEgVHlwZUVycm9yLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBwcm9ncmFtYSBzZSBlamVjdXRhIG5vcm1hbG1lbnRlIGUgaWdub3JhIGVsIGRlY29yYWRvciBAYWJzdHJhY3RtZXRob2TCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gYWJjLkFCQyBpbXBpZGUgaW5zdGFuY2lhciB1bmEgc3ViY2xhc2UgcXVlIGNvbnNlcnZhIG3DqXRvZG9zIGFic3RyYWN0b3Mgc2luIGltcGxlbWVudGFyIHkgUHl0aG9uIGxhbnphIFR5cGVFcnJvci4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtTb2xvIG11ZXN0cmEgdW5hIGFkdmVydGVuY2lhIGVzdMOpdGljYSBlbiBsYSBjb25zb2xhIHNpbiBkZXRlbmVyIGVsIHByb2dyYW1hwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIGFiYy5BQkMgaW1waWRlIGluc3RhbmNpYXIgdW5hIHN1YmNsYXNlIHF1ZSBjb25zZXJ2YSBtw6l0b2RvcyBhYnN0cmFjdG9zIHNpbiBpbXBsZW1lbnRhciB5IFB5dGhvbiBsYW56YSBUeXBlRXJyb3Iu');
    add_guidance(203, 'Protocolos en Python: Clasificador de Nivel de Acceso', 'Completa nivel_acceso(rol: str) -> str para retornar "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE".',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U2VwYXJhIGxvcyByb2xlcyBxdWUgdGllbmVuIGFjY2VzbyBkZSBsb3MgcXVlIGRlYmVuIHNlciByZWNoYXphZG9zLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGVsIHJvbCBhbnRlcyBkZSBjb21wYXJhcmxvIHkgY29uc2VydmEgZWwgdGV4dG8gZGUgc2FsaWRhIGRlbCBlbnVuY2lhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu');
    add_guidance(204, 'Quiz Conceptual: Métodos Especiales __str__ vs __repr__', '¿Cuál es la diferencia fundamental entre los métodos especiales __str__ y __repr__ en el modelo de datos de Python?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'X19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'X19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'X19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtfX3N0cl9fIHNvbG8gZnVuY2lvbmEgY29uIG7Dum1lcm9zIGVudGVyb3MgeSBfX3JlcHJfXyBzb2xvIGNvbiBsaXN0YXPCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gX19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtBbWJvcyBtw6l0b2RvcyBzb24gaWTDqW50aWNvcyB5IG5vIHB1ZWRlbiBjb2V4aXN0aXIgZW4gbGEgbWlzbWEgY2xhc2XCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gX19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtfX3JlcHJfXyBlcyBwcml2YWRvIHkgbm8gcHVlZGUgc2VyIGludm9jYWRvIGRlc2RlIGZ1ZXJhIGRlbCBtw7NkdWxvwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIF9fc3RyX18gYnVzY2EgdW5hIHJlcHJlc2VudGFjacOzbiBsZWdpYmxlIHBhcmEgZWwgdXN1YXJpbyB5IF9fcmVwcl9fIHVuYSByZXByZXNlbnRhY2nDs24gdMOpY25pY2EgZGVsIG9iamV0by4=');
    add_guidance(205, 'Búsqueda Binaria en Python con División Entera (//)', 'Completa search(a: list, target: int) -> int para retornar el índice del elemento buscado en la lista ordenada o -1 si no existe.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TGEgc2VjdWVuY2lhIGVzdMOhIG9yZGVuYWRhLiBVc2EgZXNlIG9yZGVuIHBhcmEgZGVzY2FydGFyIHVuYSBwYXJ0ZSBkZSBsYSBiw7pzcXVlZGEu' || CHR(10) ||
        'TWFudMOpbiBsb3MgbMOtbWl0ZXMgZGVsIGludGVydmFsbyB5IGNhbGN1bGEgc3UgcHVudG8gbWVkaW8uIEVuIFB5dGhvbiBuZWNlc2l0YXMgZGl2aXNpw7NuIGVudGVyYS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=');
    add_guidance(206, 'Quiz Conceptual: Operador // e Indexación en Python', '¿Por qué es indispensable utilizar el operador // en lugar de / al calcular el índice medio en Python 3?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWwgb3BlcmFkb3IgLyBwcm9kdWNlIHVuIHJlc3VsdGFkbyBkZSBwdW50byBmbG90YW50ZS4gTG9zIMOtbmRpY2VzIGRlIHVuYSBsaXN0YSByZXF1aWVyZW4gZW50ZXJvcywgcG9yIGVzbyBzZSB1dGlsaXphIC8vLg==' || CHR(10) ||
        'RWwgb3BlcmFkb3IgLyBwcm9kdWNlIHVuIHJlc3VsdGFkbyBkZSBwdW50byBmbG90YW50ZS4gTG9zIMOtbmRpY2VzIGRlIHVuYSBsaXN0YSByZXF1aWVyZW4gZW50ZXJvcywgcG9yIGVzbyBzZSB1dGlsaXphIC8vLg==' || CHR(10) ||
        'RWwgb3BlcmFkb3IgLyBwcm9kdWNlIHVuIHJlc3VsdGFkbyBkZSBwdW50byBmbG90YW50ZS4gTG9zIMOtbmRpY2VzIGRlIHVuYSBsaXN0YSByZXF1aWVyZW4gZW50ZXJvcywgcG9yIGVzbyBzZSB1dGlsaXphIC8vLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgLy8gcmVhbGl6YSBsYSBkaXZpc2nDs24gZW4gdW4gaGlsbyBzZXBhcmFkbyBkZWwgcHJvY2VzYWRvcsK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBvcGVyYWRvciAvIHByb2R1Y2UgdW4gcmVzdWx0YWRvIGRlIHB1bnRvIGZsb3RhbnRlLiBMb3Mgw61uZGljZXMgZGUgdW5hIGxpc3RhIHJlcXVpZXJlbiBlbnRlcm9zLCBwb3IgZXNvIHNlIHV0aWxpemEgLy8u' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgZWwgb3BlcmFkb3IgLyBsYW56YSBaZXJvRGl2aXNpb25FcnJvciBjdWFuZG8gZWwgcmVzdWx0YWRvIGVzIGRlY2ltYWzCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRWwgb3BlcmFkb3IgLyBwcm9kdWNlIHVuIHJlc3VsdGFkbyBkZSBwdW50byBmbG90YW50ZS4gTG9zIMOtbmRpY2VzIGRlIHVuYSBsaXN0YSByZXF1aWVyZW4gZW50ZXJvcywgcG9yIGVzbyBzZSB1dGlsaXphIC8vLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgLy8gb3JkZW5hIGF1dG9tw6F0aWNhbWVudGUgbGEgbGlzdGEgYW50ZXMgZGUgZGl2aWRpcsK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBvcGVyYWRvciAvIHByb2R1Y2UgdW4gcmVzdWx0YWRvIGRlIHB1bnRvIGZsb3RhbnRlLiBMb3Mgw61uZGljZXMgZGUgdW5hIGxpc3RhIHJlcXVpZXJlbiBlbnRlcm9zLCBwb3IgZXNvIHNlIHV0aWxpemEgLy8u');
    add_guidance(207, 'Slicing en Python: Detector de Palíndromos', 'Completa es_palindromo(texto: str) -> bool invirtiendo o comparando la cadena en minúsculas para evaluar si es un palíndromo.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'VW4gcGFsw61uZHJvbW8gY29uc2VydmEgc3VzIGNhcmFjdGVyZXMgYWwgbGVlcmxvIGVuIHNlbnRpZG8gY29udHJhcmlvLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGxhcyBtYXnDunNjdWxhcyB5IGNvbXBhcmEgbG9zIGRvcyBleHRyZW1vcyBkZSBsYSBzZWN1ZW5jaWEu' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u');
    add_guidance(208, 'Quiz Conceptual: Slicing s[::-1] en Memoria', '¿Qué hace exactamente la expresión texto[::-1] sobre una cadena str en Python?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWwgc2xpY2luZyBjb24gcGFzbyAtMSBjcmVhIHVuYSBzZWN1ZW5jaWEgaW52ZXJ0aWRhLiBFc2EgbnVldmEgc2VjdWVuY2lhIG9jdXBhIGVzcGFjaW8gYWRpY2lvbmFsIHByb3BvcmNpb25hbCBhIHN1IGxvbmdpdHVkLg==' || CHR(10) ||
        'RWwgc2xpY2luZyBjb24gcGFzbyAtMSBjcmVhIHVuYSBzZWN1ZW5jaWEgaW52ZXJ0aWRhLiBFc2EgbnVldmEgc2VjdWVuY2lhIG9jdXBhIGVzcGFjaW8gYWRpY2lvbmFsIHByb3BvcmNpb25hbCBhIHN1IGxvbmdpdHVkLg==' || CHR(10) ||
        'RWwgc2xpY2luZyBjb24gcGFzbyAtMSBjcmVhIHVuYSBzZWN1ZW5jaWEgaW52ZXJ0aWRhLiBFc2EgbnVldmEgc2VjdWVuY2lhIG9jdXBhIGVzcGFjaW8gYWRpY2lvbmFsIHByb3BvcmNpb25hbCBhIHN1IGxvbmdpdHVkLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbGltaW5hIGVsIMO6bHRpbW8gY2Fyw6FjdGVyIGRlIGxhIGNhZGVuYSBtb2RpZmljw6FuZG9sYSBpbi1wbGFjZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBzbGljaW5nIGNvbiBwYXNvIC0xIGNyZWEgdW5hIHNlY3VlbmNpYSBpbnZlcnRpZGEuIEVzYSBudWV2YSBzZWN1ZW5jaWEgb2N1cGEgZXNwYWNpbyBhZGljaW9uYWwgcHJvcG9yY2lvbmFsIGEgc3UgbG9uZ2l0dWQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtDb252aWVydGUgbGEgY2FkZW5hIGVuIHVuIGRpY2Npb25hcmlvIGRlIGZyZWN1ZW5jaWFzwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsIHNsaWNpbmcgY29uIHBhc28gLTEgY3JlYSB1bmEgc2VjdWVuY2lhIGludmVydGlkYS4gRXNhIG51ZXZhIHNlY3VlbmNpYSBvY3VwYSBlc3BhY2lvIGFkaWNpb25hbCBwcm9wb3JjaW9uYWwgYSBzdSBsb25naXR1ZC4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtMYW56YSBJbmRleEVycm9yIHBvcnF1ZSBsb3MgcGFzb3MgbmVnYXRpdm9zIG5vIGV4aXN0ZW4gZW4gUHl0aG9uwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsIHNsaWNpbmcgY29uIHBhc28gLTEgY3JlYSB1bmEgc2VjdWVuY2lhIGludmVydGlkYS4gRXNhIG51ZXZhIHNlY3VlbmNpYSBvY3VwYSBlc3BhY2lvIGFkaWNpb25hbCBwcm9wb3JjaW9uYWwgYSBzdSBsb25naXR1ZC4=');
    add_guidance(209, 'Listas en Python: Suma de Elementos Pares', 'Completa sumar_pares(nums: list) -> int para retornar la suma acumulada de los números divisibles por 2.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U29sbyBkZWJlbiBjb250cmlidWlyIGFsIHJlc3VsdGFkbyBsb3MgbsO6bWVyb3MgcXVlIGN1bXBsZW4gbGEgY29uZGljacOzbiBkZSBwYXJpZGFkLg==' || CHR(10) ||
        'UHJlcGFyYSB1biBhY3VtdWxhZG9yIHkgcmVjb3JyZSBjYWRhIGVsZW1lbnRvIGRlIGxhIGNvbGVjY2nDs24u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu');
    add_guidance(210, 'Quiz Conceptual: Crecimiento de list en CPython', '¿Cómo logra la estructura list de CPython que las llamadas sucesivas a append() tengan costo O(1) amortizado?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'Q1B5dGhvbiByZXNlcnZhIGNhcGFjaWRhZCBhZGljaW9uYWwgYWwgaGFjZXIgY3JlY2VyIHVuYSBsaXN0YS4gQXPDrSBkaXN0cmlidXllIGVsIGNvc3RlIGRlIGxhcyByZWFsb2NhY2lvbmVzIGVudHJlIG11Y2hhcyBsbGFtYWRhcyBhIGFwcGVuZCB5IGNvbnNpZ3VlIE8oMSkgYW1vcnRpemFkby4=' || CHR(10) ||
        'Q1B5dGhvbiByZXNlcnZhIGNhcGFjaWRhZCBhZGljaW9uYWwgYWwgaGFjZXIgY3JlY2VyIHVuYSBsaXN0YS4gQXPDrSBkaXN0cmlidXllIGVsIGNvc3RlIGRlIGxhcyByZWFsb2NhY2lvbmVzIGVudHJlIG11Y2hhcyBsbGFtYWRhcyBhIGFwcGVuZCB5IGNvbnNpZ3VlIE8oMSkgYW1vcnRpemFkby4=' || CHR(10) ||
        'Q1B5dGhvbiByZXNlcnZhIGNhcGFjaWRhZCBhZGljaW9uYWwgYWwgaGFjZXIgY3JlY2VyIHVuYSBsaXN0YS4gQXPDrSBkaXN0cmlidXllIGVsIGNvc3RlIGRlIGxhcyByZWFsb2NhY2lvbmVzIGVudHJlIG11Y2hhcyBsbGFtYWRhcyBhIGFwcGVuZCB5IGNvbnNpZ3VlIE8oMSkgYW1vcnRpemFkby4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtBbG1hY2VuYSBjYWRhIGVsZW1lbnRvIGVuIHVuIGFyY2hpdm8gdGVtcG9yYWwgZW4gZGlzY2/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gQ1B5dGhvbiByZXNlcnZhIGNhcGFjaWRhZCBhZGljaW9uYWwgYWwgaGFjZXIgY3JlY2VyIHVuYSBsaXN0YS4gQXPDrSBkaXN0cmlidXllIGVsIGNvc3RlIGRlIGxhcyByZWFsb2NhY2lvbmVzIGVudHJlIG11Y2hhcyBsbGFtYWRhcyBhIGFwcGVuZCB5IGNvbnNpZ3VlIE8oMSkgYW1vcnRpemFkby4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtVc2EgdW5hIGxpc3RhIGRvYmxlbWVudGUgZW5sYXphZGEgc2luIGJsb3F1ZXMgY29udGlndW9zwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIENQeXRob24gcmVzZXJ2YSBjYXBhY2lkYWQgYWRpY2lvbmFsIGFsIGhhY2VyIGNyZWNlciB1bmEgbGlzdGEuIEFzw60gZGlzdHJpYnV5ZSBlbCBjb3N0ZSBkZSBsYXMgcmVhbG9jYWNpb25lcyBlbnRyZSBtdWNoYXMgbGxhbWFkYXMgYSBhcHBlbmQgeSBjb25zaWd1ZSBPKDEpIGFtb3J0aXphZG8u' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtMaW1pdGEgZWwgdGFtYcOxbyBtw6F4aW1vIGRlIGxhcyBsaXN0YXMgYSAyNTYgZWxlbWVudG9zwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIENQeXRob24gcmVzZXJ2YSBjYXBhY2lkYWQgYWRpY2lvbmFsIGFsIGhhY2VyIGNyZWNlciB1bmEgbGlzdGEuIEFzw60gZGlzdHJpYnV5ZSBlbCBjb3N0ZSBkZSBsYXMgcmVhbG9jYWNpb25lcyBlbnRyZSBtdWNoYXMgbGxhbWFkYXMgYSBhcHBlbmQgeSBjb25zaWd1ZSBPKDEpIGFtb3J0aXphZG8u');
    add_guidance(211, 'Conjuntos Hash en Python: Primer Duplicado en O(n)', 'Completa primer_duplicado(nums: list) -> int utilizando un conjunto (set) para retornar el primer valor repetido o -1 si no hay duplicados.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TmVjZXNpdGFzIHJlY29yZGFyIGxvcyB2YWxvcmVzIHF1ZSB5YSBhcGFyZWNpZXJvbiBkdXJhbnRlIGVsIHJlY29ycmlkby4=' || CHR(10) ||
        'VW4gY29uanVudG8gcGVybWl0ZSBjb21wcm9iYXIgc2kgdW4gZWxlbWVudG8geWEgc2UgaGEgdmlzaXRhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u');
    add_guidance(212, 'Quiz Conceptual: Elementos Hashables en set y dict', '¿Por qué una tupla (tuple) de enteros puede ser clave en un dict o elemento de un set en Python, pero una lista (list) no?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'VW5hIHR1cGxhIGRlIGVudGVyb3MgZXMgaW5tdXRhYmxlIHkgdGllbmUgdW4gaGFzaCBlc3RhYmxlLiBMYXMgbGlzdGFzIHNvbiBtdXRhYmxlcyB5IG5vIHNvbiBoYXNoYWJsZXMsIHBvciBlc28gbm8gcHVlZGVuIHNlciBjbGF2ZXMgZGUgdW4gZGljdCBuaSBlbGVtZW50b3MgZGUgdW4gc2V0Lg==' || CHR(10) ||
        'VW5hIHR1cGxhIGRlIGVudGVyb3MgZXMgaW5tdXRhYmxlIHkgdGllbmUgdW4gaGFzaCBlc3RhYmxlLiBMYXMgbGlzdGFzIHNvbiBtdXRhYmxlcyB5IG5vIHNvbiBoYXNoYWJsZXMsIHBvciBlc28gbm8gcHVlZGVuIHNlciBjbGF2ZXMgZGUgdW4gZGljdCBuaSBlbGVtZW50b3MgZGUgdW4gc2V0Lg==' || CHR(10) ||
        'VW5hIHR1cGxhIGRlIGVudGVyb3MgZXMgaW5tdXRhYmxlIHkgdGllbmUgdW4gaGFzaCBlc3RhYmxlLiBMYXMgbGlzdGFzIHNvbiBtdXRhYmxlcyB5IG5vIHNvbiBoYXNoYWJsZXMsIHBvciBlc28gbm8gcHVlZGVuIHNlciBjbGF2ZXMgZGUgdW4gZGljdCBuaSBlbGVtZW50b3MgZGUgdW4gc2V0Lg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgbGlzdCBvY3VwYSBtw6FzIGRlIDY0IGJpdHMgZW4gbWVtb3JpYcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBVbmEgdHVwbGEgZGUgZW50ZXJvcyBlcyBpbm11dGFibGUgeSB0aWVuZSB1biBoYXNoIGVzdGFibGUuIExhcyBsaXN0YXMgc29uIG11dGFibGVzIHkgbm8gc29uIGhhc2hhYmxlcywgcG9yIGVzbyBubyBwdWVkZW4gc2VyIGNsYXZlcyBkZSB1biBkaWN0IG5pIGVsZW1lbnRvcyBkZSB1biBzZXQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgdHVwbGUgb3JkZW5hIHN1cyBlbGVtZW50b3MgYXV0b23DoXRpY2FtZW50ZSBhbCBjcmVhcnNlwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIFVuYSB0dXBsYSBkZSBlbnRlcm9zIGVzIGlubXV0YWJsZSB5IHRpZW5lIHVuIGhhc2ggZXN0YWJsZS4gTGFzIGxpc3RhcyBzb24gbXV0YWJsZXMgeSBubyBzb24gaGFzaGFibGVzLCBwb3IgZXNvIG5vIHB1ZWRlbiBzZXIgY2xhdmVzIGRlIHVuIGRpY3QgbmkgZWxlbWVudG9zIGRlIHVuIHNldC4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgbG9zIGNvbmp1bnRvcyBzZXQgc29sbyBhY2VwdGFuIGNhZGVuYXMgZGUgdGV4dG/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gVW5hIHR1cGxhIGRlIGVudGVyb3MgZXMgaW5tdXRhYmxlIHkgdGllbmUgdW4gaGFzaCBlc3RhYmxlLiBMYXMgbGlzdGFzIHNvbiBtdXRhYmxlcyB5IG5vIHNvbiBoYXNoYWJsZXMsIHBvciBlc28gbm8gcHVlZGVuIHNlciBjbGF2ZXMgZGUgdW4gZGljdCBuaSBlbGVtZW50b3MgZGUgdW4gc2V0Lg==');
    add_guidance(105, 'Polimorfismo en C++: Clasificación de Sonidos', 'Completa emitirSonido(std::string tipo) para retornar "Guau" cuando reciba "Perro" y "Miau" cuando reciba "Gato".',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHNvbmlkbyBjb3JyZXNwb25kZSBhIGNhZGEgZXNwZWNpZS4=' || CHR(10) ||
        'Q29tcGFyYSBsb3Mgbm9tYnJlcyBpZ25vcmFuZG8gbGFzIGRpZmVyZW5jaWFzIGVudHJlIG1hecO6c2N1bGFzIHkgbWluw7pzY3VsYXMu' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==');
    add_guidance(302, 'Quiz Conceptual: Destructor Virtual en C++', '¿Qué riesgo crítico ocurre en C++ si eliminas con delete un objeto derivado a través de un puntero a su clase base que NO tiene destructor virtual?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWxpbWluYXIgdW4gb2JqZXRvIGRlcml2YWRvIG1lZGlhbnRlIHVuIHB1bnRlcm8gYmFzZSByZXF1aWVyZSB1biBkZXN0cnVjdG9yIHZpcnR1YWwgcGFyYSBxdWUgc2UgZWplY3V0ZSBjb3JyZWN0YW1lbnRlIGxhIGRlc3RydWNjacOzbiBkZSBsYSBqZXJhcnF1w61hLg==' || CHR(10) ||
        'RWxpbWluYXIgdW4gb2JqZXRvIGRlcml2YWRvIG1lZGlhbnRlIHVuIHB1bnRlcm8gYmFzZSByZXF1aWVyZSB1biBkZXN0cnVjdG9yIHZpcnR1YWwgcGFyYSBxdWUgc2UgZWplY3V0ZSBjb3JyZWN0YW1lbnRlIGxhIGRlc3RydWNjacOzbiBkZSBsYSBqZXJhcnF1w61hLg==' || CHR(10) ||
        'RWxpbWluYXIgdW4gb2JqZXRvIGRlcml2YWRvIG1lZGlhbnRlIHVuIHB1bnRlcm8gYmFzZSByZXF1aWVyZSB1biBkZXN0cnVjdG9yIHZpcnR1YWwgcGFyYSBxdWUgc2UgZWplY3V0ZSBjb3JyZWN0YW1lbnRlIGxhIGRlc3RydWNjacOzbiBkZSBsYSBqZXJhcnF1w61hLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBjb21waWxhZG9yIGRlIEMrKyBjb252aWVydGUgdG9kb3MgbG9zIG3DqXRvZG9zIGRlIGxhIGNsYXNlIGVuIHByaXZhZG9zwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsaW1pbmFyIHVuIG9iamV0byBkZXJpdmFkbyBtZWRpYW50ZSB1biBwdW50ZXJvIGJhc2UgcmVxdWllcmUgdW4gZGVzdHJ1Y3RvciB2aXJ0dWFsIHBhcmEgcXVlIHNlIGVqZWN1dGUgY29ycmVjdGFtZW50ZSBsYSBkZXN0cnVjY2nDs24gZGUgbGEgamVyYXJxdcOtYS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBwdW50ZXJvIGJhc2Ugc2UgbXVsdGlwbGljYSBwb3IgZG9zIGVuIG1lbW9yaWHCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRWxpbWluYXIgdW4gb2JqZXRvIGRlcml2YWRvIG1lZGlhbnRlIHVuIHB1bnRlcm8gYmFzZSByZXF1aWVyZSB1biBkZXN0cnVjdG9yIHZpcnR1YWwgcGFyYSBxdWUgc2UgZWplY3V0ZSBjb3JyZWN0YW1lbnRlIGxhIGRlc3RydWNjacOzbiBkZSBsYSBqZXJhcnF1w61hLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCByZWNvbGVjdG9yIGRlIGJhc3VyYSBkZSBDKysgZGV0aWVuZSBlbCBwcm9ncmFtYSBpbm1lZGlhdGFtZW50ZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbGltaW5hciB1biBvYmpldG8gZGVyaXZhZG8gbWVkaWFudGUgdW4gcHVudGVybyBiYXNlIHJlcXVpZXJlIHVuIGRlc3RydWN0b3IgdmlydHVhbCBwYXJhIHF1ZSBzZSBlamVjdXRlIGNvcnJlY3RhbWVudGUgbGEgZGVzdHJ1Y2Npw7NuIGRlIGxhIGplcmFycXXDrWEu');
    add_guidance(303, 'Contratos en C++: Clasificador de Nivel de Acceso', 'Completa nivelAcceso(std::string rol) para retornar "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE".',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U2VwYXJhIGxvcyByb2xlcyBxdWUgdGllbmVuIGFjY2VzbyBkZSBsb3MgcXVlIGRlYmVuIHNlciByZWNoYXphZG9zLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGVsIHJvbCBhbnRlcyBkZSBjb21wYXJhcmxvIHkgY29uc2VydmEgZWwgdGV4dG8gZGUgc2FsaWRhIGRlbCBlbnVuY2lhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu');
    add_guidance(304, 'Quiz Conceptual: std::unique_ptr y Semántica RAII', '¿Cuál es la característica principal de std::unique_ptr<T> introducido en el C++ moderno frente a los punteros crudos con new/delete?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'UkFJSSB2aW5jdWxhIGxhIHZpZGEgZGVsIHJlY3Vyc28gYSBsYSBkZSBzdSBvYmpldG8uIHVuaXF1ZV9wdHIgZXhwcmVzYSBwcm9waWVkYWQgZXhjbHVzaXZhIHkgbGliZXJhIGVsIHJlY3Vyc28gYWwgZGVzdHJ1aXJzZS4=' || CHR(10) ||
        'UkFJSSB2aW5jdWxhIGxhIHZpZGEgZGVsIHJlY3Vyc28gYSBsYSBkZSBzdSBvYmpldG8uIHVuaXF1ZV9wdHIgZXhwcmVzYSBwcm9waWVkYWQgZXhjbHVzaXZhIHkgbGliZXJhIGVsIHJlY3Vyc28gYWwgZGVzdHJ1aXJzZS4=' || CHR(10) ||
        'UkFJSSB2aW5jdWxhIGxhIHZpZGEgZGVsIHJlY3Vyc28gYSBsYSBkZSBzdSBvYmpldG8uIHVuaXF1ZV9wdHIgZXhwcmVzYSBwcm9waWVkYWQgZXhjbHVzaXZhIHkgbGliZXJhIGVsIHJlY3Vyc28gYWwgZGVzdHJ1aXJzZS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQZXJtaXRlIHF1ZSBtw7psdGlwbGVzIHB1bnRlcm9zIHNlYW4gZHVlw7FvcyBzaW11bHTDoW5lb3MgbWVkaWFudGUgY29udGVvIGF0w7NtaWNvwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIFJBSUkgdmluY3VsYSBsYSB2aWRhIGRlbCByZWN1cnNvIGEgbGEgZGUgc3Ugb2JqZXRvLiB1bmlxdWVfcHRyIGV4cHJlc2EgcHJvcGllZGFkIGV4Y2x1c2l2YSB5IGxpYmVyYSBlbCByZWN1cnNvIGFsIGRlc3RydWlyc2Uu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtSZXF1aWVyZSBsbGFtYXIgbWFudWFsbWVudGUgYSBmcmVlKCkgYW50ZXMgZGUgcXVlIHRlcm1pbmUgbGEgZnVuY2nDs27CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gUkFJSSB2aW5jdWxhIGxhIHZpZGEgZGVsIHJlY3Vyc28gYSBsYSBkZSBzdSBvYmpldG8uIHVuaXF1ZV9wdHIgZXhwcmVzYSBwcm9waWVkYWQgZXhjbHVzaXZhIHkgbGliZXJhIGVsIHJlY3Vyc28gYWwgZGVzdHJ1aXJzZS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtEZXNhY3RpdmEgbGFzIGNvbXByb2JhY2lvbmVzIGRlIHRpcG9zIGRlbCBjb21waWxhZG9yIGcrK8K7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBSQUlJIHZpbmN1bGEgbGEgdmlkYSBkZWwgcmVjdXJzbyBhIGxhIGRlIHN1IG9iamV0by4gdW5pcXVlX3B0ciBleHByZXNhIHByb3BpZWRhZCBleGNsdXNpdmEgeSBsaWJlcmEgZWwgcmVjdXJzbyBhbCBkZXN0cnVpcnNlLg==');
    add_guidance(305, 'Búsqueda Binaria en C++ sobre const std::vector<int>&', 'Completa search(const std::vector<int>& a, int target) retornando el índice donde se ubica target o -1 si no está presente.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TGEgc2VjdWVuY2lhIGVzdMOhIG9yZGVuYWRhLiBVc2EgZXNlIG9yZGVuIHBhcmEgZGVzY2FydGFyIHVuYSBwYXJ0ZSBkZSBsYSBiw7pzcXVlZGEu' || CHR(10) ||
        'TWFudMOpbiBsb3MgbMOtbWl0ZXMgZGVsIGludGVydmFsbyB5IGNhbGN1bGEgc3UgcHVudG8gbWVkaW8uIEVuIFB5dGhvbiBuZWNlc2l0YXMgZGl2aXNpw7NuIGVudGVyYS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=');
    add_guidance(306, 'Quiz Conceptual: Paso por Referencia Constante (const T&)', '¿Por qué una función de búsqueda en C++ debe recibir const std::vector<int>& en lugar de std::vector<int> por valor?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'VW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==' || CHR(10) ||
        'VW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==' || CHR(10) ||
        'VW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPYmxpZ2EgYWwgY29tcGlsYWRvciBhIGNvcGlhciBlbCB2ZWN0b3IgY29tcGxldG8gZW4gZWwgcmVnaXN0cm8gZGVsIHByb2Nlc2Fkb3LCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gVW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtDb252aWVydGUgZWwgdmVjdG9yIGVuIHVuYSBsaXN0YSBlbmxhemFkYSBkaW7DoW1pY2FtZW50ZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBVbmEgcmVmZXJlbmNpYSBjb25zdGFudGUgZXZpdGEgY29waWFyIGVsIHZlY3RvciB5IHBlcm1pdGUgbGVlcmxvIHNpbiBtb2RpZmljYXJsbyBhIHRyYXbDqXMgZGUgZXNhIHJlZmVyZW5jaWEu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQZXJtaXRlIG1vZGlmaWNhciBsaWJyZW1lbnRlIGVsIHRhbWHDsW8gZGVsIHZlY3RvciBkZW50cm8gZGUgbGEgZnVuY2nDs27CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gVW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==');
    add_guidance(307, 'Dos Índices en C++: Verificador de Palíndromos', 'Completa esPalindromo(std::string texto) recorriendo desde los extremos left y right para evaluar si la cadena es palíndroma.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'VW4gcGFsw61uZHJvbW8gY29uc2VydmEgc3VzIGNhcmFjdGVyZXMgYWwgbGVlcmxvIGVuIHNlbnRpZG8gY29udHJhcmlvLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGxhcyBtYXnDunNjdWxhcyB5IGNvbXBhcmEgbG9zIGRvcyBleHRyZW1vcyBkZSBsYSBzZWN1ZW5jaWEu' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u');
    add_guidance(308, 'Quiz Conceptual: Iteradores de Acceso Aleatorio en STL', '¿Qué contenedores de la STL de C++ ofrecen iteradores de acceso aleatorio (Random Access Iterators) en tiempo O(1)?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'dmVjdG9yLCBkZXF1ZSB5IHN0cmluZyBvZnJlY2VuIGl0ZXJhZG9yZXMgZGUgYWNjZXNvIGFsZWF0b3JpbyBjb24gc2FsdG9zIGl0ICsgbiBlbiBPKDEpLiBMb3MgaXRlcmFkb3JlcyBkZSBsaXN0IGRlYmVuIGF2YW56YXIgZW5sYWNlIHBvciBlbmxhY2Uu' || CHR(10) ||
        'dmVjdG9yLCBkZXF1ZSB5IHN0cmluZyBvZnJlY2VuIGl0ZXJhZG9yZXMgZGUgYWNjZXNvIGFsZWF0b3JpbyBjb24gc2FsdG9zIGl0ICsgbiBlbiBPKDEpLiBMb3MgaXRlcmFkb3JlcyBkZSBsaXN0IGRlYmVuIGF2YW56YXIgZW5sYWNlIHBvciBlbmxhY2Uu' || CHR(10) ||
        'dmVjdG9yLCBkZXF1ZSB5IHN0cmluZyBvZnJlY2VuIGl0ZXJhZG9yZXMgZGUgYWNjZXNvIGFsZWF0b3JpbyBjb24gc2FsdG9zIGl0ICsgbiBlbiBPKDEpLiBMb3MgaXRlcmFkb3JlcyBkZSBsaXN0IGRlYmVuIGF2YW56YXIgZW5sYWNlIHBvciBlbmxhY2Uu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqvDmm5pY2FtZW50ZSBzdGQ6Omxpc3QgeSBzdGQ6OmZvcndhcmRfbGlzdMK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiB2ZWN0b3IsIGRlcXVlIHkgc3RyaW5nIG9mcmVjZW4gaXRlcmFkb3JlcyBkZSBhY2Nlc28gYWxlYXRvcmlvIGNvbiBzYWx0b3MgaXQgKyBuIGVuIE8oMSkuIExvcyBpdGVyYWRvcmVzIGRlIGxpc3QgZGViZW4gYXZhbnphciBlbmxhY2UgcG9yIGVubGFjZS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtTb2xvIHN0ZDo6bWFwIHkgc3RkOjpzZXTCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gdmVjdG9yLCBkZXF1ZSB5IHN0cmluZyBvZnJlY2VuIGl0ZXJhZG9yZXMgZGUgYWNjZXNvIGFsZWF0b3JpbyBjb24gc2FsdG9zIGl0ICsgbiBlbiBPKDEpLiBMb3MgaXRlcmFkb3JlcyBkZSBsaXN0IGRlYmVuIGF2YW56YXIgZW5sYWNlIHBvciBlbmxhY2Uu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtOaW5nw7puIGNvbnRlbmVkb3IgZGUgbGEgU1RMIHNvcG9ydGEgYWNjZXNvIGFsZWF0b3Jpb8K7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiB2ZWN0b3IsIGRlcXVlIHkgc3RyaW5nIG9mcmVjZW4gaXRlcmFkb3JlcyBkZSBhY2Nlc28gYWxlYXRvcmlvIGNvbiBzYWx0b3MgaXQgKyBuIGVuIE8oMSkuIExvcyBpdGVyYWRvcmVzIGRlIGxpc3QgZGViZW4gYXZhbnphciBlbmxhY2UgcG9yIGVubGFjZS4=');
    add_guidance(309, 'STL std::vector: Acumulador de Elementos Pares', 'Completa sumarPares(const std::vector<int>& nums) para acumular y retornar la suma de todos los enteros pares del vector.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U29sbyBkZWJlbiBjb250cmlidWlyIGFsIHJlc3VsdGFkbyBsb3MgbsO6bWVyb3MgcXVlIGN1bXBsZW4gbGEgY29uZGljacOzbiBkZSBwYXJpZGFkLg==' || CHR(10) ||
        'UHJlcGFyYSB1biBhY3VtdWxhZG9yIHkgcmVjb3JyZSBjYWRhIGVsZW1lbnRvIGRlIGxhIGNvbGVjY2nDs24u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu');
    add_guidance(310, 'Quiz Conceptual: size() vs capacity() en std::vector', '¿Qué hace el método reserve(n) en un std::vector<T> de C++ antes de insertar una gran cantidad de elementos?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'cmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==' || CHR(10) ||
        'cmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==' || CHR(10) ||
        'cmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtSZWxsZW5hIGVsIHZlY3RvciBjb24gbiBjZXJvcyBtb2RpZmljYW5kbyBzdSBzaXplKCkgaW5tZWRpYXRhbWVudGXCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gcmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPcmRlbmEgbG9zIGVsZW1lbnRvcyBkZSBtYXlvciBhIG1lbm9yIGNhcGFjaWRhZMK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiByZXNlcnZlKG4pIGdhcmFudGl6YSBjYXBhY2lkYWQgcGFyYSBhbCBtZW5vcyBuIGVsZW1lbnRvcyB5IHJlZHVjZSByZWFsb2NhY2lvbmVzIHBvc3RlcmlvcmVzLiBMYSBjYXBhY2lkYWQgcHVlZGUgYXVtZW50YXIsIHBlcm8gZWwgdGFtYcOxbyB5IGxvcyBlbGVtZW50b3MgZXhpc3RlbnRlcyBzZSBjb25zZXJ2YW4u' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtCbG9xdWVhIGVsIHZlY3RvciBpbXBpZGllbmRvIGFncmVnYXIgbcOhcyBlbGVtZW50b3PCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gcmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==');
    add_guidance(311, 'STL std::unordered_set: Primer Duplicado en O(n)', 'Completa primerDuplicado(const std::vector<int>& nums) empleando std::unordered_set<int> para retornar el primer valor repetido o -1.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TmVjZXNpdGFzIHJlY29yZGFyIGxvcyB2YWxvcmVzIHF1ZSB5YSBhcGFyZWNpZXJvbiBkdXJhbnRlIGVsIHJlY29ycmlkby4=' || CHR(10) ||
        'VW4gY29uanVudG8gcGVybWl0ZSBjb21wcm9iYXIgc2kgdW4gZWxlbWVudG8geWEgc2UgaGEgdmlzaXRhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u');
    add_guidance(312, 'Quiz Conceptual: std::unordered_map vs std::map', '¿Cuál es la diferencia estructural y de complejidad temporal entre std::unordered_map y std::map en la STL de C++?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'dW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=' || CHR(10) ||
        'dW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=' || CHR(10) ||
        'dW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtBbWJvcyB1c2FuIHRhYmxhcyBoYXNoIGlkw6ludGljYXMgcGVybyBjb24gZGlzdGludG8gbm9tYnJlIGRlIGNhYmVjZXJhwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIHVub3JkZXJlZF9tYXAgdXNhIHVuYSB0YWJsYSBoYXNoIGNvbiBiw7pzcXVlZGEgcHJvbWVkaW8gTygxKS4gbWFwIGNvbnNlcnZhIGxhcyBjbGF2ZXMgb3JkZW5hZGFzIHkgb2ZyZWNlIGLDunNxdWVkYSBPKGxvZyBuKSwgbm9ybWFsbWVudGUgbWVkaWFudGUgdW4gw6FyYm9sIGJhbGFuY2VhZG8u' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtzdGQ6Om1hcCBubyBwZXJtaXRlIGFsbWFjZW5hciBlbnRlcm9zIGNvbW8gdmFsb3IgYXNvY2lhZG/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gdW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtzdGQ6OnVub3JkZXJlZF9tYXAgbWFudGllbmUgbGFzIGNsYXZlcyBvcmRlbmFkYXMgYWxmYWLDqXRpY2FtZW50ZSBlbiB0b2RvIG1vbWVudG/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gdW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=');
    DBMS_OUTPUT.PUT_LINE('Guias didacticas incorporadas: ' || v_updated);
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK TO guidance_seed;
    RAISE;
END;
/
