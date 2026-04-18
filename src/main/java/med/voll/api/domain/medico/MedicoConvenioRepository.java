package med.voll.api.domain.medico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicoConvenioRepository extends JpaRepository<MedicoConvenio, Long> {

    List<MedicoConvenio> findAllByMedicoIdAndAtivoTrue(Long medicoId);

    boolean existsByMedicoIdAndConvenioIdAndAtivoTrue(Long medicoId, Long convenioId);

    Optional<MedicoConvenio> findByMedicoIdAndConvenioId(Long medicoId, Long convenioId);
}
