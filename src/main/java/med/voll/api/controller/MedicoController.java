package med.voll.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import med.voll.api.domain.medico.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("medicos")
@Tag(name = "Médicos", description = "Endpoints para gerenciamento de médicos")
public class MedicoController {

    @Autowired
    private MedicoRepository medicoRepository;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar médico", description = "Cria um novo médico no sistema")
    public  ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroMedico dados, UriComponentsBuilder uriBuilder) {
        var medico = new Medico(dados);
        medicoRepository.save(medico);

        var uri =uriBuilder.path("/medicos/{id}").buildAndExpand(medico.getId()).toUri();

        return ResponseEntity.created(uri).body(new DadosDetalhamentoMedico(medico));
    }

    @GetMapping
    @Operation(summary = "Listar médicos", description = "Lista médicos ativos com paginação")
    public ResponseEntity<Page<DadosListagemMedico>> listar(@PageableDefault(size = 10) Pageable paginacao) {
        var page = medicoRepository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
        return ResponseEntity.ok(page);
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar médico", description = "Atualiza os dados de um médico")
    public ResponseEntity atualizar(@RequestBody @Valid DadosAtualizacaoMedico dados) {
        var medico = medicoRepository.getReferenceById(dados.id());
        medico.atualizarInformacoes(dados);
        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Excluir médico", description = "Realiza exclusão lógica (soft delete) de um médico")
    public ResponseEntity deletar(@PathVariable long id) {
        var medico = medicoRepository.getReferenceById(id);
        medico.excluir();
        return  ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar médico", description = "Retorna os detalhes de um médico específico")
    public ResponseEntity detalhar(@PathVariable long id) {
        var medico = medicoRepository.getReferenceById(id);
        return  ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

}
