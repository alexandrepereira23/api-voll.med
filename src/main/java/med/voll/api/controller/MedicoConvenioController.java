package med.voll.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import med.voll.api.domain.medico.*;
import med.voll.api.exception.ValidacaoException;
import med.voll.api.domain.convenio.ConvenioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("medicos/{medicoId}/convenios")
@Tag(name = "Médicos - Convênios", description = "Gerenciamento de convênios aceitos pelo médico")
public class MedicoConvenioController {

    private final MedicoRepository medicoRepository;
    private final ConvenioRepository convenioRepository;
    private final MedicoConvenioRepository medicoConvenioRepository;

    public MedicoConvenioController(MedicoRepository medicoRepository,
                                    ConvenioRepository convenioRepository,
                                    MedicoConvenioRepository medicoConvenioRepository) {
        this.medicoRepository = medicoRepository;
        this.convenioRepository = convenioRepository;
        this.medicoConvenioRepository = medicoConvenioRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FUNCIONARIO')")
    @Operation(summary = "Vincular convênio ao médico", description = "Associa um convênio à lista de planos aceitos pelo médico")
    public ResponseEntity<DadosDetalhamentoConvenioMedico> vincular(
            @PathVariable Long medicoId,
            @RequestBody @Valid DadosVinculoConvenioMedico dados,
            UriComponentsBuilder uriBuilder) {

        var medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado"));
        if (!medico.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado");
        }

        var convenio = convenioRepository.findById(dados.convenioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Convênio não encontrado"));
        if (!convenio.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Convênio não encontrado");
        }

        if (medicoConvenioRepository.existsByMedicoIdAndConvenioIdAndAtivoTrue(medicoId, dados.convenioId())) {
            throw new ValidacaoException("Médico já está vinculado a este convênio");
        }

        var vinculo = new MedicoConvenio(medico, convenio);
        medicoConvenioRepository.save(vinculo);

        var uri = uriBuilder.path("/medicos/{medicoId}/convenios/{id}")
                .buildAndExpand(medicoId, vinculo.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoConvenioMedico(vinculo));
    }

    @GetMapping
    @Operation(summary = "Listar convênios do médico", description = "Lista todos os convênios ativos aceitos pelo médico")
    public ResponseEntity<List<DadosDetalhamentoConvenioMedico>> listar(@PathVariable Long medicoId) {
        var vinculos = medicoConvenioRepository.findAllByMedicoIdAndAtivoTrue(medicoId)
                .stream()
                .map(DadosDetalhamentoConvenioMedico::new)
                .toList();
        return ResponseEntity.ok(vinculos);
    }

    @DeleteMapping("/{convenioId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_FUNCIONARIO')")
    @Operation(summary = "Desvincular convênio do médico", description = "Remove a associação entre o médico e o convênio")
    public ResponseEntity<Void> desvincular(
            @PathVariable Long medicoId,
            @PathVariable Long convenioId) {

        var vinculo = medicoConvenioRepository.findByMedicoIdAndConvenioId(medicoId, convenioId)
                .filter(MedicoConvenio::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vínculo não encontrado"));
        vinculo.desvincular();
        return ResponseEntity.noContent().build();
    }
}
