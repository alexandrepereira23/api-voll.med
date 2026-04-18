package med.voll.api.service;

import med.voll.api.domain.ia.DadosRespostaIa;
import med.voll.api.domain.prontuario.Prontuario;
import med.voll.api.domain.prontuario.ProntuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IaServiceTest {

    @Mock ProntuarioRepository prontuarioRepository;
    @Mock RestClient restClient;
    @Mock RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock RestClient.RequestBodySpec requestBodySpec;
    @Mock RestClient.ResponseSpec responseSpec;

    IaService iaService;

    @BeforeEach
    void setUp() {
        iaService = new IaService(restClient, prontuarioRepository);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void configurarRestClientMock(String textoResposta) {
        Map<Object, Object> responseBody = new HashMap<>();
        responseBody.put("content", List.of(Map.of("text", textoResposta)));

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn((Map) responseBody).when(responseSpec).body(Map.class);
    }

    @Test
    @DisplayName("resumirHistorico deve retornar mensagem padrão quando não há prontuários")
    void deveRetornarMensagemPadraoQuandoNaoProntuarios() {
        when(prontuarioRepository.findAllByAtivoTrueAndPacienteId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        DadosRespostaIa resultado = iaService.resumirHistorico(1L);

        assertThat(resultado.resposta()).contains("Nenhum prontuário encontrado");
        verify(restClient, never()).post();
    }

    @Test
    @DisplayName("gerarPreDiagnostico deve chamar a API e retornar a resposta da IA")
    void deveGerarPreDiagnostico() {
        configurarRestClientMock("Hipóteses: gripe, COVID-19");

        var resultado = iaService.gerarPreDiagnostico(1L, "febre, tosse seca");

        assertThat(resultado.resposta()).isEqualTo("Hipóteses: gripe, COVID-19");
        verify(restClient).post();
    }

    @Test
    @DisplayName("gerarLaudo deve chamar a API e retornar laudo estruturado")
    void deveGerarLaudo() {
        configurarRestClientMock("Laudo clínico estruturado");

        var resultado = iaService.gerarLaudo(1L, "Paciente com cefaleia intensa");

        assertThat(resultado.resposta()).isEqualTo("Laudo clínico estruturado");
        verify(restClient).post();
    }

    @Test
    @DisplayName("resumirHistorico deve chamar a API quando há prontuários")
    void deveResumirHistoricoComProntuarios() {
        configurarRestClientMock("Resumo do histórico clínico");

        var prontuario = mock(Prontuario.class);
        when(prontuario.getDataRegistro()).thenReturn(LocalDateTime.now().minusDays(10));
        when(prontuario.getDiagnostico()).thenReturn("Gripe");
        when(prontuario.getCid10()).thenReturn("J11");
        when(prontuario.getAnamnese()).thenReturn("Febre e tosse");
        when(prontuario.getObservacoes()).thenReturn("Repouso");

        var page = new PageImpl<>(List.of(prontuario));
        when(prontuarioRepository.findAllByAtivoTrueAndPacienteId(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        var resultado = iaService.resumirHistorico(1L);

        assertThat(resultado.resposta()).isEqualTo("Resumo do histórico clínico");
        verify(restClient).post();
    }
}
