/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class ExceptionalPromise<V> implements Promise<V> {

	private final Throwable throwable;
	private final Executor executor;
	
	ExceptionalPromise(final Throwable throwable, final Executor executor) {
		this.throwable = throwable;
		this.executor = executor;
	}
	
	@Override
	public Promise<V> then(final Consumer<? super V> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException("Missing consumer");
		}
		return this;
	}

	@Override
	public <W> Promise<W> then(final Function<? super V, ? extends W> function) {
		if (function == null) {
			throw new IllegalArgumentException("Missing function");
		}
		return new ExceptionalPromise<>(throwable, executor);
	}

	@Override
	public Promise<V> thenCatch(final Consumer<? super Throwable> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException("Missing consumer");
		}
		executor.execute(() -> consumer.accept(throwable));
		return this;
	}

	@Override
	public Promise<V> thenCatch(final Function<? super Throwable, ? extends V> function) {
		if (function == null) {
			throw new IllegalArgumentException("Missing function");
		}
		return ResolvablePromise.newPromise((resolver) -> {
			try {
				resolver.resolve(function.apply(throwable));
			} catch (Throwable t) {
				resolver.resolveExceptionally(t);
			}
		}, executor);
	}

	@Override
	public Promise<V> thenFinally(final Runnable runnable) {
		if (runnable == null) {
			throw new IllegalArgumentException("Missing runnable");
		}
		executor.execute(runnable);
		return this;
	}

	@Override
	public <W> Promise<W> thenFinally(final Supplier<W> supplier) {
		if (supplier == null) {
			throw new IllegalArgumentException("Missing supplier");
		}
		return ResolvablePromise.newPromise((resolver) -> {
			try {
				resolver.resolve(supplier.get());
			} catch (Throwable t) {
				resolver.resolveExceptionally(t);
			}
		}, executor);
	}
	
}
