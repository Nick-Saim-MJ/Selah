package com.selahfinance.mayordomia.application.port.out;

import com.selahfinance.mayordomia.domain.model.PoliticaMayordomia;
import java.util.UUID;

/** Porcentajes vigentes del hogar (los provee el módulo hogar a través de un adaptador). */
public interface PoliticaMayordomiaPort {

    PoliticaMayordomia de(UUID hogarId);
}
