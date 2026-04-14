CREATE TABLE disponibilidade_medico (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    medico_id   BIGINT NOT NULL,
    dia_semana  VARCHAR(20) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim    TIME NOT NULL,
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_disponibilidade_medico FOREIGN KEY (medico_id) REFERENCES medicos(id)
);
