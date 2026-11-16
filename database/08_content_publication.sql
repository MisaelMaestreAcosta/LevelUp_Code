-- Run once after the base schema, as its owner.
ALTER TABLE Retos ADD (
    estado VARCHAR2(15) DEFAULT 'PUBLICADO' NOT NULL,
    revision NUMBER DEFAULT 1 NOT NULL,
    fecha_actualizacion TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT ck_retos_estado CHECK (estado IN ('BORRADOR','PUBLICADO','ARCHIVADO'))
);
CREATE TABLE Solucion_Referencia (
    id_reto NUMBER PRIMARY KEY,
    codigo_solucion CLOB NOT NULL,
    CONSTRAINT fk_solucion_reto FOREIGN KEY (id_reto) REFERENCES Reto_Codigo(id_reto) ON DELETE CASCADE
);
CREATE INDEX idx_retos_estado ON Retos(estado, id_leccion);
