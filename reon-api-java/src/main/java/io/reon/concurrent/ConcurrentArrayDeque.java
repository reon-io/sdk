package io.reon.concurrent;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

public class ConcurrentArrayDeque<E> implements Collection<E>, Deque<E>, Cloneable, Serializable {
	private final ArrayDeque<E> deque = new ArrayDeque<E>();

	@Override
	public synchronized void addFirst(E e) {
		deque.addFirst(e);
	}

	@Override
	public synchronized void addLast(E e) {
		deque.addLast(e);
	}

	@Override
	public synchronized boolean offerFirst(E e) {
		return deque.offerFirst(e);
	}

	@Override
	public synchronized boolean offerLast(E e) {
		return deque.offerLast(e);
	}

	@Override
	public synchronized E removeFirst() {
		return deque.removeFirst();
	}

	@Override
	public synchronized E removeLast() {
		return deque.removeLast();
	}

	@Override
	public synchronized E pollFirst() {
		return deque.pollFirst();
	}

	@Override
	public synchronized E pollLast() {
		return deque.pollLast();
	}

	@Override
	public synchronized E getFirst() {
		return deque.getFirst();
	}

	@Override
	public synchronized E getLast() {
		return deque.getLast();
	}

	@Override
	public synchronized E peekFirst() {
		return deque.peekFirst();
	}

	@Override
	public synchronized E peekLast() {
		return deque.peekLast();
	}

	@Override
	public synchronized boolean removeFirstOccurrence(Object o) {
		return deque.removeFirstOccurrence(o);
	}

	@Override
	public synchronized boolean removeLastOccurrence(Object o) {
		return deque.removeLastOccurrence(o);
	}

	@Override
	public synchronized boolean offer(E e) {
		return deque.offer(e);
	}

	@Override
	public synchronized E remove() {
		return deque.remove();
	}

	@Override
	public synchronized E poll() {
		return deque.poll();
	}

	@Override
	public synchronized E element() {
		return deque.element();
	}

	@Override
	public synchronized E peek() {
		return deque.peek();
	}

	@Override
	public synchronized void push(E e) {
		deque.push(e);
	}

	@Override
	public synchronized E pop() {
		return deque.pop();
	}

	@Override
	public synchronized Iterator<E> descendingIterator() {
		return deque.descendingIterator();
	}

	@Override
	public synchronized boolean add(E e) {
		return deque.add(e);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends E> collection) {
		return deque.addAll(collection);
	}

	@Override
	public synchronized void clear() {
		deque.clear();
	}

	@Override
	public synchronized boolean contains(Object o) {
		return deque.contains(o);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> collection) {
		return deque.containsAll(collection);
	}

	@Override
	public synchronized boolean isEmpty() {
		return deque.isEmpty();
	}

	@Override
	public synchronized Iterator<E> iterator() {
		return deque.iterator();
	}

	@Override
	public synchronized boolean remove(Object o) {
		return deque.remove(o);
	}

	@Override
	public synchronized boolean removeAll(Collection<?> collection) {
		return deque.removeAll(collection);
	}

	@Override
	public synchronized boolean retainAll(Collection<?> collection) {
		return deque.retainAll(collection);
	}

	@Override
	public synchronized int size() {
		return deque.size();
	}

	@Override
	public synchronized Object[] toArray() {
		return deque.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] ts) {
		return deque.toArray(ts);
	}
}
