package med.voll.api.domain.prontuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroProntuario(
        @NotNull Long consultaId,
        @NotBlank String anamnese,
        @NotBlank String diagnostico,
        String cid10,
        String observacoes
) {}
