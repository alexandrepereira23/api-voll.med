package med.voll.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.endereco.Endereco;
import med.voll.api.domain.medico.*;
import med.voll.api.domain.usuario.Perfil;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import med.voll.api.infra.security.TokenService;
import med.voll.api.service.MedicoService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicoController.class)
@Import(MethodSecurityTestConfig.class)
class MedicoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean MedicoService medicoService;
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

    private DadosCadastroMedico dadosCadastro() {
        return new DadosCadastroMedico(
                "Dr. João Silva",
                "joao@voll.med",
                "11999999999",
                "123456",
                1L,
                new DadosEndereco("Rua das Flores", "Centro", "01310100", "São Paulo", "SP", null, null)
        );
    }

    private DadosDetalhamentoMedico detalhamento() {
        var endereco = new Endereco("Rua das Flores", "Centro", "01310100", "São Paulo", "SP", null, null);
        return new DadosDetalhamentoMedico(1L, "Dr. João Silva", "joao@voll.med", "123456", "11999999999", "CARDIOLOGIA", endereco);
    }

    // ── testes ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ROLE_FUNCIONARIO deve cadastrar médico e receber 201")
    void deveCadastrarMedicoComFuncionario() throws Exception {
        when(medicoService.cadastrar(any())).thenReturn(detalhamento());

        mvc.perform(post("/medicos")
                        .with(user(usuarioFuncionario())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosCadastro())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Dr. João Silva"));
    }

    @Test
    @DisplayName("ROLE_MEDICO não deve cadastrar médico — deve receber 403")
    void naoDeveCadastrarMedicoComRoleMedico() throws Exception {
        mvc.perform(post("/medicos")
                        .with(user(usuarioMedico())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosCadastro())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve listar médicos com sucesso para qualquer usuário autenticado")
    void deveListarMedicos() throws Exception {
        when(medicoService.listar(any(Pageable.class), any(Usuario.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/medicos")
                        .with(user(usuarioFuncionario())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deve detalhar médico com sucesso")
    void deveDetalharMedico() throws Exception {
        when(medicoService.detalhar(1L)).thenReturn(detalhamento());

        mvc.perform(get("/medicos/1")
                        .with(user(usuarioFuncionario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.crm").value("123456"));
    }

    @Test
    @DisplayName("deve excluir médico com ROLE_FUNCIONARIO e receber 204")
    void deveExcluirMedicoComFuncionario() throws Exception {
        mvc.perform(delete("/medicos/1")
                        .with(user(usuarioFuncionario())).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deve rejeitar requisição não autenticada com 401")
    void deveRejeitarRequisicaoNaoAutenticada() throws Exception {
        mvc.perform(get("/medicos"))
                .andExpect(status().isUnauthorized());
    }
}
