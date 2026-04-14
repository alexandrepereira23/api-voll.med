package med.voll.api.domain.prontuario;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoProntuario(
        @NotNull Long id,
        String anamnese,
        String diagnostico,
        String cid10,
        String observacoes
) {}
