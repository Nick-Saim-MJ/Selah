package com.selahfinance.integraciones.infrastructure.adapter;

import com.selahfinance.integraciones.application.port.out.WebhooksPorts.CifradorPort;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cifra los secretos de webhook (AES-256-GCM) con una clave que vive en el entorno
 * ({@code SELAH_CLAVE_CIFRADO}), no en la base de datos. Formato: base64(iv || cifrado+tag).
 */
@Component
class AesGcmCifradorAdapter implements CifradorPort {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String claveConfigurada;

    AesGcmCifradorAdapter(@Value("${selah.integraciones.clave-cifrado:}") String claveConfigurada) {
        this.claveConfigurada = claveConfigurada;
    }

    @Override
    public String cifrar(String textoPlano) {
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, clave(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            byte[] todo = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, todo, 0, iv.length);
            System.arraycopy(cifrado, 0, todo, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(todo);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar el secreto", e);
        }
    }

    @Override
    public String descifrar(String textoCifrado) {
        try {
            byte[] todo = Base64.getDecoder().decode(textoCifrado);
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, clave(), new GCMParameterSpec(TAG_BITS, todo, 0, IV_BYTES));
            return new String(cipher.doFinal(todo, IV_BYTES, todo.length - IV_BYTES), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo descifrar el secreto (¿cambió SELAH_CLAVE_CIFRADO?)", e);
        }
    }

    private SecretKeySpec clave() throws GeneralSecurityException {
        if (claveConfigurada == null || claveConfigurada.length() < 16) {
            throw new IllegalStateException("Configura selah.integraciones.clave-cifrado (SELAH_CLAVE_CIFRADO, mínimo 16 caracteres)");
        }
        // Se deriva una clave de 256 bits de la frase configurada
        return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(claveConfigurada.getBytes(StandardCharsets.UTF_8)), "AES");
    }
}
