package com.valeartola.catalogo.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Error REST de la cátedra (application/problem+json).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemResponse(Integer status, String code, String detail) {
}
