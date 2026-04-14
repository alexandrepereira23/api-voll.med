package med.voll.api.domain.prontuario;

import jakarta.persistence.*;
import lombok.*;
import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.paciente.Paciente;

import java.time.LocalDateTime;

@Table(name = "prontuarios")
@Entity(name = "Prontuario")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(columnDefinition = "TEXT")
    private String anamnese;

    private String diagnostico;

    private String cid10;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    private LocalDateTime dataRegistro;

    private boolean ativo;

    public Prontuario(Consulta consulta, Medico medico, Paciente paciente,
                      String anamnese, String diagnostico, String cid10, String observacoes) {
        this.consulta = consulta;
        this.medico = medico;
        this.paciente = paciente;
        this.anamnese = anamnese;
        this.diagnostico = diagnostico;
        this.cid10 = cid10;
        this.observacoes = observacoes;
        this.dataRegistro = LocalDateTime.now();
        this.ativo = true;
    }

    public boolean podeEditar() {
        return LocalDateTime.now().isBefore(dataRegistro.plusHours(24));
    }

    public void atualizar(DadosAtualizacaoProntuario dados) {
        if (dados.anamnese() != null) this.anamnese = dados.anamnese();
        if (dados.diagnostico() != null) this.diagnostico = dados.diagnostico();
        if (dados.cid10() != null) this.cid10 = dados.cid10();
        if (dados.observacoes() != null) this.observacoes = dados.observacoes();
    }

    public void inativar() {
        this.ativo = false;
    }
}
