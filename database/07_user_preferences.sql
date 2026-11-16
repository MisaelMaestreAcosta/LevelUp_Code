-- Run once as the application schema owner after 01_schema.sql.
CREATE TABLE Preferencia_Usuario (
    id_usuario NUMBER PRIMARY KEY,
    lenguaje_preferido VARCHAR2(10) DEFAULT 'JAVA' NOT NULL,
    tamano_editor NUMBER DEFAULT 14 NOT NULL,
    espacios_indentacion NUMBER DEFAULT 4 NOT NULL,
    reducir_animaciones NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT fk_preferencia_usuario FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT ck_preferencia_lenguaje CHECK (lenguaje_preferido IN ('JAVA','PYTHON','CPP')),
    CONSTRAINT ck_preferencia_fuente CHECK (tamano_editor BETWEEN 12 AND 24 AND tamano_editor=TRUNC(tamano_editor)),
    CONSTRAINT ck_preferencia_indent CHECK (espacios_indentacion IN (2,4,8)),
    CONSTRAINT ck_preferencia_animacion CHECK (reducir_animaciones IN (0,1))
);
