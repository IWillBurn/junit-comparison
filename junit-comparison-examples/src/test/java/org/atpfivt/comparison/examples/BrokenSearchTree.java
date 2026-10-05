package org.atpfivt.comparison.examples;

import java.util.ArrayList;
import java.util.List;

final class BrokenSearchTree implements SearchTree {

	private final List<Integer> keys = new ArrayList<>();

	@Override
	public boolean add(int key) {
		return this.keys.add(key);
	}

	@Override
	public boolean contains(int key) {
		return this.keys.contains(key);
	}

	@Override
	public int size() {
		return this.keys.size();
	}

	@Override
	public List<Integer> toSortedList() {
		return List.copyOf(this.keys);
	}

}
