package med.voll.api.domain.prontuario;

import java.time.LocalDateTime;

public record DadosDetalhamentoProntuario(
        Long id,
        Long consultaId,
        String nomeMedico,
        String nomePaciente,
        String anamnese,
        String diagnostico,
        String cid10,
        String observacoes,
        LocalDateTime dataRegistro
) {
    public DadosDetalhamentoProntuario(Prontuario p) {
        this(
                p.getId(),
                p.getConsulta().getId(),
                p.getMedico().getNome(),
                p.getPaciente().getNome(),
                p.getAnamnese(),
                p.getDiagnostico(),
                p.getCid10(),
                p.getObservacoes(),
                p.getDataRegistro()
        );
    }
}
