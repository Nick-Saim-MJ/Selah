package com.selahfinance.movimientos.domain.model;

public enum OrigenMovimiento {
    /** Registrado por el usuario en la app. */
    APP,
    /** Creado por el sistema de otro equipo vía /api/v1/integraciones. */
    API_EXTERNA,
    IMPORTACION
}
