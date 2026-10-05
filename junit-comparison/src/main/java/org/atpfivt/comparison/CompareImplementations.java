package org.atpfivt.comparison;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.atpfivt.comparison.internal.ImplementationsProvider;
import org.junit.jupiter.api.InvocationComposition;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Runs the test templates of the annotated class once per implementation of a
 * contract.
 *
 * <p>A test template is affected if its method declares a parameter whose type
 * is exactly {@link #contract()}. A fresh instance of the implementation is
 * injected into that parameter and into parameters of the same type of the
 * lifecycle methods of the same test. Methods without such a parameter and
 * plain {@code @Test} methods are not affected; use {@link ImplementationTest}
 * instead of {@code @Test}.
 *
 * <p>The implementations form the innermost level of the test tree, below the
 * invocations of other test templates such as the sets of arguments of a
 * {@code @ParameterizedTest} or the repetitions of a {@code @RepeatedTest}:
 *
 * <pre>
 * containsInsertedKey(int, SearchTree)
 * ├─ [1] key = 1
 * │  ├─ BinarySearchTree
 * │  └─ TreeSetSearchTree
 * └─ [2] key = 42
 *    ├─ BinarySearchTree
 *    └─ TreeSetSearchTree
 * </pre>
 *
 * <p>The configuration parameter
 * {@value ImplementationsProvider#IMPLEMENTATIONS_PROPERTY} restricts the
 * implementations to a comma-separated list of simple class names.
 *
 * @see ImplementationTest
 * @see DisabledForImplementation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(ImplementationsProvider.class)
@InvocationComposition(levels = CompareImplementations.class)
public @interface CompareImplementations {

	/**
	 * The type of the parameters the implementations are injected into.
	 */
	Class<?> contract();

	/**
	 * The implementations of {@link #contract()}, each with a no-arg
	 * constructor.
	 */
	Class<?>[] value();

}
