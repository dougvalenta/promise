/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.LinkedList;

/**
 *
 * @author Doug Valenta
 */
public class ThrowingQueue<E> extends LinkedList<E> {
	
	@Override
	public boolean add(final E element) {
		throw new IllegalStateException();
	}
	
}
