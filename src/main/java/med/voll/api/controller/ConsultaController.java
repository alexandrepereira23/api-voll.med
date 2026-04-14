package med.voll.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import med.voll.api.domain.consulta.*;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.service.ConsultaService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("consultas")
@Tag(name = "Consultas", description = "Endpoints para gerenciamento de consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_FUNCIONARIO')")
    @Operation(summary = "Agendar consulta", description = "Agenda uma nova consulta no sistema")
    public ResponseEntity<DadosDetalhamentoConsulta> agendar(@RequestBody @Valid DadosAgendamentoConsulta dados) {
        var detalhamento = consultaService.agendar(dados);
        return ResponseEntity.ok(detalhamento);
    }

    @GetMapping
    @Operation(summary = "Listar consultas", description = "Lista consultas ativas com paginação")
    public ResponseEntity<Page<DadosListagemConsulta>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = {"dataHora"}) Pageable paginacao,
            @AuthenticationPrincipal Usuario usuario
    ) {
        var page = consultaService.listarAtivas(paginacao, usuario);
        return ResponseEntity.ok(page);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_FUNCIONARIO')")
    @Operation(summary = "Cancelar consulta", description = "Cancela uma consulta agendada")
    public ResponseEntity<Void> cancelar(@RequestBody @Valid DadosCancelamentoConsulta dados) {
        consultaService.cancelar(dados);
        return ResponseEntity.noContent().build();
    }
}
