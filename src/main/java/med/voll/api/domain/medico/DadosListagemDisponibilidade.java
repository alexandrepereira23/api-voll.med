package med.voll.api.domain.medico;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DadosListagemDisponibilidade(
        Long id,
        DayOfWeek diaSemana,
        LocalTime horaInicio,
        LocalTime horaFim
) {
    public DadosListagemDisponibilidade(DisponibilidadeMedico d) {
        this(d.getId(), d.getDiaSemana(), d.getHoraInicio(), d.getHoraFim());
    }
}
