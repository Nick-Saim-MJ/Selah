package com.selahfinance.reflexion.domain.model;

import com.selahfinance.shared.domain.DimensionMayordomia;

/** Pregunta del banco rotativo de reflexión (Lev. 23:3: el sábado como espacio de evaluación). */
public record PreguntaReflexion(short id, String texto, String referenciaBiblica, DimensionMayordomia dimension) {
}
