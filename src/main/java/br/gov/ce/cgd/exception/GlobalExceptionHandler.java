package br.gov.ce.cgd.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata validações de Bean Validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Erro de validação");
        response.put("message", "Campos inválidos");
        response.put("errors", errors);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        return response;
    }

    /**
     * Trata exceções de violação de integridade de dados
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        
        String msg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        String errorMessage;
        
        if (msg != null && (msg.contains("cpf") || msg.contains("CPF"))) {
            errorMessage = "O CPF informado já está cadastrado";
            response.put("error", "CPF duplicado");
        } else if (msg != null && (msg.contains("email") || msg.contains("EMAIL"))) {
            errorMessage = "O email informado já está cadastrado";
            response.put("error", "Email duplicado");
        } else {
            errorMessage = "Erro de integridade no banco de dados";
            response.put("error", "Violação de integridade");
        }
        
        response.put("message", errorMessage);
        response.put("status", HttpStatus.CONFLICT.value());
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Trata qualquer outra exceção genérica
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Erro interno do servidor");
        response.put("message", ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        // Log para debug
        ex.printStackTrace();
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Trata erros de autenticação/autorização
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Acesso negado");
        response.put("message", "Você não tem permissão para esta ação");
        response.put("status", HttpStatus.FORBIDDEN.value());
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}