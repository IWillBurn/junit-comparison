package org.atpfivt.comparison;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.atpfivt.comparison.internal.ImplementationsProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.InvocationComposition;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

class CompareImplementationsTests {

	@Test
	void implementationTestRunsOncePerImplementation() {
		assertEquals(List.of( //
			"startsAtZero(Counter) > Simple: SUCCESSFUL", //
			"startsAtZero(Counter) > Atomic: SUCCESSFUL"), //
			leaves(execute(ImplementationTestCase.class)));
	}

	@Test
	void implementationsAreNestedBelowArgumentsOfParameterizedTest() {
		assertEquals(List.of( //
			"incrementsTimes(int, Counter) > [1] times = 1 > Simple: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > [1] times = 1 > Atomic: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > [2] times = 3 > Simple: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > [2] times = 3 > Atomic: SUCCESSFUL"), //
			leaves(execute(ParameterizedTestCase.class)));
	}

	@Test
	void implementationsAreNestedBelowRepetitions() {
		assertEquals(List.of( //
			"incrementsOnce(Counter) > repetition 1 of 2 > Simple: SUCCESSFUL", //
			"incrementsOnce(Counter) > repetition 1 of 2 > Atomic: SUCCESSFUL", //
			"incrementsOnce(Counter) > repetition 2 of 2 > Simple: SUCCESSFUL", //
			"incrementsOnce(Counter) > repetition 2 of 2 > Atomic: SUCCESSFUL"), //
			leaves(execute(RepeatedTestCase.class)));
	}

	@Test
	void declarationOnMethodIsCombinedWithDeclarationOfLibrary() {
		assertEquals(List.of( //
			"incrementsTimes(int, Counter) > repetition 1 of 2 > [1] times = 2 > Simple: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > repetition 1 of 2 > [1] times = 2 > Atomic: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > repetition 2 of 2 > [1] times = 2 > Simple: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > repetition 2 of 2 > [1] times = 2 > Atomic: SUCCESSFUL"), //
			leaves(execute(ThreeLevelsTestCase.class)));
	}

	@Test
	void methodsWithoutParameterOfContractTypeAreNotAffected() {
		assertEquals(List.of( //
			"parameterizedWithoutContract(int) > [1] value = 1: SUCCESSFUL", //
			"plainTest(): SUCCESSFUL"), //
			leaves(execute(UnaffectedMethodsTestCase.class)));
	}

	@Test
	void failingImplementationFailsOnlyItsOwnTests() {
		assertEquals(List.of( //
			"incrementsTimes(int, Counter) > [1] times = 1 > Simple: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > [1] times = 1 > Broken: FAILED", //
			"incrementsTimes(int, Counter) > [2] times = 3 > Simple: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > [2] times = 3 > Broken: FAILED"), //
			leaves(execute(BrokenImplementationTestCase.class)));
	}

	@Test
	void disabledForImplementationSkipsItsTestsWithReason() {
		EngineExecutionResults results = execute(DisabledForImplementationTestCase.class);

		assertEquals(List.of( //
			"startsAtZero(Counter) > Simple: SUCCESSFUL", //
			"startsAtZero(Counter) > Broken: SKIPPED"), //
			leaves(results));
		assertEquals(List.of("Disabled for Broken ==> known bug"), results.testEvents().skipped().stream() //
				.map(event -> event.getRequiredPayload(String.class)) //
				.toList());
	}

	@Test
	void configurationParameterSelectsImplementations() {
		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter") //
				.selectors(selectClass(ParameterizedTestCase.class)) //
				.configurationParameter(ImplementationsProvider.IMPLEMENTATIONS_PROPERTY, "Atomic") //
				.execute();

		assertEquals(List.of( //
			"incrementsTimes(int, Counter) > [1] times = 1 > Atomic: SUCCESSFUL", //
			"incrementsTimes(int, Counter) > [2] times = 3 > Atomic: SUCCESSFUL"), //
			leaves(results));
	}

	@Test
	void sameInstanceIsInjectedIntoLifecycleMethodsOfOneTest() {
		assertEquals(List.of( //
			"seesValueOfBeforeEach(Counter) > Simple: SUCCESSFUL", //
			"seesValueOfBeforeEach(Counter) > Atomic: SUCCESSFUL"), //
			leaves(execute(LifecycleMethodsTestCase.class)));
	}

	@Test
	void nestedClassesInheritTheDeclaration() {
		assertEquals(List.of( //
			"startsAtZero(Counter) > Simple: SUCCESSFUL", //
			"startsAtZero(Counter) > Atomic: SUCCESSFUL"), //
			leaves(execute(EnclosingTestCase.class)));
	}

	@Test
	void singleImplementationCanBeSelectedByUniqueId() {
		UniqueId uniqueId = execute(ParameterizedTestCase.class).testEvents().finished().stream() //
				.map(Event::getTestDescriptor) //
				.filter(descriptor -> path(descriptor).equals("incrementsTimes(int, Counter) > [2] times = 3 > Atomic")) //
				.map(TestDescriptor::getUniqueId) //
				.findFirst() //
				.orElseThrow();

		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter") //
				.selectors(selectUniqueId(uniqueId)) //
				.execute();

		assertEquals(List.of("incrementsTimes(int, Counter) > [2] times = 3 > Atomic: SUCCESSFUL"), leaves(results));
	}

	@Test
	void implementationOfAnotherTypeIsConfigurationError() {
		EngineExecutionResults results = execute(InvalidImplementationTestCase.class);

		Throwable failure = results.containerEvents().failed().stream() //
				.map(event -> event.getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow()) //
				.findFirst() //
				.orElseThrow();
		assertInstanceOf(ExtensionConfigurationException.class, failure);
		assertTrue(failure.getMessage().endsWith("String does not implement " + Counter.class.getName()));
	}

	private static EngineExecutionResults execute(Class<?> testClass) {
		return EngineTestKit.engine("junit-jupiter") //
				.selectors(selectClass(testClass)) //
				.execute();
	}

	private static List<String> leaves(EngineExecutionResults results) {
		List<String> leaves = new ArrayList<>();
		results.testEvents().stream().forEach(event -> {
			switch (event.getType()) {
				case FINISHED -> leaves.add(path(event.getTestDescriptor()) + ": "
						+ event.getRequiredPayload(TestExecutionResult.class).getStatus());
				case SKIPPED -> leaves.add(path(event.getTestDescriptor()) + ": SKIPPED");
				default -> {
				}
			}
		});
		return leaves;
	}

	private static String path(TestDescriptor descriptor) {
		Deque<String> names = new ArrayDeque<>();
		TestDescriptor current = descriptor;
		while (current.getSource().filter(MethodSource.class::isInstance).isPresent()) {
			names.addFirst(current.getDisplayName());
			current = current.getParent().orElseThrow();
		}
		return String.join(" > ", names);
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Atomic.class })
	static class ImplementationTestCase {

		@ImplementationTest
		void startsAtZero(Counter counter) {
			assertEquals(0, counter.value());
		}
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Atomic.class })
	static class ParameterizedTestCase {

		@ParameterizedTest
		@ValueSource(ints = { 1, 3 })
		void incrementsTimes(int times, Counter counter) {
			for (int i = 0; i < times; i++) {
				counter.increment();
			}
			assertEquals(times, counter.value());
		}
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Atomic.class })
	static class RepeatedTestCase {

		@RepeatedTest(2)
		void incrementsOnce(Counter counter) {
			assertEquals(1, counter.increment());
		}
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Atomic.class })
	static class ThreeLevelsTestCase {

		@RepeatedTest(2)
		@ParameterizedTest
		@ValueSource(ints = 2)
		@InvocationComposition(levels = ParameterizedTest.class)
		void incrementsTimes(int times, Counter counter) {
			for (int i = 0; i < times; i++) {
				counter.increment();
			}
			assertEquals(times, counter.value());
		}
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Atomic.class })
	@TestMethodOrder(MethodOrderer.MethodName.class)
	static class UnaffectedMethodsTestCase {

		@Test
		void plainTest() {
		}

		@ParameterizedTest
		@ValueSource(ints = 1)
		void parameterizedWithoutContract(int value) {
			assertEquals(1, value);
		}
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Broken.class })
	static class BrokenImplementationTestCase {

		@ParameterizedTest
		@ValueSource(ints = { 1, 3 })
		void incrementsTimes(int times, Counter counter) {
			for (int i = 0; i < times; i++) {
				counter.increment();
			}
			assertEquals(times, counter.value());
		}
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Broken.class })
	static class DisabledForImplementationTestCase {

		@ImplementationTest
		@DisabledForImplementation(value = Counter.Broken.class, reason = "known bug")
		void startsAtZero(Counter counter) {
			assertEquals(0, counter.value());
		}
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Atomic.class })
	static class LifecycleMethodsTestCase {

		private Counter counterOfBeforeEach;

		@BeforeEach
		void incrementOnce(Counter counter) {
			this.counterOfBeforeEach = counter;
			counter.increment();
		}

		@ImplementationTest
		void seesValueOfBeforeEach(Counter counter) {
			assertSame(this.counterOfBeforeEach, counter);
			assertEquals(1, counter.value());
		}

		@AfterEach
		void sameInstanceInAfterEach(Counter counter) {
			assertSame(this.counterOfBeforeEach, counter);
		}
	}

	@CompareImplementations(contract = Counter.class, value = { Counter.Simple.class, Counter.Atomic.class })
	static class EnclosingTestCase {

		@Nested
		class NestedTestCase {

			@ImplementationTest
			void startsAtZero(Counter counter) {
				assertEquals(0, counter.value());
			}
		}
	}

	@CompareImplementations(contract = Counter.class, value = String.class)
	static class InvalidImplementationTestCase {

		@ImplementationTest
		void startsAtZero(Counter counter) {
		}
	}

}
