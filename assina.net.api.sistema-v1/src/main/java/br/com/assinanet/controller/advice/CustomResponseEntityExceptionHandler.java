package br.com.assinanet.controller.advice;

import br.com.assinanet.response.Response;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;

/**
 * @author Samuel Oliveira - samuel@swconsultoria.com.br
 */
@RestController
@ControllerAdvice
@Log4j2
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NullPointerException.class)
    public final ResponseEntity<Response<?>> handleNullPointerException(NullPointerException ex, WebRequest request) {
        Response<?> response = new Response<>();
        response.getErrors().add("Erro ao processar requisição: " +
                Optional.ofNullable(ex.getCause()).map(Throwable::getStackTrace).map(stackTraceElements ->
                        stackTraceElements[0]).map(element -> element.getFileName() + ":" + element.getLineNumber() + " - " + ex.getMessage())
                        .orElse(Optional.ofNullable(ex.getStackTrace()).map(stackTraceElements ->
                                stackTraceElements[0]).map(ele -> ele.getFileName() + ":" + ele.getLineNumber() + " - " + ex.getMessage()).orElse(ex.getMessage())));
        log.error("NullPointer", ex);
        return ResponseEntity.badRequest().body(response);
    }

}
