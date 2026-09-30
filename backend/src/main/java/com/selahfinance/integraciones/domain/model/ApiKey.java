package com.selahfinance.integraciones.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Formato: {@code sf_<8 hex>.<64 hex>}. Se guarda solo el prefijo (para buscar) y el
 * SHA-256 de la key completa. La key en claro se entrega una única vez al crearla.
 */
public record ApiKey(String valorPlano, String prefijo, String hash) {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    public static ApiKey generar() {
        String prefijo = "sf_" + HEX.formatHex(bytesAleatorios(4));
        String plano = prefijo + "." + HEX.formatHex(bytesAleatorios(32));
        return new ApiKey(plano, prefijo, sha256(plano));
    }

    public static Optional<String> prefijoDe(String valorPlano) {
        if (valorPlano == null || !valorPlano.startsWith("sf_")) {
            return Optional.empty();
        }
        int punto = valorPlano.indexOf('.');
        return punto > 0 ? Optional.of(valorPlano.substring(0, punto)) : Optional.empty();
    }

    /** Compara en tiempo constante para no filtrar información por tiempos de respuesta. */
    public static boolean coincide(String valorPlano, String hashGuardado) {
        return MessageDigest.isEqual(sha256(valorPlano).getBytes(StandardCharsets.US_ASCII),
                hashGuardado.getBytes(StandardCharsets.US_ASCII));
    }

    public static String sha256(String valor) {
        try {
            return HEX.formatHex(MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] bytesAleatorios(int n) {
        byte[] bytes = new byte[n];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
