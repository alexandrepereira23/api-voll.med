package med.voll.api.domain.consulta;

import med.voll.api.domain.convenio.Convenio;
import med.voll.api.domain.convenio.ConvenioRepository;
import med.voll.api.exception.ValidacaoException;
import med.voll.api.domain.medico.DisponibilidadeMedicoRepository;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoConvenioRepository;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacienteRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AgendaDeConsultas {

    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final DisponibilidadeMedicoRepository disponibilidadeRepository;
    private final ConvenioRepository convenioRepository;
    private final MedicoConvenioRepository medicoConvenioRepository;

    public AgendaDeConsultas(ConsultaRepository consultaRepository,
                             MedicoRepository medicoRepository,
                             PacienteRepository pacienteRepository,
                             DisponibilidadeMedicoRepository disponibilidadeRepository,
                             ConvenioRepository convenioRepository,
                             MedicoConvenioRepository medicoConvenioRepository) {
        this.consultaRepository = consultaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.convenioRepository = convenioRepository;
        this.medicoConvenioRepository = medicoConvenioRepository;
    }

    public DadosDetalhamentoConsulta agendar(DadosAgendamentoConsulta dados) {

        // 1. Validações de Pré-Requisitos (Inatividade, Existência)
        // Busca o Paciente e verifica a inatividade
        var paciente = pacienteRepository.findById(dados.idPaciente());
        if (paciente.isEmpty() || !paciente.get().isAtivo()) {
            throw new ValidacaoException("Paciente não existe ou está inativo no sistema!");
        }

        Medico medico = null; // Inicializa o médico fora do bloco

        // Se o médico for informado (opcional), verifica se existe e inatividade
        if (dados.idMedico() != null) {
            var medicoOptional = medicoRepository.findById(dados.idMedico());
            if (medicoOptional.isEmpty() || !medicoOptional.get().isAtivo()) {
                throw new ValidacaoException("Médico não existe ou está inativo no sistema!");
            }
            medico = medicoOptional.get();
        }

        // 2. Validações de Regras de Negócio
        var prioridade = dados.prioridade() != null ? dados.prioridade() : PrioridadeConsulta.ROTINA;
        validarHorarioFuncionamento(dados.data());
        validarAntecedenciaMinima(dados.data(), prioridade);

        Consulta consultaOrigem = null;
        if (dados.consultaOrigemId() != null) {
            consultaOrigem = validarRetorno(dados.consultaOrigemId(), dados.data());
        } else {
            validarPacienteComOutraConsultaNoDia(dados);
        }

        // Escolhe o médico (se não foi informado, será aleatório)
        if (medico == null) {
            medico = escolherMedicoAleatorio(dados);
        } else {
            validarDisponibilidadeMedico(medico.getId(), dados.data());
            validarMedicoOcupado(medico.getId(), dados.data());
        }

        if (medico == null) {
            throw new ValidacaoException("Não existe médico disponível na data e horário informados!");
        }

        // 3. Persistência
        var consulta = consultaOrigem != null
                ? new Consulta(medico, paciente.get(), dados.data(), prioridade, consultaOrigem)
                : new Consulta(medico, paciente.get(), dados.data(), prioridade);

        if (dados.convenioId() != null) {
            Convenio convenio = convenioRepository.findById(dados.convenioId())
                    .orElseThrow(() -> new ValidacaoException("Convênio informado não encontrado."));
            if (!convenio.isAtivo()) {
                throw new ValidacaoException("Convênio informado está inativo.");
            }
            if (!medicoConvenioRepository.existsByMedicoIdAndConvenioIdAndAtivoTrue(medico.getId(), dados.convenioId())) {
                throw new ValidacaoException("Médico não aceita o convênio informado.");
            }
            consulta.setConvenio(convenio);
        }

        consultaRepository.save(consulta);

        // 4. Retorno
        return new DadosDetalhamentoConsulta(consulta);
    }

    // ------------------ MÉTODO DE CANCELAMENTO ------------------

    public void cancelar(DadosCancelamentoConsulta dados) {
        var consulta = consultaRepository.findById(dados.idConsulta())
                .orElseThrow(() -> new ValidacaoException("ID da consulta informado não existe!"));

        if (consulta.getDataHora().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new ValidacaoException("Consulta somente pode ser cancelada com antecedência mínima de 24 horas.");
        }

        consulta.cancelar(dados.motivo(), dados.canceladoPor());
    }


    // ------------------ MÉTODOS DE VALIDAÇÃO DE REGRA DE NEGÓCIO ------------------

    private void validarHorarioFuncionamento(LocalDateTime data) {
        // ... (lógica de Horário de Funcionamento - Sem alteração) ...
        var diaDaSemana = data.getDayOfWeek();
        var hora = data.getHour();

        // 1. Horário de funcionamento: Seg a Sáb, das 07:00 às 19:00
        if (diaDaSemana == DayOfWeek.SUNDAY) {
            throw new ValidacaoException("Clínica não funciona aos domingos.");
        }

        if (hora < 7 || hora >= 19) {
            throw new ValidacaoException("Horário de agendamento fora do funcionamento da clínica.");
        }
    }

    private void validarAntecedenciaMinima(LocalDateTime data, PrioridadeConsulta prioridade) {
        if (prioridade == PrioridadeConsulta.URGENCIA) {
            return;
        }
        var agora = LocalDateTime.now();
        int minutosMinimos = prioridade == PrioridadeConsulta.PRIORITARIO ? 10 : 30;
        if (data.isBefore(agora.plusMinutes(minutosMinimos))) {
            throw new ValidacaoException(
                    "Consulta deve ser agendada com antecedência mínima de " + minutosMinimos + " minutos.");
        }
    }

    private void validarPacienteComOutraConsultaNoDia(DadosAgendamentoConsulta dados) {
        var primeiroHorario = dados.data().withHour(7).withMinute(0);
        var ultimoHorario = dados.data().withHour(19).withMinute(0);

        // CORREÇÃO: Usando o método do Repository com o sufixo AndAtivoTrue
        if (consultaRepository.existsByPacienteIdAndDataHoraBetweenAndAtivoTrue(dados.idPaciente(), primeiroHorario, ultimoHorario)) {
            throw new ValidacaoException("Paciente já possui uma consulta agendada neste dia.");
        }
    }

    private void validarMedicoOcupado(Long idMedico, LocalDateTime dataHora) {
        // Valida se o médico está ocupado no horário (regra 7)
        if (consultaRepository.existsByMedicoIdAndDataHoraAndAtivoTrue(idMedico, dataHora)) {
            throw new ValidacaoException("Médico já possui outra consulta ATIVA agendada neste horário.");
        }
    }

    private void validarDisponibilidadeMedico(long medicoId, LocalDateTime dataHora) {
        var diaSemana = dataHora.getDayOfWeek();
        var hora = dataHora.toLocalTime();
        boolean temDisponibilidade = disponibilidadeRepository
                .existsByMedicoIdAndDiaSemanaAndHoraInicioLessThanEqualAndHoraFimGreaterThanAndAtivoTrue(
                        medicoId, diaSemana, hora, hora);
        if (!temDisponibilidade) {
            throw new ValidacaoException("Médico não possui disponibilidade cadastrada no dia/horário solicitado.");
        }
    }

    private Consulta validarRetorno(Long consultaOrigemId, LocalDateTime dataRetorno) {
        var origem = consultaRepository.findById(consultaOrigemId)
                .orElseThrow(() -> new ValidacaoException("Consulta de origem não encontrada!"));

        if (!origem.isAtivo()) {
            throw new ValidacaoException("A consulta de origem está cancelada e não permite retorno.");
        }

        if (dataRetorno.isAfter(origem.getDataHora().plusDays(30))) {
            throw new ValidacaoException("Retorno deve ocorrer em até 30 dias após a consulta original.");
        }

        if (consultaRepository.existsByConsultaOrigemId(consultaOrigemId)) {
            throw new ValidacaoException("Já existe um retorno agendado para esta consulta.");
        }

        return origem;
    }

    private Medico escolherMedicoAleatorio(DadosAgendamentoConsulta dados) {
        var diaSemana = dados.data().getDayOfWeek().name();
        var hora = dados.data().toLocalTime();
        Optional<Medico> medicoOptional = medicoRepository.escolherMedicoAleatorioLivreNaData(
                dados.data(), diaSemana, hora);
        return medicoOptional.orElse(null);
    }

    // O método 'escolherMedico(DadosAgendamentoConsulta dados)' original foi dividido em:
    // 1. Lógica de checagem do médico no método 'agendar'
    // 2. Método auxiliar 'validarMedicoOcupado'
    // 3. Método auxiliar 'escolherMedicoAleatorio'
}