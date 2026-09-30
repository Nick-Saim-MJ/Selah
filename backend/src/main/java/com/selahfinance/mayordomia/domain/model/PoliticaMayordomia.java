package com.selahfinance.mayordomia.domain.model;

import java.math.BigDecimal;

/** Porcentajes vigentes del hogar (base 100). pctOfrenda ya es 0 si el hogar no aparta ofrenda. */
public record PoliticaMayordomia(BigDecimal pctDiezmo, BigDecimal pctOfrenda) {
}
