package med.voll.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import med.voll.api.domain.medico.*;
import med.voll.api.domain.usuario.Perfil;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import med.voll.api.infra.security.TokenService;
import med.voll.api.service.EspecialidadeService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EspecialidadeController.class)
@Import(MethodSecurityTestConfig.class)
class EspecialidadeControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean EspecialidadeService especialidadeService;
    @MockBean TokenService tokenService;
    @MockBean UsuarioRepository usuarioRepository;
    @MockBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // ── helpers ───────────────────────────────────────────────────────────────

    private Usuario usuarioAdmin() {
        return new Usuario(1L, "admin@test.com", "senha", Perfil.ROLE_ADMIN, null);
    }

    private Usuario usuarioFuncionario() {
        return new Usuario(2L, "func@test.com", "senha", Perfil.ROLE_FUNCIONARIO, null);
    }

    private DadosDetalhamentoEspecialidade detalhamento(String nome) {
        return new DadosDetalhamentoEspecialidade(1L, nome, true);
    }

    // ── testes ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ROLE_ADMIN deve cadastrar especialidade e receber 201")
    void deveCadastrarEspecialidadeComAdmin() throws Exception {
        when(especialidadeService.criar(any())).thenReturn(detalhamento("Neurologia"));

        mvc.perform(post("/especialidades")
                        .with(user(usuarioAdmin())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DadosCadastroEspecialidade("Neurologia"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Neurologia"));
    }

    @Test
    @DisplayName("ROLE_FUNCIONARIO não deve cadastrar especialidade — deve receber 403")
    void naoDeveCadastrarEspecialidadeComFuncionario() throws Exception {
        mvc.perform(post("/especialidades")
                        .with(user(usuarioFuncionario())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DadosCadastroEspecialidade("Neurologia"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve listar especialidades para qualquer usuário autenticado")
    void deveListarEspecialidades() throws Exception {
        when(especialidadeService.listar(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new DadosListagemEspecialidade(1L, "Cardiologia"))));

        mvc.perform(get("/especialidades")
                        .with(user(usuarioFuncionario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Cardiologia"));
    }

    @Test
    @DisplayName("deve detalhar especialidade para qualquer usuário autenticado")
    void deveDetalharEspecialidade() throws Exception {
        when(especialidadeService.detalhar(1L)).thenReturn(detalhamento("Cardiologia"));

        mvc.perform(get("/especialidades/1")
                        .with(user(usuarioFuncionario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cardiologia"));
    }

    @Test
    @DisplayName("ROLE_ADMIN deve atualizar especialidade e receber 200")
    void deveAtualizarEspecialidadeComAdmin() throws Exception {
        when(especialidadeService.atualizar(anyLong(), any())).thenReturn(detalhamento("Cardiologia Intervencionista"));

        mvc.perform(put("/especialidades/1")
                        .with(user(usuarioAdmin())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DadosAtualizacaoEspecialidade("Cardiologia Intervencionista"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cardiologia Intervencionista"));
    }

    @Test
    @DisplayName("ROLE_FUNCIONARIO não deve atualizar especialidade — deve receber 403")
    void naoDeveAtualizarEspecialidadeComFuncionario() throws Exception {
        mvc.perform(put("/especialidades/1")
                        .with(user(usuarioFuncionario())).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DadosAtualizacaoEspecialidade("Nome"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_ADMIN deve inativar especialidade e receber 204")
    void deveInativarEspecialidadeComAdmin() throws Exception {
        mvc.perform(delete("/especialidades/1")
                        .with(user(usuarioAdmin())).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("ROLE_FUNCIONARIO não deve inativar especialidade — deve receber 403")
    void naoDeveInativarEspecialidadeComFuncionario() throws Exception {
        mvc.perform(delete("/especialidades/1")
                        .with(user(usuarioFuncionario())).with(csrf()))
                .andExpect(status().isForbidden());
    }
}
