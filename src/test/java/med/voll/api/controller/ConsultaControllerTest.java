package med.voll.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import med.voll.api.domain.consulta.*;
import med.voll.api.domain.usuario.Perfil;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import med.voll.api.infra.security.TokenService;
import med.voll.api.service.ConsultaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import med.voll.api.config.MethodSecurityTestConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultaController.class)
@Import(MethodSecurityTestConfig.class)
class ConsultaControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ConsultaService consultaService;
    @MockBean TokenService tokenService;
    @MockBean UsuarioRepository usuarioRepository;
    @MockBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // ── helpers ───────────────────────────────────────────────────────────────

    private Usuario usuarioFuncionario() {
        return new Usuario(1L, "func@test.com", "senha", Perfil.ROLE_FUNCIONARIO, null);
    }

    private Usuario usuarioMedico() {
        return new Usuario(2L, "medico@test.com", "senha", Perfil.ROLE_MEDICO, null);
    }

    private LocalDateTime dataValida() {
        return LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    private DadosAgendamentoConsulta dadosAgendamento() {
        return new DadosAgendamentoConsulta(1L, 1L, dataValida(), PrioridadeConsulta.ROTINA, null, null);
    }

    private DadosDetalhamentoConsulta detalhamento() {
        return new DadosDetalhamentoConsulta(1L, 1L, 1L, dataValida(), PrioridadeConsulta.ROTINA, TipoConsulta.NORMAL, null, null);
    }

    // ── testes ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ROLE_FUNCIONARIO deve agendar consulta e receber 200")
    void deveAgendarConsultaComFuncionario() throws Exception {
        when(consultaService.agendar(any())).thenReturn(detalhamento());

        mvc.perform(post("/consultas")
                        .with(user(usuarioFuncionario())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosAgendamento())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("ROLE_MEDICO não deve agendar consulta — deve receber 403")
    void naoDeveAgendarConsultaComRoleMedico() throws Exception {
        mvc.perform(post("/consultas")
                        .with(user(usuarioMedico())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosAgendamento())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve listar consultas com sucesso para qualquer usuário autenticado")
    void deveListarConsultas() throws Exception {
        when(consultaService.listarAtivas(any(Pageable.class), any(Usuario.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/consultas")
                        .with(user(usuarioFuncionario())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ROLE_MEDICO deve listar apenas suas consultas")
    void medicoDeveListarApenasPropriasSultas() throws Exception {
        when(consultaService.listarAtivas(any(Pageable.class), any(Usuario.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/consultas")
                        .with(user(usuarioMedico())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ROLE_FUNCIONARIO deve cancelar consulta e receber 204")
    void deveCancelarConsultaComFuncionario() throws Exception {
        var dados = new DadosCancelamentoConsulta(1L, MotivoCancelamento.PACIENTE_DESISTIU, CanceladoPor.PACIENTE);

        mvc.perform(delete("/consultas")
                        .with(user(usuarioFuncionario())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dados)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("ROLE_MEDICO não deve cancelar consulta — deve receber 403")
    void naoDeveCancelarConsultaComRoleMedico() throws Exception {
        var dados = new DadosCancelamentoConsulta(1L, MotivoCancelamento.PACIENTE_DESISTIU, null);

        mvc.perform(delete("/consultas")
                        .with(user(usuarioMedico())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dados)))
                .andExpect(status().isForbidden());
    }
}
