package org.atpfivt.comparison;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skips the tests of the annotated method for the listed implementations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisabledForImplementation {

	/**
	 * The implementations to skip.
	 */
	Class<?>[] value();

	/**
	 * Why the implementations are skipped.
	 */
	String reason() default "";

}
