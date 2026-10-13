package br.com.pix.controller;

import br.com.pix.api.TransferenciaPixRequest;
import br.com.pix.api.TransferenciaPixResponse;
import br.com.pix.service.TransferenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pix")
@Tag(name = "Transferências PIX", description = "Operações de transferência entre contas")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping("/transferir")
    @Operation(summary = "Realizar transferência PIX")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transferência concluída"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio não atendida",
                    content = @Content(schema = @Schema(implementation = br.com.pix.exception.ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Conta ou chave PIX não encontrada",
                    content = @Content(schema = @Schema(implementation = br.com.pix.exception.ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Valor acima do limite por transação",
                    content = @Content(schema = @Schema(implementation = br.com.pix.exception.ApiError.class)))
    })
    public ResponseEntity<TransferenciaPixResponse> transferir(@Valid @RequestBody TransferenciaPixRequest request) {
        return ResponseEntity.ok(transferenciaService.transferir(request));
    }
}
