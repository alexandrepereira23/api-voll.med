package med.voll.api.domain.prontuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {

    Page<Prontuario> findAllByAtivoTrue(Pageable pageable);

    Page<Prontuario> findAllByAtivoTrueAndMedicoId(Long medicoId, Pageable pageable);

    Page<Prontuario> findAllByAtivoTrueAndPacienteId(Long pacienteId, Pageable pageable);

    Page<Prontuario> findAllByAtivoTrueAndMedicoIdAndPacienteId(Long medicoId, Long pacienteId, Pageable pageable);

    boolean existsByConsultaId(Long consultaId);
}
