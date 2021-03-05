package com.hltech.judged.server

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration
@SpringBootTest(classes = App.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@interface FunctionalTest {

    @AliasFor(annotation = ContextConfiguration.class, attribute = 'initializers')
    Class<? extends ApplicationContextInitializer> initializers() default PostgresPropertiesInitializer
}
