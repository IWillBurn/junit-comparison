package org.atpfivt.comparison.examples;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class BinarySearchTree implements SearchTree {

	private Node root;
	private int size;

	@Override
	public boolean add(int key) {
		Node parent = null;
		Node node = this.root;
		while (node != null) {
			if (key == node.key) {
				return false;
			}
			parent = node;
			node = key < node.key ? node.left : node.right;
		}
		Node added = new Node(key);
		if (parent == null) {
			this.root = added;
		}
		else if (key < parent.key) {
			parent.left = added;
		}
		else {
			parent.right = added;
		}
		this.size++;
		return true;
	}

	@Override
	public boolean contains(int key) {
		Node node = this.root;
		while (node != null && node.key != key) {
			node = key < node.key ? node.left : node.right;
		}
		return node != null;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public List<Integer> toSortedList() {
		List<Integer> keys = new ArrayList<>(this.size);
		Deque<Node> stack = new ArrayDeque<>();
		Node node = this.root;
		while (node != null || !stack.isEmpty()) {
			while (node != null) {
				stack.push(node);
				node = node.left;
			}
			node = stack.pop();
			keys.add(node.key);
			node = node.right;
		}
		return keys;
	}

	private static final class Node {

		private final int key;
		private Node left;
		private Node right;

		Node(int key) {
			this.key = key;
		}
	}

}
