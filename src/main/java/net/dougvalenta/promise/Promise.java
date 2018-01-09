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
public interface Promise<V> {

	/**
	 * Submit a {@link java.util.function.Consumer} to be invoked when this promise
	 * resolves with a value.
	 * 
	 * @param consumer to be invoked when this promise resolves with a value
	 * @return this promise
	 * @throws IllegalArgumentException if {@code consumer} is null
	 */
	public Promise<V> then(Consumer<? super V> consumer);

	/**
	 * Submit a {@link java.util.function.Function} to be invoked when this promise
	 * resolves with a value, returning a new promise that will resolve with the value
	 * returned by the function.
	 * 
	 * <p>
	 * If this promise resolves exceptionally, the returned promise will resolve exceptionally
	 * as well.
	 * 
	 * <p>
	 * If the function throws a {@link Throwable}, the returned promise will resolve
	 * exceptionally with the throwable thrown.
	 * 
	 * @param <W> the type of the value returned by the function
	 * @param function to be invoked when this promise resolves with a value
	 * @return a new promise that will resolve with the value returned by the function
	 * @throws IllegalArgumentException if {@code function} is null
	 */
	public <W> Promise<W> then(Function<? super V, ? extends W> function);
	
	/**
	 * Submit a {@link java.util.function.Consumer} to be invoked when this promise
	 * resolves exceptionally.
	 * 
	 * @param consumer to be invoked when this promise resolves exceptionally
	 * @return this promise
	 * @throws IllegalArgumentException if {@code consumer} is null
	 */
	public Promise<V> thenCatch(Consumer<? super Throwable> consumer);
	
	/**
	 * Submit a {@link java.util.function.Function} to be invoked when this promise
	 * resolves exceptionally, returning a new promise that will resolve with the
	 * value this promise resolves with, or, if this promise resolves exceptionally,
	 * with the value returned by the function.
	 * 
	 * <p>
	 * If the function throws a {@link Throwable}, the returned promise will resolve
	 * exceptionally with the throwable thrown.
	 * 
	 * @param function to be invoked when this promise resolves exceptionally
	 * @return a new promise that will resolve with the value this promise resolves with, or,
	 * if this promise resolves exceptionally, with the value returned by the function
	 * @throws IllegalArgumentException if {@code function} is null
	 */
	public Promise<V> thenCatch(Function<? super Throwable, ? extends V> function);
	
	/**
	 * Submit a {@link Runnable} to be invoked when this promise resolves.
	 * 
	 * @param runnable to be invoked when this promise resolves
	 * @return this promise
	 * @throws IllegalArgumentException if {@code runnable} is null
	 */
	public Promise<V> thenFinally(Runnable runnable);
	
	/**
	 * Submit a {@link java.util.function.Supplier} to be invoked when this promise 
	 * resolves, returning a new promise that will resolve with the value returned by 
	 * the supplier.
	 * 
	 * <p>
	 * If the supplier throws a {@link Throwable}, the returned promise will resolve
	 * exceptionally with the throwable thrown.
	 * 
	 * @param <W> the type of the value returned by the supplier
	 * @param supplier to be invoked when this promise resolves
	 * @return a new promise that will resolve with the value returned by the supplier
	 * @throws IllegalArgumentException if {@code supplier} is null
	 */
	public <W> Promise<W> thenFinally(Supplier<W> supplier);
	
}
