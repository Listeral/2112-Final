package util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** An implementation of RingBuffer that implements the BlockingQueue interface */
public class RingBuffer<E> implements BlockingQueue<E> {
	E[] queue;
	int head;
	int tail;
	boolean hasSpace;
	Lock lock;
	
	public RingBuffer(int cap) {
		queue = (E[]) new Object[cap];//change later
		head = 0;
		tail = 0;
		hasSpace = true;
		lock = new RingBufferLock();
	}
	
	@Override
	public boolean add(Object e) {
		if (size() < queue.length) {
			queue[tail] = (E) e;
			tail = (tail + 1) % queue.length;
			if (tail == head) hasSpace = false;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean contains(Object o) {
		for (E elem : queue) {
			if (elem != null && elem.equals(o)) return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof RingBuffer<?>) {
			RingBuffer<E> other = (RingBuffer<E>) o;
			return (size() == other.size());
		}
		return false;
	}
	
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	
	@Override
	public int size() {
		int size = Math.abs(tail-head);
		//case where head = tail because full ring buffer
		if (size == 0 && queue[head] != null) return queue.length;
		return size;
	}

	@Override
	public Iterator iterator() {
		return new RingBufferIterator<E>(this);
	}
	
	@Override
	public boolean remove(Object o) {
		for (int i = Math.min(head,tail); i <= Math.max(head, tail); i++) {
			if (queue[i].equals(o)) {
				if (i == head)
					head = (head+1) % queue.length;
				else if (i == tail)
					tail = (tail-1 < 0 ? queue.length-1 : tail-1);
				queue[i] = null;
				hasSpace = true;
				return true;
			}
		}
		return false;
	}
	/*
	public boolean remove() {
		if (queue[head] != null) {
			queue[head] = null;
			head = (head+1)%queue.length;
			return true;
		}
		return false;
	}
	*/
	@Override
	public E remove() {
		if (queue[head] != null) {
			E temp = queue[head];
			head++;
			queue[head-1] = null;
			hasSpace = true;
			return temp;
		}
		return null;
	}

	@Override
	public E poll() {
		E temp = queue[head];
		queue[head] = null;
		return temp;
	}

	@Override
	public void put(Object e) throws InterruptedException {
		lock.lock();
		try { add(e); }
		finally { lock.unlock(); }
	}
	
	@Override
	public E element() throws NoSuchElementException {
		if (isEmpty()) throw new NoSuchElementException();
		return queue[head];
	}

	@Override
	public boolean offer(Object e) {
		if (hasSpace) {
			add(e);
			return true;
		}
		return false;
	}
	
	@Override
	public E peek() {
		return queue[head];
	}
	
	/* A lock for this ring buffer implementation */
	public static class RingBufferLock implements Lock {
		
		boolean hasSpace = true;
		
		public void lock() {
			synchronized(this) {
				while (!hasSpace) {
					try {
						this.wait();
					} catch (Exception e) {} }
				hasSpace = false;
			}
		}
		
		public void unlock() {
			synchronized(this) {
				hasSpace = true;
				this.notifyAll();
			}
		}
	}
	
	/* An iterator for this ring buffer implementation */
	public static class RingBufferIterator<E> implements Iterator<E> {
		private final RingBuffer rb;
		
		public RingBufferIterator(RingBuffer rb) {
			this.rb = rb;
		}
		
		public boolean hasNext() {
			if (!rb.isEmpty()) return true;
			return false;
		}
		
		public E next() {
			return (E) rb.remove();
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/** Do these need to be implemented? */
	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray(Object[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public E take() throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int remainingCapacity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int drainTo(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int drainTo(Collection c, int maxElements) {
		throw new UnsupportedOperationException();
	}

	
	/** The following methods are left unimplemented */
	
	@Override
	public boolean offer(Object e, long timeout, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public E poll(long timeout, TimeUnit unit) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
