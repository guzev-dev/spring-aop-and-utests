package guzev.petproj.pl.exceptions;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class ExceptionResponseHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Object> handleDuplicateKeyException(DuplicateKeyException ex) {

        return getResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException() {

        return getResponseEntity("Cannot find resource with the given input.", HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {

        return getResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private static ResponseEntity<Object> getResponseEntity(String message, HttpStatusCode status) {
        return new ResponseEntity<>(getExceptionResponseBody(message, status), status);
    }

    private static Map<?, ?> getExceptionResponseBody(String message, HttpStatusCode statusCode) {
        return Map.of("error", Map.of("message", message, "status", statusCode.value()));
    }

}
