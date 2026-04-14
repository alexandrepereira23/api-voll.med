package med.voll.api.domain.medico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface DisponibilidadeMedicoRepository extends JpaRepository<DisponibilidadeMedico, Long> {

    List<DisponibilidadeMedico> findAllByMedicoIdAndAtivoTrue(Long medicoId);

    boolean existsByMedicoIdAndDiaSemanaAndHoraInicioLessThanEqualAndHoraFimGreaterThanAndAtivoTrue(
            Long medicoId, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim);
}
