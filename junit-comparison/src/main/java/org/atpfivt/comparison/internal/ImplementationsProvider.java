package org.atpfivt.comparison.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.atpfivt.comparison.CompareImplementations;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * Provides one invocation per implementation declared by
 * {@link CompareImplementations @CompareImplementations}.
 *
 * <p>Internal API: registered by the annotations of this library.
 */
public final class ImplementationsProvider implements TestTemplateInvocationContextProvider {

	/**
	 * Configuration parameter that restricts the implementations to a
	 * comma-separated list of simple class names: {@value}
	 */
	public static final String IMPLEMENTATIONS_PROPERTY = "comparison.implementations";

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return findDeclaration(context) //
				.filter(declaration -> hasParameterOfType(context, declaration.contract())) //
				.isPresent();
	}

	@Override
	public boolean mayEncloseTestTemplateInvocations(ExtensionContext context) {
		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		CompareImplementations declaration = findDeclaration(context).orElseThrow();
		validate(declaration);
		Set<String> selected = selectedImplementations(context);
		return Arrays.stream(declaration.value()) //
				.filter(type -> selected.isEmpty() || selected.contains(type.getSimpleName())) //
				.map(type -> new ImplementationInvocationContext(declaration.contract(), type));
	}

	@Override
	public boolean mayReturnZeroTestTemplateInvocationContexts(ExtensionContext context) {
		return true;
	}

	private static Optional<CompareImplementations> findDeclaration(ExtensionContext context) {
		return AnnotationSupport.findAnnotation(context.getRequiredTestClass(), CompareImplementations.class,
			context.getEnclosingTestClasses());
	}

	private static boolean hasParameterOfType(ExtensionContext context, Class<?> type) {
		return List.of(context.getRequiredTestMethod().getParameterTypes()).contains(type);
	}

	private static void validate(CompareImplementations declaration) {
		if (declaration.value().length == 0) {
			throw new ExtensionConfigurationException(
				"@CompareImplementations must declare at least one implementation of "
						+ declaration.contract().getName());
		}
		for (Class<?> type : declaration.value()) {
			if (!declaration.contract().isAssignableFrom(type)) {
				throw new ExtensionConfigurationException(
					type.getName() + " does not implement " + declaration.contract().getName());
			}
		}
	}

	private static Set<String> selectedImplementations(ExtensionContext context) {
		return context.getConfigurationParameter(IMPLEMENTATIONS_PROPERTY) //
				.map(value -> Arrays.stream(value.split(",")) //
						.map(String::trim) //
						.filter(name -> !name.isEmpty()) //
						.collect(Collectors.toSet())) //
				.orElse(Set.of());
	}

}
