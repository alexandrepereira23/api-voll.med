package med.voll.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import med.voll.api.domain.consulta.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("consultas")
@Tag(name = "Consultas", description = "Endpoints para gerenciamento de consultas")
public class ConsultaController {

    @Autowired
    private AgendaDeConsultas agenda;

    @Autowired
    private ConsultaRepository consultaRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Agendar consulta", description = "Agenda uma nova consulta no sistema")
    public ResponseEntity<DadosDetalhamentoConsulta> agendar(@RequestBody @Valid DadosAgendamentoConsulta dados) {
        DadosDetalhamentoConsulta detalhamento = agenda.agendar(dados);
        return ResponseEntity.ok(detalhamento);
    }

    @GetMapping
    @Operation(summary = "Listar consultas", description = "Lista consultas ativas com paginação")
    public ResponseEntity<Page<DadosListagemConsulta>> listar(
            @PageableDefault(size = 10, sort = {"dataHora"}) Pageable paginacao
    ) {
        Page<DadosListagemConsulta> page = consultaRepository
                .findAllByAtivoTrue(paginacao)
                .map(DadosListagemConsulta::new);

        return ResponseEntity.ok(page);
    }

    @DeleteMapping
    @Transactional
    @Operation(summary = "Cancelar consulta", description = "Cancela uma consulta agendada")
    public ResponseEntity<Void> cancelar(@RequestBody @Valid DadosCancelamentoConsulta dados) {
        agenda.cancelar(dados);
        return ResponseEntity.noContent().build();
    }
}
