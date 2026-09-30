package com.selahfinance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita las tareas programadas (@Scheduled) de los módulos: reportes, recordatorios, webhooks. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
