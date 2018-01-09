/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *
 * @author Doug Valenta
 */
public final class Promises {
	
	private static final Executor SAME_THREAD_EXECUTOR = (runnable) -> runnable.run();
	
	/**
	 * Returns a new promise that will have already been resolved with the supplied 
	 * value.
	 * 
	 * @param <V> the type of the value the returned promise will resolved with
	 * @param value to resolve the returned promise with
	 * @return a new promise
	 * @throws IllegalArgumentException if {@code value} is null
	 */
	public static <V> Promise<V> resolved(final V value) {
		if (value == null) {
			throw new IllegalArgumentException("Missing value");
		}
		return new ResolvedPromise<>(value, SAME_THREAD_EXECUTOR);
	}
	
	/**
	 * Returns a new promise that will have already been resolved with the supplied 
	 * value, using the supplied {@link java.util.concurrent.Executor}.
	 * 
	 * @param <V> the type of the value the returned promise will resolved with
	 * @param value to resolve the returned promise with
	 * @param executor to use
	 * @return a new promise
	 * @throws IllegalArgumentException if {@code value} or {@code executor} is null
	 */
	public static <V> Promise<V> resolved(final V value, final Executor executor) {
		if (value == null) {
			throw new IllegalArgumentException("Missing value");
		}
		if (executor == null) {
			throw new IllegalArgumentException("Missing executor");
		}
		return new ResolvedPromise<>(value, executor);
	}
	
	/**
	 * Returns a new promise that will have already been resolved exceptionally with
	 * the supplied {@link Throwable}.
	 * 
	 * @param <V> the type of the value the returned promise would resolve with
	 * @param throwable to resolve the returned promise with
	 * @return a new promise
	 * @throws IllegalArgumentException if {@code throwable} is null
	 */
	public static <V> Promise<V> resolvedExceptionally(final Throwable throwable) {
		if (throwable == null) {
			throw new IllegalArgumentException("Missing throwable");
		}
		return new ExceptionalPromise<>(throwable, SAME_THREAD_EXECUTOR);
	}
	
	/**
	 * Returns a new promise that will have already been resolved exceptionally with
	 * the supplied {@link Throwable}, using the supplied 
	 * {@link java.util.concurrent.Executor}.
	 * 
	 * @param <V> the type of the value the returned promise would resolve with
	 * @param throwable to resolve the returned promise with
	 * @param executor to use
	 * @return a new promise
	 * @throws IllegalArgumentException if {@code throwable} or {@code executor} is null
	 */
	public static <V> Promise<V> resolvedExceptionally(final Throwable throwable, final Executor executor) {
		if (throwable == null) {
			throw new IllegalArgumentException("Missing throwable");
		}
		if (executor == null) {
			throw new IllegalArgumentException("Missing executor");
		}
		return new ExceptionalPromise<>(throwable, executor);
	}
	
	/**
	 * Returns a new promise that can be resolved with a value or exceptionally by
	 * the {@link Resolver} passed to the supplied 
	 * {@link java.util.function.Consumer}.
	 * 
	 * @param <V> the type of the value the returned promise can resolve with
	 * @param consumer to be passed a resolver for resolving the returned promise
	 * @return a new promise
	 * @throws IllegalArgumentException if {@code consumer} is null
	 */
	public static <V> Promise<V> resolvable(final Consumer<? super Resolver<V>> consumer) {
		if (consumer == null) {
			throw new IllegalArgumentException("Missing consumer");
		}
		return ResolvablePromise.newPromise(consumer, SAME_THREAD_EXECUTOR);
	}
	
	/**
	 * Returns a new promise that can be resolved with a value or exceptionally by the
	 * {@link Resolver} passed to the supplied {@link java.util.function.Consumer},
	 * using the supplied {@link java.util.concurrent.Executor}.
	 * 
	 * @param <V> the type of the value the returned promise can resolve with
	 * @param consumer to be passed a resolver for resolving the returned promise
	 * @param executor to use
	 * @return a new promise
	 * @throws IllegalArgumentException if {@code consumer} or {@code executor} is null
	 */
	public static <V> Promise<V> resolvable(final Consumer<? super Resolver<V>> consumer, final Executor executor) {
		if (consumer == null) {
			throw new IllegalArgumentException("Missing consumer");
		}
		if (executor == null) {
			throw new IllegalArgumentException("Missing executor");
		}
		return ResolvablePromise.newPromise(consumer, executor);
	}
	
	/**
	 * Returns a new promise that will never resolve.
	 * 
	 * @return a new promise 
	 */
	public static Promise<?> never() {
		return new NeverPromise<>();
	}
	
	/**
	 * Returns a new promise that will resolve when all the supplied promises have
	 * resolved, using the supplied {@link java.util.concurrent.Executor}.
	 * 
	 * <p>
	 * When the returned promise resolves with a value, that value is a collection of all
	 * resolved values of the supplied promises, in the order in which their promises were
	 * supplied to this method.
	 * 
	 * <p>
	 * If any of supplied promises resolves exceptionally, the returned promise resolves
	 * exceptionally with the {@link Throwable} provided by the first of the supplied promises 
	 * to do so.
	 * 
	 * <p>
	 * If no promises are supplied to this method, the returned promise resolves with an
	 * empty collection.
	 * 
	 * @param <V> the type of the values to promise
	 * @param executor to use
	 * @param promises to promise values promised by
	 * @return a new promise
	 * @throws IllegalArgumentException if {@code executor} or {@code promises} is null
	 */
	public static <V> Promise<Collection<V>> allOf(final Executor executor, final Promise<? extends V>... promises) {
		if (executor == null) {
			throw new IllegalArgumentException("Missing executor");
		}
		if (promises == null) {
			throw new IllegalArgumentException("Missing promises");
		}
		if (promises.length == 0) {
			return new ResolvedPromise<>(Collections.emptyList(), executor);
		}
		return ResolvablePromise.newPromise((resolver) -> {
			final AtomicInteger promisesResolved = new AtomicInteger();
			final List<V> values = new ArrayList<>(promises.length);
			for (int i = 0; i < promises.length; i++) {
				final int index = i;
				promises[i].then((value) -> {
					values.add(index, value);
					if (promisesResolved.addAndGet(1) == promises.length) {
						resolver.resolve(values);
					}
				}).thenCatch((throwable) -> {
					resolver.resolveExceptionallyIfNotResolved(throwable);
				});
			}
		}, executor);
	}
	
	/**
	 * Returns a new promise that will resolve when any of the supplied promises has
	 * resolved, using the supplied {@link java.util.concurrent.Executor}.
	 * 
	 * <p>
	 * If any of supplied promises resolves with a value, the returned promise resolves
	 * with the value provided by the first of the supplied promises to do so.
	 * 
	 * <p>
	 * When the returned promise resolves exceptionally, the {@link Throwable} provided
	 * contains as suppressed throwables all throwables provided by the supplied promises, 
	 * in the order in which their promises were supplied to this method.
	 * 
	 * <p>
	 * If no promises are supplied to this method, the returned promise never resolves.
	 * 
	 * @param <V> the type of the value to promise
	 * @param executor to use
	 * @param promises to promise a value promised by
	 * @return a new promise
	 * @throws IllegalArgumentException if {@code executor} or {@code promises} is null
	 */
	public static <V> Promise<V> anyOf(final Executor executor, final Promise<? extends V>... promises) {
		if (executor == null) {
			throw new IllegalArgumentException("Missing executor");
		}
		if (promises == null) {
			throw new IllegalArgumentException("Missing promises");
		}
		if (promises.length == 0) {
			return new NeverPromise<>();
		}
		return ResolvablePromise.newPromise((resolver) -> {
			final AtomicInteger promisesResolvedExceptionally = new AtomicInteger();
			final List<Throwable> throwables = new ArrayList<>(promises.length);
			for (int i = 0; i < promises.length; i++) {
				final int index = i;
				promises[i].then((value) -> {
					resolver.resolveIfNotResolved(value);
				}).thenCatch((throwable) -> {
					throwables.add(index, throwable);
					if (promisesResolvedExceptionally.addAndGet(1) == promises.length) {
						final Throwable aggregateThrowable = new Throwable();
						for (Throwable suppressedThrowable : throwables) {
							aggregateThrowable.addSuppressed(suppressedThrowable);
						}
						resolver.resolveExceptionally(aggregateThrowable);
					}
				});
			}
		}, executor);
	}
	
	public static Promise<Void> afterAll(final Executor executor, final Promise<?>... promises) {
		return ResolvablePromise.newPromise((resolver) -> {
			final AtomicInteger promisesResolved = new AtomicInteger();
			for (int i = 0; i < promises.length; i++) {
				final int index = i;
				promises[i].then((value) -> {
					if (promisesResolved.addAndGet(1) == promises.length) {
						resolver.resolveIfNotResolved(null);
					}
				}).thenCatch((throwable) -> {
					resolver.resolveExceptionallyIfNotResolved(throwable);
				});
			}
		}, executor);
	}
	
	public static Promise<Void> afterAny(final Executor executor, final Promise<?>... promises) {
		return ResolvablePromise.newPromise((resolver) -> {
			final AtomicInteger promisesResolvedExceptionally = new AtomicInteger();
			final List<Throwable> throwables = new ArrayList<>(promises.length);
			for (int i = 0; i < promises.length; i++) {
				final int index = i;
				promises[i].then((value) -> {
					resolver.resolveIfNotResolved(null);
				}).thenCatch((throwable) -> {
					throwables.add(index, throwable);
					if (promisesResolvedExceptionally.addAndGet(1) == promises.length) {
						final Throwable aggregateThrowable = new Throwable();
						for (Throwable suppressedThrowable : throwables) {
							aggregateThrowable.addSuppressed(suppressedThrowable);
						}
						resolver.resolveExceptionally(aggregateThrowable);
					}
				});
			}
		}, executor);
	}
	
	private Promises() {
		// NOP
	}
	
}
