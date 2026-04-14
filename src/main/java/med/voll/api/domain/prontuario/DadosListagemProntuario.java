package med.voll.api.domain.prontuario;

import java.time.LocalDateTime;

public record DadosListagemProntuario(
        Long id,
        Long consultaId,
        String nomePaciente,
        String diagnostico,
        LocalDateTime dataRegistro
) {
    public DadosListagemProntuario(Prontuario p) {
        this(
                p.getId(),
                p.getConsulta().getId(),
                p.getPaciente().getNome(),
                p.getDiagnostico(),
                p.getDataRegistro()
        );
    }
}
