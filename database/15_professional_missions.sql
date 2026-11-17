-- Apply after 14_module_finals.sql as the application schema owner.
-- Adds mission briefs and requirements without changing existing attempts.
DECLARE
    PROCEDURE create_if_missing(p_type VARCHAR2, p_name VARCHAR2, p_ddl VARCHAR2) IS
        v_count NUMBER;
    BEGIN
        IF p_type='TABLE' THEN
            SELECT COUNT(*) INTO v_count FROM user_tables WHERE table_name=p_name;
        ELSE
            SELECT COUNT(*) INTO v_count FROM user_constraints
            WHERE table_name='CASOS_PRUEBA' AND constraint_name=p_name;
        END IF;
        IF v_count=0 THEN EXECUTE IMMEDIATE p_ddl; END IF;
    END;
BEGIN
    create_if_missing('CONSTRAINT','UQ_CASO_RETO_ID',
        'ALTER TABLE Casos_Prueba ADD CONSTRAINT uq_caso_reto_id UNIQUE(id_reto,id_caso)');

    create_if_missing('TABLE','MISION_PROFESIONAL',q'[
        CREATE TABLE Mision_Profesional (
            codigo VARCHAR2(60 CHAR) PRIMARY KEY,
            id_reto NUMBER NOT NULL UNIQUE,
            cliente VARCHAR2(120 CHAR) NOT NULL,
            encargo CLOB NOT NULL,
            CONSTRAINT uq_mision_codigo_reto UNIQUE(codigo,id_reto),
            CONSTRAINT fk_mision_reto FOREIGN KEY(id_reto)
                REFERENCES Reto_Codigo(id_reto) ON DELETE CASCADE
        )]');

    create_if_missing('TABLE','REQUISITO_MISION',q'[
        CREATE TABLE Requisito_Mision (
            codigo_mision VARCHAR2(60 CHAR) NOT NULL,
            orden NUMBER(6,0) NOT NULL CHECK(orden>0),
            descripcion VARCHAR2(500 CHAR) NOT NULL,
            CONSTRAINT pk_requisito_mision PRIMARY KEY(codigo_mision,orden),
            CONSTRAINT fk_requisito_mision FOREIGN KEY(codigo_mision)
                REFERENCES Mision_Profesional(codigo) ON DELETE CASCADE
        )]');

    create_if_missing('TABLE','REQUISITO_CASO_PRUEBA',q'[
        CREATE TABLE Requisito_Caso_Prueba (
            codigo_mision VARCHAR2(60 CHAR) NOT NULL,
            orden_requisito NUMBER(6,0) NOT NULL,
            id_reto NUMBER NOT NULL,
            id_caso NUMBER NOT NULL,
            CONSTRAINT pk_requisito_caso PRIMARY KEY(codigo_mision,orden_requisito,id_caso),
            CONSTRAINT fk_rcp_requisito FOREIGN KEY(codigo_mision,orden_requisito)
                REFERENCES Requisito_Mision(codigo_mision,orden) ON DELETE CASCADE,
            CONSTRAINT fk_rcp_mision_reto FOREIGN KEY(codigo_mision,id_reto)
                REFERENCES Mision_Profesional(codigo,id_reto) ON DELETE CASCADE,
            CONSTRAINT fk_rcp_caso_reto FOREIGN KEY(id_reto,id_caso)
                REFERENCES Casos_Prueba(id_reto,id_caso) ON DELETE CASCADE
        )]');
END;
/
