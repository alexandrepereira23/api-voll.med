CREATE TABLE prontuarios (
    id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    consulta_id   BIGINT NOT NULL UNIQUE,
    medico_id     BIGINT NOT NULL,
    paciente_id   BIGINT NOT NULL,
    anamnese      TEXT NOT NULL,
    diagnostico   VARCHAR(500) NOT NULL,
    cid10         VARCHAR(10),
    observacoes   TEXT,
    data_registro DATETIME NOT NULL,
    ativo         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_prontuarios_consulta  FOREIGN KEY (consulta_id)  REFERENCES consultas(id),
    CONSTRAINT fk_prontuarios_medico    FOREIGN KEY (medico_id)    REFERENCES medicos(id),
    CONSTRAINT fk_prontuarios_paciente  FOREIGN KEY (paciente_id)  REFERENCES pacientes(id)
);
