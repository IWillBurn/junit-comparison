package org.atpfivt.comparison.internal;

import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

record ImplementationInvocationContext(Class<?> contract, Class<?> implementation)
		implements TestTemplateInvocationContext {

	@Override
	public String getDisplayName(int invocationIndex) {
		return this.implementation.getSimpleName();
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return List.of(new ImplementationParameterResolver(this.contract, this.implementation),
			new DisabledForImplementationCondition(this.implementation));
	}

}
