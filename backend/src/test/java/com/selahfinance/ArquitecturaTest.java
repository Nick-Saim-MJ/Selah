package com.selahfinance;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Hace cumplir la regla de dependencia de Clean Architecture en cada build.
 * Si alguien rompe una capa, {@code mvn test} falla indicando la clase y el motivo.
 */
@AnalyzeClasses(packages = "com.selahfinance", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    private static final Pattern MODULO = Pattern.compile("^com\\.selahfinance\\.(\\w+)\\.(.*)$");
    private static final Set<String> TRANSVERSALES = Set.of("shared", "config");

    @ArchTest
    static final ArchRule dominioEsJavaPuro = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "jakarta.servlet..", "org.hibernate..",
                    "tools.jackson..", "com.fasterxml.jackson..", "lombok..")
            .because("el dominio no debe depender de frameworks");

    @ArchTest
    static final ArchRule dominioNoConoceCapasExternas = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..", "..config..");

    @ArchTest
    static final ArchRule aplicacionNoConoceInfraestructura = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..config..",
                    "jakarta.persistence..", "org.springframework.web..", "org.springframework.data..")
            .because("los casos de uso solo hablan con puertos");

    @ArchTest
    static final ArchRule modulosSinCiclos = slices().matching("com.selahfinance.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule modulosSeComunicanPorPuertos = classes()
            .that().resideInAPackage("com.selahfinance..")
            .should(soloUsarApiPublicaDeOtrosModulos())
            .because("de otro módulo solo se usa su application.port.in y su domain (modelos/eventos)");

    private static ArchCondition<JavaClass> soloUsarApiPublicaDeOtrosModulos() {
        return new ArchCondition<>("usar solo la API pública de otros módulos") {
            @Override
            public void check(JavaClass origen, ConditionEvents events) {
                var mOrigen = MODULO.matcher(origen.getPackageName());
                if (!mOrigen.matches() || TRANSVERSALES.contains(mOrigen.group(1))) {
                    return;
                }
                origen.getDirectDependenciesFromSelf().forEach(dep -> {
                    var mDestino = MODULO.matcher(dep.getTargetClass().getPackageName());
                    if (!mDestino.matches()) {
                        return;
                    }
                    String moduloDestino = mDestino.group(1);
                    String capaDestino = mDestino.group(2);
                    boolean otroModulo = !moduloDestino.equals(mOrigen.group(1)) && !TRANSVERSALES.contains(moduloDestino);
                    boolean publica = capaDestino.startsWith("application.port.in") || capaDestino.startsWith("domain")
                            || capaDestino.startsWith("application.port.out") && origen.getPackageName().contains(".infrastructure.adapter");
                    if (otroModulo && !publica) {
                        events.add(SimpleConditionEvent.violated(dep, dep.getDescription()));
                    }
                });
            }
        };
    }
}
