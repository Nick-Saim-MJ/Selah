package com.selahfinance.integraciones.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Firma del cuerpo con HMAC-SHA256 para que el receptor verifique que el aviso es nuestro:
 * {@code X-Selah-Firma: sha256=<hex>} calculada con el secreto de la suscripción.
 */
public final class FirmaWebhook {

    private FirmaWebhook() {
    }

    public static String firmar(String secreto, String cuerpo) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "sha256=" + HexFormat.of().formatHex(mac.doFinal(cuerpo.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo firmar el webhook", e);
        }
    }
}
