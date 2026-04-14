package med.voll.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import med.voll.api.domain.prontuario.*;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.service.ProntuarioService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("prontuarios")
@Tag(name = "Prontuários", description = "Endpoints para gerenciamento de prontuários eletrônicos")
public class ProntuarioController {

    private final ProntuarioService prontuarioService;

    public ProntuarioController(ProntuarioService prontuarioService) {
        this.prontuarioService = prontuarioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MEDICO')")
    @Operation(summary = "Criar prontuário", description = "Cria um prontuário eletrônico vinculado a uma consulta realizada")
    public ResponseEntity<DadosDetalhamentoProntuario> criar(
            @RequestBody @Valid DadosCadastroProntuario dados,
            @AuthenticationPrincipal Usuario usuario,
            UriComponentsBuilder uriBuilder
    ) {
        var prontuario = prontuarioService.criar(dados, usuario);
        var uri = uriBuilder.path("/prontuarios/{id}").buildAndExpand(prontuario.id()).toUri();
        return ResponseEntity.created(uri).body(prontuario);
    }

    @GetMapping
    @Operation(summary = "Listar prontuários", description = "Lista prontuários ativos. Médico vê apenas os seus; Admin e Funcionário veem todos")
    public ResponseEntity<Page<DadosListagemProntuario>> listar(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(prontuarioService.listar(pageable, usuario));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar prontuário", description = "Retorna os dados completos de um prontuário")
    public ResponseEntity<DadosDetalhamentoProntuario> detalhar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(prontuarioService.detalhar(id, usuario));
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Histórico do paciente", description = "Lista o histórico clínico de um paciente")
    public ResponseEntity<Page<DadosListagemProntuario>> listarPorPaciente(
            @PathVariable Long pacienteId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(prontuarioService.listarPorPaciente(pacienteId, pageable, usuario));
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_MEDICO')")
    @Operation(summary = "Atualizar prontuário", description = "Atualiza um prontuário dentro da janela de 24 horas após o registro")
    public ResponseEntity<DadosDetalhamentoProntuario> atualizar(
            @RequestBody @Valid DadosAtualizacaoProntuario dados,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(prontuarioService.atualizar(dados, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Inativar prontuário", description = "Realiza a exclusão lógica de um prontuário (somente Admin)")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        prontuarioService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
