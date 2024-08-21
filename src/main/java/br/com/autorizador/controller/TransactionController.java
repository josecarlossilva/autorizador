package br.com.autorizador.controller;

import br.com.autorizador.model.Transaction;
import br.com.autorizador.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    /**
     *
     * - @ExceptionHandler(Exception.class): Esta anotação especifica que o método handleError
     *   deve tratar todas as exceções do tipo Exception lançadas por qualquer método no controlador.
     *
     * - @ResponseStatus(HttpStatus.OK): Esta anotação define o código de status HTTP da resposta como
     *   200 OK quando uma exceção é tratada por este método para fins de seguranca.
     *
     */

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Map<String, String>> handleError(Exception ex) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("code", "07");
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}