package com.selahfinance.reflexion.domain.model;

import com.selahfinance.shared.domain.Dinero;

/** Lo que pasó en la semana: cuánto entró, cuánto se gastó y si el diezmo del mes está al día (null si no aplica). */
public record ResumenSemana(Dinero entro, Dinero gasto, Boolean diezmoAlDia) {
}
