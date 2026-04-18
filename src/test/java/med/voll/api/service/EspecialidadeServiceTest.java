package med.voll.api.service;

import med.voll.api.domain.medico.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialidadeServiceTest {

    @Mock EspecialidadeRepository especialidadeRepository;

    @InjectMocks EspecialidadeService especialidadeService;

    @Test
    @DisplayName("deve lançar 409 ao criar especialidade com nome duplicado (case-insensitive)")
    void deveLancar409QuandoNomeDuplicado() {
        when(especialidadeRepository.existsByNomeIgnoreCase("Cardiologia")).thenReturn(true);

        var dados = new DadosCadastroEspecialidade("Cardiologia");

        assertThatThrownBy(() -> especialidadeService.criar(dados))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("deve criar especialidade com sucesso quando nome é único")
    void deveCriarEspecialidadeComSucesso() {
        when(especialidadeRepository.existsByNomeIgnoreCase("Neurologia")).thenReturn(false);
        when(especialidadeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dados = new DadosCadastroEspecialidade("Neurologia");
        var resultado = especialidadeService.criar(dados);

        assertThat(resultado.nome()).isEqualTo("Neurologia");
        assertThat(resultado.ativo()).isTrue();
        verify(especialidadeRepository).save(any(EspecialidadeEntity.class));
    }

    @Test
    @DisplayName("deve lançar 404 ao detalhar especialidade inexistente")
    void deveLancar404AoDetalharInexistente() {
        when(especialidadeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> especialidadeService.detalhar(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("deve retornar detalhamento correto ao detalhar especialidade existente")
    void deveDetalharEspecialidadeComSucesso() {
        var esp = new EspecialidadeEntity("Ortopedia");
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(esp));

        var resultado = especialidadeService.detalhar(1L);

        assertThat(resultado.nome()).isEqualTo("Ortopedia");
    }

    @Test
    @DisplayName("deve lançar 404 ao atualizar especialidade inexistente")
    void deveLancar404AoAtualizarInexistente() {
        when(especialidadeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> especialidadeService.atualizar(99L, new DadosAtualizacaoEspecialidade("Novo")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("deve atualizar nome da especialidade com sucesso")
    void deveAtualizarEspecialidadeComSucesso() {
        var esp = new EspecialidadeEntity("Ortopedia");
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(esp));

        var resultado = especialidadeService.atualizar(1L, new DadosAtualizacaoEspecialidade("Ortopedia Geral"));

        assertThat(resultado.nome()).isEqualTo("Ortopedia Geral");
    }

    @Test
    @DisplayName("deve lançar 404 ao inativar especialidade inexistente")
    void deveLancar404AoInativarInexistente() {
        when(especialidadeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> especialidadeService.inativar(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("deve inativar especialidade com sucesso")
    void deveInativarEspecialidadeComSucesso() {
        var esp = new EspecialidadeEntity("Dermatologia");
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(esp));

        especialidadeService.inativar(1L);

        assertThat(esp.isAtivo()).isFalse();
    }
}
