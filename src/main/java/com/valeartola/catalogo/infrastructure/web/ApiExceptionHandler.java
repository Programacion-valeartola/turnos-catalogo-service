package com.valeartola.catalogo.infrastructure.web;

import com.valeartola.catalogo.domain.exception.InvalidSearchCriteriaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce los errores a respuestas application/problem+json con un code estable,
 * sin exponer detalles internos.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    public ProblemDetail handleInvalidCriteria(InvalidSearchCriteriaException e) {
        return badRequest("INVALID_SEARCH_CRITERIA", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return badRequest("VALIDATION_ERROR", "Valor inválido para el parámetro '" + e.getName() + "'");
    }

    private ProblemDetail badRequest(String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setProperty("code", code);
        return problem;
    }
}
