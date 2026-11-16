-- Run once after 09_content_versions.sql.
ALTER TABLE Retos DROP CONSTRAINT ck_retos_tipo;
ALTER TABLE Retos ADD CONSTRAINT ck_retos_tipo CHECK (tipo_reto IN ('CODIGO','QUIZ','PREDICCION','DEPURACION','ORDENAR'));
CREATE TABLE Reto_Prediccion (
    id_reto NUMBER PRIMARY KEY,
    codigo_ejemplo CLOB NOT NULL,
    CONSTRAINT fk_prediccion_reto FOREIGN KEY (id_reto) REFERENCES Reto_Quiz(id_reto) ON DELETE CASCADE
);
CREATE TABLE Reto_Ordenamiento (
    id_reto NUMBER PRIMARY KEY,
    fragmentos CLOB NOT NULL,
    orden_correcto VARCHAR2(200) NOT NULL,
    CONSTRAINT fk_ordenamiento_reto FOREIGN KEY (id_reto) REFERENCES Retos(id_reto) ON DELETE CASCADE
);
-- Debugging uses the same code/template/time-limit/test-case structure as Reto_Codigo.
