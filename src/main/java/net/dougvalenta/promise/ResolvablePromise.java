/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class ResolvablePromise<V> implements Promise<V> {

	static <V> ResolvablePromise<V> newPromise(final Consumer<? super net.dougvalenta.promise.Resolver<V>> consumer, final Executor executor) {
		return newPromise(consumer, executor, new LinkedList<>(), new LinkedList<>());
	}
	
	static <V> ResolvablePromise<V> newPromise(final Consumer<? super net.dougvalenta.promise.Resolver<V>> consumer, final Executor executor, final Queue<Consumer<? super V>> valueConsumers, final Queue<Consumer<? super Throwable>> throwableConsumers) {
		final ResolvablePromise<V> promise = new ResolvablePromise<>(executor, valueConsumers, throwableConsumers);
		executor.execute(() -> consumer.accept(promise.new Resolver()));
		return promise;
	}
	
	private class Resolver implements net.dougvalenta.promise.Resolver<V> {

		@Override
		public void resolve(final V value) {
			if (value == null) {
				throw new IllegalArgumentException("Missing value");
			}
			ResolvablePromise.this.resolve(value);
		}

		@Override
		public void resolveExceptionally(final Throwable throwable) {
			if (throwable == null) {
				throw new IllegalArgumentException("Missing throwable");
			}
			ResolvablePromise.this.resolveExceptionally(throwable);
		}

		@Override
		public void resolveIfNotResolved(final V value) {
			if (value == null) {
				throw new IllegalArgumentException("Missing value");
			}
			try {
				ResolvablePromise.this.resolve(value);
			} catch (IllegalStateException e) {
				// NOP
			}
		}

		@Override
		public void resolveExceptionallyIfNotResolved(final Throwable throwable) {
			if (throwable == null) {
				throw new IllegalArgumentException("Missing throwable");
			}
			try {
				ResolvablePromise.this.resolveExceptionally(throwable);
			} catch (IllegalStateException e) {
				// NOP
			}
		}

		@Override
		public boolean isResolved() {
			return (value != null || throwable != null);
		}
		
	}
	
	private final Executor executor;
	private final Queue<Consumer<? super V>> valueConsumers;
	private final Queue<Consumer<? super Throwable>> throwableConsumers;

	private ResolvablePromise(final Executor executor, final Queue<Consumer<? super V>> valueConsumers, final Queue<Consumer<? super Throwable>> throwableConsumers) {
		this.executor = executor;
		this.valueConsumers = valueConsumers;
		this.throwableConsumers = throwableConsumers;
	}

	private V value;
	private Throwable throwable;
	
	private <T> void doResolution(final T item, final Queue<Consumer<? super T>> consumers) {
		RuntimeException exception = null;
		while (!consumers.isEmpty()) {
			final Consumer<? super T> consumer = consumers.remove();
			try {
				executor.execute(() -> consumer.accept(item));
			} catch (RuntimeException e) {
				if (exception == null) {
					exception = e;
				} else {
					exception.addSuppressed(e);
				}
			}
		}
		if (exception != null) {
			throw exception;
		}
	}
	
	private void resolve(final V value) {
		synchronized (this) {
			if (this.value != null || throwable != null) {
				throw new IllegalStateException("Promise already resolved");
			}
			this.value = value;
		}
		throwableConsumers.clear();
		doResolution(value, valueConsumers);
	}
	
	private void resolveExceptionally(final Throwable throwable) {
		synchronized (this) {
			if (value != null || this.throwable != null) {
				throw new IllegalStateException("Promise already resolved");
			}
			this.throwable = throwable;
		}
		valueConsumers.clear();
		doResolution(throwable, throwableConsumers);
	}

	@Override
	public Promise<V> then(final Consumer<? super V> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException("Missing consumer");
		}
		final boolean executeNow;
		synchronized (this) {
			if (value != null) {
				executeNow = true;
			} else {
				if (throwable == null) {
					valueConsumers.add(consumer);
				}
				executeNow = false;
			}
		}
		if (executeNow) {
			executor.execute(() -> consumer.accept(value));
		}
		return this;
	}

	@Override
	public <W> Promise<W> then(final Function<? super V, ? extends W> function) {
		if (function == null) {
			throw new IllegalArgumentException("Missing function");
		}
		return newPromise((resolver) -> {
			then((value) -> {
				try {
					resolver.resolve(function.apply(value));
				} catch (Throwable t) {
					resolver.resolveExceptionally(t);
				}
			}).thenCatch(resolver::resolveExceptionally);
		}, executor);
	}

	@Override
	public Promise<V> thenCatch(final Consumer<? super Throwable> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException("Missing consumer");
		}
		final boolean executeNow;
		synchronized (this) {
			if (throwable != null) {
				executeNow = true;
			} else {
				if (value == null) {
					throwableConsumers.add(consumer);
				}
				executeNow = false;
			}
		}
		if (executeNow) {
			executor.execute(() -> consumer.accept(throwable));
		}
		return this;
	}

	@Override
	public Promise<V> thenCatch(final Function<? super Throwable, ? extends V> function) {
		if (function == null) {
			throw new IllegalArgumentException("Missing function");
		}
		return newPromise((resolver) -> {
			thenCatch((throwable) -> {
				try {
					resolver.resolve(function.apply(throwable));
				} catch (Throwable t) {
					resolver.resolveExceptionally(t);
				}
			}).then(resolver::resolve);
		}, executor);	
	}

	@Override
	public Promise<V> thenFinally(final Runnable runnable) {
		if (runnable == null) {
			throw new IllegalArgumentException("Missing runnable");
		}
		final boolean executeNow;
		synchronized (this) {
			if (value != null || throwable != null) {
				executeNow = true;
			} else {
				executeNow = false;
				valueConsumers.add((value) -> runnable.run());
				throwableConsumers.add((value) -> runnable.run());
			}
		}
		if (executeNow) {
			executor.execute(runnable);
		}
		return this;
	}

	@Override
	public <W> Promise<W> thenFinally(final Supplier<W> supplier) {
		if (supplier == null) {
			throw new IllegalArgumentException("Missing supplier");
		}
		return newPromise((resolver) -> {
			thenFinally(() -> {
				try {
					resolver.resolve(supplier.get());
				} catch (Throwable t) {
					resolver.resolveExceptionally(t);
				}
			});
		}, executor);
	}

}
