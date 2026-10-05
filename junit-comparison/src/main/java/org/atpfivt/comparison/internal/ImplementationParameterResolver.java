package org.atpfivt.comparison.internal;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Resolves parameters of exactly the contract type with an instance of the
 * implementation that is shared by all parameters of one test. An
 * implementation that is {@link AutoCloseable} is closed after the test.
 */
record ImplementationParameterResolver(Class<?> contract, Class<?> implementation) implements ParameterResolver {

	private static final Namespace NAMESPACE = Namespace.create(ImplementationParameterResolver.class);

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.getParameter().getType() == this.contract;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return extensionContext.getStore(NAMESPACE).computeIfAbsent(this.implementation,
			ImplementationParameterResolver::instantiate, Object.class);
	}

	private static Object instantiate(Class<?> type) {
		try {
			Constructor<?> constructor = type.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		}
		catch (ReflectiveOperationException ex) {
			throw new ExtensionConfigurationException("Cannot instantiate implementation " + type.getName(), ex);
		}
	}

}
