package med.voll.api.domain.medico;

import jakarta.persistence.*;
import lombok.*;
import med.voll.api.domain.convenio.Convenio;

@Table(name = "medico_convenios")
@Entity(name = "MedicoConvenio")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class MedicoConvenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id")
    private Convenio convenio;

    private boolean ativo;

    public MedicoConvenio(Medico medico, Convenio convenio) {
        this.medico = medico;
        this.convenio = convenio;
        this.ativo = true;
    }

    public void desvincular() {
        this.ativo = false;
    }
}
