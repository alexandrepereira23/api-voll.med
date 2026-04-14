package med.voll.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import med.voll.api.domain.paciente.*;
import med.voll.api.service.PacienteService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("pacientes")
@Tag(name = "Pacientes", description = "Endpoints para gerenciamento de pacientes")
public class PacientesController {

    private final PacienteService pacienteService;

    public PacientesController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_FUNCIONARIO')")
    @Operation(summary = "Cadastrar paciente", description = "Cria um novo paciente no sistema")
    public ResponseEntity<DadosDetalhamentoPaciente> cadastrar(@RequestBody @Valid DadosCadastroPaciente dadosCadastroPaciente, UriComponentsBuilder uriBuilder) {
        var paciente = pacienteService.cadastrar(dadosCadastroPaciente);
        var uri = uriBuilder.path("/pacientes/{id}").buildAndExpand(paciente.id()).toUri();
        return ResponseEntity.created(uri).body(paciente);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_FUNCIONARIO')")
    @Operation(summary = "Listar pacientes", description = "Lista pacientes ativos com paginação")
    public ResponseEntity<Page<DadosListagemPaciente>> listar(
            @ParameterObject @PageableDefault(size = 10) Pageable paginacao) {
        var page = pacienteService.listarAtivos(paginacao);
        return  ResponseEntity.ok(page);
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_FUNCIONARIO')")
    @Operation(summary = "Atualizar paciente", description = "Atualiza os dados de um paciente")
    public ResponseEntity<DadosDetalhamentoPaciente> atualizar(@RequestBody @Valid DadosAtualizacaoPaciente dados){
        var paciente = pacienteService.atualizar(dados);
        return ResponseEntity.ok(paciente);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_FUNCIONARIO')")
    @Operation(summary = "Excluir paciente", description = "Realiza exclusão lógica (soft delete) de um paciente")
    public ResponseEntity<Void> excluir(@PathVariable Long id){
        pacienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar paciente", description = "Retorna os detalhes de um paciente específico")
    public ResponseEntity<DadosDetalhamentoPaciente> detalhar(@PathVariable Long id) {
        var paciente = pacienteService.detalhar(id);
        return  ResponseEntity.ok(paciente);
    }


}
