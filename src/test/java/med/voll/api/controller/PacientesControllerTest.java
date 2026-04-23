package med.voll.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import med.voll.api.config.MethodSecurityTestConfig;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.endereco.Endereco;
import med.voll.api.domain.paciente.*;
import med.voll.api.domain.usuario.Perfil;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import med.voll.api.infra.security.TokenService;
import med.voll.api.service.PacienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PacientesController.class)
@Import(MethodSecurityTestConfig.class)
class PacientesControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean PacienteService pacienteService;
    @MockBean TokenService tokenService;
    @MockBean UsuarioRepository usuarioRepository;
    @MockBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private Usuario usuarioFuncionario() {
        return new Usuario(1L, "func@test.com", "senha", Perfil.ROLE_FUNCIONARIO, null);
    }

    private Usuario usuarioMedico() {
        return new Usuario(2L, "medico@test.com", "senha", Perfil.ROLE_MEDICO, null);
    }

    private DadosCadastroPaciente dadosCadastro() {
        var endereco = new DadosEndereco("Rua A", "Bairro B", "01310100", "São Paulo", "SP", null, null);
        return new DadosCadastroPaciente("Maria Silva", "maria@email.com", "11999999999", "12345678901", endereco);
    }

    private DadosDetalhamentoPaciente detalhamento() {
        var endereco = new Endereco("Rua A", "Bairro B", "01310100", "São Paulo", "SP", null, null);
        return new DadosDetalhamentoPaciente(1L, "Maria Silva", "maria@email.com", "11999999999", "12345678901", endereco, true);
    }

    @Test
    @DisplayName("ROLE_FUNCIONARIO deve cadastrar paciente e receber 201")
    void deveCadastrarPacienteComFuncionario() throws Exception {
        when(pacienteService.cadastrar(any())).thenReturn(detalhamento());

        mvc.perform(post("/pacientes")
                        .with(user(usuarioFuncionario())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosCadastro())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    @DisplayName("ROLE_MEDICO não deve cadastrar paciente — deve receber 403")
    void naoDeveCadastrarPacienteComRoleMedico() throws Exception {
        mvc.perform(post("/pacientes")
                        .with(user(usuarioMedico())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosCadastro())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve listar pacientes com ROLE_FUNCIONARIO e receber 200")
    void deveListarPacientesComFuncionario() throws Exception {
        when(pacienteService.listarAtivos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/pacientes")
                        .with(user(usuarioFuncionario())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ROLE_MEDICO não deve listar pacientes — deve receber 403")
    void naoDeveListarPacientesComRoleMedico() throws Exception {
        mvc.perform(get("/pacientes")
                        .with(user(usuarioMedico())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve detalhar paciente autenticado com qualquer perfil")
    void deveDetalharPaciente() throws Exception {
        when(pacienteService.detalhar(1L)).thenReturn(detalhamento());

        mvc.perform(get("/pacientes/1")
                        .with(user(usuarioMedico())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678901"));
    }

    @Test
    @DisplayName("ROLE_FUNCIONARIO deve atualizar paciente e receber 200")
    void deveAtualizarPacienteComFuncionario() throws Exception {
        when(pacienteService.atualizar(any())).thenReturn(detalhamento());

        var dados = new DadosAtualizacaoPaciente(1L, "Maria S.", null, null);

        mvc.perform(put("/pacientes")
                        .with(user(usuarioFuncionario())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dados)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ROLE_FUNCIONARIO deve excluir paciente e receber 204")
    void deveExcluirPacienteComFuncionario() throws Exception {
        mvc.perform(delete("/pacientes/1")
                        .with(user(usuarioFuncionario())).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deve rejeitar requisição não autenticada com 401")
    void deveRejeitarRequisicaoNaoAutenticada() throws Exception {
        mvc.perform(get("/pacientes"))
                .andExpect(status().isUnauthorized());
    }
}
