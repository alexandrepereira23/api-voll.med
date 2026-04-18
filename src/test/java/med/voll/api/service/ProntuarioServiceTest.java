package med.voll.api.service;

import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.consulta.ConsultaRepository;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.Paciente;
import med.voll.api.domain.prontuario.*;
import med.voll.api.domain.usuario.Perfil;
import med.voll.api.domain.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProntuarioServiceTest {

    @Mock ProntuarioRepository prontuarioRepository;
    @Mock ConsultaRepository consultaRepository;
    @Mock MedicoRepository medicoRepository;

    @InjectMocks ProntuarioService prontuarioService;

    // ── helpers ───────────────────────────────────────────────────────────────

    private Usuario usuarioMedico() {
        return new Usuario(1L, "medico@test.com", "senha", Perfil.ROLE_MEDICO, null);
    }

    private Medico medicoMock(long id) {
        var m = mock(Medico.class);
        when(m.getId()).thenReturn(id);
        return m;
    }

    private Consulta consultaAtiva(Medico medico) {
        var c = mock(Consulta.class);
        when(c.isAtivo()).thenReturn(true);
        when(c.getMedico()).thenReturn(medico);
        when(c.getPaciente()).thenReturn(mock(Paciente.class));
        return c;
    }

    private DadosCadastroProntuario dadosCadastro() {
        return new DadosCadastroProntuario(1L, "Anamnese", "Diagnóstico", "G43", "Observações");
    }

    // ── criar ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deve lançar 400 quando consulta está cancelada ao criar prontuário")
    void deveLancar400QuandoConsultaCancelada() {
        var consulta = mock(Consulta.class);
        when(consulta.isAtivo()).thenReturn(false);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> prontuarioService.criar(dadosCadastro(), usuarioMedico()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("deve lançar 409 quando já existe prontuário para a consulta")
    void deveLancar409QuandoProntuarioJaExiste() {
        var medico = medicoMock(1L);
        var consulta = consultaAtiva(medico);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(prontuarioRepository.existsByConsultaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> prontuarioService.criar(dadosCadastro(), usuarioMedico()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("deve lançar 403 quando médico logado não é o responsável pela consulta")
    void deveLancar403QuandoMedicoLogadoDiferente() {
        var medicoResponsavel = medicoMock(99L); // outro médico
        var medicoLogado = medicoMock(1L);
        var consulta = consultaAtiva(medicoResponsavel);
        var usuario = usuarioMedico();

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(prontuarioRepository.existsByConsultaId(1L)).thenReturn(false);
        when(medicoRepository.findByUsuario(usuario)).thenReturn(Optional.of(medicoLogado));

        assertThatThrownBy(() -> prontuarioService.criar(dadosCadastro(), usuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("deve criar prontuário com sucesso quando todos os dados são válidos")
    void deveCriarProntuarioComSucesso() {
        var medico = medicoMock(1L);
        var consulta = consultaAtiva(medico);
        var usuario = usuarioMedico();

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(prontuarioRepository.existsByConsultaId(1L)).thenReturn(false);
        when(medicoRepository.findByUsuario(usuario)).thenReturn(Optional.of(medico));
        when(prontuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var resultado = prontuarioService.criar(dadosCadastro(), usuario);

        assertThat(resultado).isNotNull();
        verify(prontuarioRepository).save(any(Prontuario.class));
    }

    // ── atualizar ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deve lançar 422 ao tentar editar prontuário fora da janela de 24h")
    void deveLancar422QuandoJanelaExpirada() {
        var medico = medicoMock(1L);
        var prontuario = mock(Prontuario.class);
        var usuario = usuarioMedico();

        when(prontuario.isAtivo()).thenReturn(true);
        when(prontuario.getMedico()).thenReturn(medico);
        when(prontuario.podeEditar()).thenReturn(false);
        when(prontuarioRepository.findById(1L)).thenReturn(Optional.of(prontuario));
        when(medicoRepository.findByUsuario(usuario)).thenReturn(Optional.of(medico));

        var dados = new DadosAtualizacaoProntuario(1L, "nova anamnese", null, null, null);

        assertThatThrownBy(() -> prontuarioService.atualizar(dados, usuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    @DisplayName("deve lançar 403 ao tentar editar prontuário de outro médico")
    void deveLancar403AoEditarProntuarioDeOutroMedico() {
        var medicoResponsavel = medicoMock(99L);
        var medicoLogado = medicoMock(1L);
        var prontuario = mock(Prontuario.class);
        var usuario = usuarioMedico();

        when(prontuario.isAtivo()).thenReturn(true);
        when(prontuario.getMedico()).thenReturn(medicoResponsavel);
        when(prontuarioRepository.findById(1L)).thenReturn(Optional.of(prontuario));
        when(medicoRepository.findByUsuario(usuario)).thenReturn(Optional.of(medicoLogado));

        var dados = new DadosAtualizacaoProntuario(1L, "nova anamnese", null, null, null);

        assertThatThrownBy(() -> prontuarioService.atualizar(dados, usuario))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("deve lançar 404 ao tentar editar prontuário inexistente")
    void deveLancar404AoEditarProntuarioInexistente() {
        when(prontuarioRepository.findById(99L)).thenReturn(Optional.empty());

        var dados = new DadosAtualizacaoProntuario(99L, null, null, null, null);

        assertThatThrownBy(() -> prontuarioService.atualizar(dados, usuarioMedico()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
