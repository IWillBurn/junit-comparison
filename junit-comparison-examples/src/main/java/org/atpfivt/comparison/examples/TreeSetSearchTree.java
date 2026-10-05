package org.atpfivt.comparison.examples;

import java.util.List;
import java.util.TreeSet;

public final class TreeSetSearchTree implements SearchTree {

	private final TreeSet<Integer> keys = new TreeSet<>();

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
