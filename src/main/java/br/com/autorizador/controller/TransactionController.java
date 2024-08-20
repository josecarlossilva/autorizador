package br.com.autorizador.controller;

import br.com.autorizador.model.Transaction;
import br.com.autorizador.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private AuthorizationService authorizationService;

    @PostMapping("/authorize")
    public ResponseEntity<String> authorizeTransaction(@RequestBody Transaction transaction) {
        String responseCodeJson = authorizationService.authorizeTransaction(transaction);
        return new ResponseEntity<>(responseCodeJson, HttpStatus.OK);
    }
}