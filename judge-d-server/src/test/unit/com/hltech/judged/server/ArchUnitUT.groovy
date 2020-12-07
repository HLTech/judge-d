package com.hltech.judged.server

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.runner.RunWith

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

@RunWith(value = ArchUnitRunner)
@AnalyzeClasses(packagesOf = App.class, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchUnitUT {
    private static final DOMAIN_PACKAGE = '..domain..'
    private static final INFRASTRUCTURE_PACKAGE = '..infrastructure..'
    private static final INTERFACES_PACKAGE = '..interfaces'

    private static final DEFAULT_PACKAGE = ''
    private static final SLF4J_PACKAGE = 'org.slf4j..'
    private static final LOMBOK_PACKAGE = 'lombok..'
    private static final CORE_JAVA_PACKAGE = 'java..'
    private static final VAUNT_PACKAGE = 'com.hltech.vaunt..'
    private static final PACT_MODEL_PACKAGE = 'au.com.dius.pact.model..'
    private static final SWAGGER_PACT_VALIDATOR_PACKAGE = 'com.atlassian.oai.validator..'
    private static final GOOGLE_COMMON_PACKAGE = 'com.google.common..'

    @ArchTest
    public static final ArchRule NO_CIRCULAR_DEPENDENCIES_BETWEEN_PACKAGES = SlicesRuleDefinition.slices()
        .matching("com.hltech.judged.server.(*)..")
        .should()
        .beFreeOfCycles()

    @ArchTest
    public static final ArchRule DOMAIN_SHOULD_NOT_HAVE_EXTERNAL_DEPENDENCIES = noClasses()
        .that()
        .resideInAPackage(DOMAIN_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideOutsideOfPackages(DOMAIN_PACKAGE, DEFAULT_PACKAGE, SLF4J_PACKAGE, LOMBOK_PACKAGE, CORE_JAVA_PACKAGE,
            VAUNT_PACKAGE, PACT_MODEL_PACKAGE, SWAGGER_PACT_VALIDATOR_PACKAGE, GOOGLE_COMMON_PACKAGE)

    @ArchTest
    public static final ArchRule INFRASTRUCTURE_SHOULD_NOT_DEPEND_ON_INTERFACES = noClasses()
        .that()
        .resideInAPackage(INFRASTRUCTURE_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(INTERFACES_PACKAGE)
}
