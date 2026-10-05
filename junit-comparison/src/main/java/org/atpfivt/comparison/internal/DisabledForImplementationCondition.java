package org.atpfivt.comparison.internal;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.List;

import org.atpfivt.comparison.DisabledForImplementation;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

record DisabledForImplementationCondition(Class<?> implementation) implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return context.getTestMethod() //
				.flatMap(method -> AnnotationSupport.findAnnotation(method, DisabledForImplementation.class)) //
				.filter(annotation -> List.of(annotation.value()).contains(this.implementation)) //
				.map(annotation -> disabled("Disabled for " + this.implementation.getSimpleName(),
					annotation.reason())) //
				.orElseGet(() -> enabled("Enabled for " + this.implementation.getSimpleName()));
	}

}
