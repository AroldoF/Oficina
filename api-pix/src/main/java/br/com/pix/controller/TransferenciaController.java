package br.com.pix.controller;

import br.com.pix.api.TransferenciaPixRequest;
import br.com.pix.api.TransferenciaPixResponse;
import br.com.pix.service.TransferenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pix")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping("/transferir")
    public ResponseEntity<TransferenciaPixResponse> transferir(@Valid @RequestBody TransferenciaPixRequest request) {
        return ResponseEntity.ok(transferenciaService.transferir(request));
    }
}
