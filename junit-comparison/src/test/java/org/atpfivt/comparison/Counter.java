package org.atpfivt.comparison;

import java.util.concurrent.atomic.AtomicInteger;

interface Counter {

	int increment();

	int value();

	final class Simple implements Counter {

		private int value;

		@Override
		public int increment() {
			return ++this.value;
		}

		@Override
		public int value() {
			return this.value;
		}
	}

	final class Atomic implements Counter {

		private final AtomicInteger value = new AtomicInteger();

		@Override
		public int increment() {
			return this.value.incrementAndGet();
		}

		@Override
		public int value() {
			return this.value.get();
		}
	}

	final class Broken implements Counter {

		private int value;

		@Override
		public int increment() {
			this.value += 2;
			return this.value;
		}

		@Override
		public int value() {
			return this.value;
		}
	}

}
