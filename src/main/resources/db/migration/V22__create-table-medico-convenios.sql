CREATE TABLE medico_convenios (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    medico_id   BIGINT      NOT NULL,
    convenio_id BIGINT      NOT NULL,
    ativo       TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_medico_convenio (medico_id, convenio_id),
    CONSTRAINT fk_medico_convenios_medico   FOREIGN KEY (medico_id)   REFERENCES medicos(id),
    CONSTRAINT fk_medico_convenios_convenio FOREIGN KEY (convenio_id) REFERENCES convenios(id)
);
