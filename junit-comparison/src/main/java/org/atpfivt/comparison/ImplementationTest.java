package org.atpfivt.comparison;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.atpfivt.comparison.internal.ImplementationsProvider;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A test that runs once per implementation declared by
 * {@link CompareImplementations @CompareImplementations}; the counterpart of
 * {@code @Test} for methods with a parameter of the contract type.
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TestTemplate
@ExtendWith(ImplementationsProvider.class)
public @interface ImplementationTest {
}
