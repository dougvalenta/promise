/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author Doug Valenta
 */
final class NeverPromise<V> implements Promise<V> {
	
	@Override
	public Promise<V> then(final Consumer<? super V> consumer) {
		return this;
	}

	@Override
	public <W> Promise<W> then(final Function<? super V, ? extends W> function) {
		return new NeverPromise<>();
	}

	@Override
	public Promise<V> thenCatch(final Consumer<? super Throwable> consumer) {
		return this;
	}

	@Override
	public Promise<V> thenCatch(final Function<? super Throwable, ? extends V> function) {
		return new NeverPromise<>();
	}

	@Override
	public Promise<V> thenFinally(final Runnable runnable) {
		return this;
	}

	@Override
	public <W> Promise<W> thenFinally(final Supplier<W> supplier) {
		return new NeverPromise<>();
	}
	
}
