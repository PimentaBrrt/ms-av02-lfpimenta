package lfpimenta.microservicos.p2.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GerenciamentoException {

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Usuário não localizado!")
    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public void handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException e) {
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Usuário já cadastrado!")
    @ExceptionHandler(UsuarioJaExisteException.class)
    public void handleUsuarioJaExiste(UsuarioJaExisteException e) {
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Senha atual inválida!")
    @ExceptionHandler(SenhaAtualInvalidaException.class)
    public void handleSenhaAtualInvalida(SenhaAtualInvalidaException e) {
    }

    @ResponseStatus(value = HttpStatus.LOCKED, reason = "Usuário bloqueado!")
    @ExceptionHandler(UsuarioBloqueadoException.class)
    public void handleUsuarioBloqueado(UsuarioBloqueadoException e) {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<String, String>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

}
