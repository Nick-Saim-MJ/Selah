package com.selahfinance.integraciones.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selahfinance.integraciones.domain.model.ApiKey;
import com.selahfinance.integraciones.domain.model.ClienteApi;
import com.selahfinance.integraciones.domain.model.SuscripcionWebhook;
import com.selahfinance.integraciones.domain.service.FirmaWebhook;
import com.selahfinance.shared.domain.DomainException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IntegracionesDominioTest {

    @Test
    void laApiKeyGeneradaSeReconoceYSoloSeGuardaSuHash() {
        var key = ApiKey.generar();

        assertThat(key.valorPlano()).startsWith("sf_").contains(".");
        assertThat(ApiKey.prefijoDe(key.valorPlano())).contains(key.prefijo());
        assertThat(ApiKey.coincide(key.valorPlano(), key.hash())).isTrue();
        assertThat(ApiKey.coincide(key.valorPlano() + "x", key.hash())).isFalse();
        assertThat(key.hash()).doesNotContain(key.valorPlano());
        assertThat(ApiKey.prefijoDe("basura")).isEmpty();
        assertThat(ApiKey.prefijoDe(null)).isEmpty();
    }

    @Test
    void unClienteSoloAceptaScopesConocidos() {
        var key = ApiKey.generar();

        assertThatThrownBy(() -> ClienteApi.nuevo("salud", null, null, Set.of("todo:todo"), null, key))
                .isInstanceOf(DomainException.class).hasMessageContaining("Scope no permitido");
        assertThat(ClienteApi.nuevo("salud", null, null, Set.of("habitos:write"), null, key).activo()).isTrue();
    }

    @Test
    void unClienteRevocadoOVencidoNoEsVigente() {
        var key = ApiKey.generar();
        var ahora = Instant.parse("2026-09-28T12:00:00Z");
        var cliente = ClienteApi.nuevo("salud", null, null, Set.of("habitos:write"), ahora.plusSeconds(60), key);

        assertThat(cliente.vigente(ahora)).isTrue();
        assertThat(cliente.vigente(ahora.plusSeconds(61))).isFalse();
        assertThat(cliente.revocar().vigente(ahora)).isFalse();
    }

    @Test
    void laFirmaHmacEsEstableYDependeDelSecretoYDelCuerpo() {
        String firma = FirmaWebhook.firmar("whsec_abc", "{\"a\":1}");

        assertThat(firma).startsWith("sha256=").hasSize("sha256=".length() + 64);
        assertThat(FirmaWebhook.firmar("whsec_abc", "{\"a\":1}")).isEqualTo(firma);
        assertThat(FirmaWebhook.firmar("whsec_otro", "{\"a\":1}")).isNotEqualTo(firma);
        assertThat(FirmaWebhook.firmar("whsec_abc", "{\"a\":2}")).isNotEqualTo(firma);
    }

    @Test
    void elWebhookValidaEventoYUrl() {
        var cliente = UUID.randomUUID();

        assertThatThrownBy(() -> SuscripcionWebhook.nueva(cliente, "evento.inventado", "https://x.com/h"))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> SuscripcionWebhook.nueva(cliente, "*", "ftp://x.com/h")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> SuscripcionWebhook.nueva(cliente, "*", "no es url")).isInstanceOf(DomainException.class);
        assertThat(SuscripcionWebhook.nueva(cliente, "movimiento.registrado", "https://equipo.ejemplo.com/hook").activa())
                .isTrue();
    }
}
