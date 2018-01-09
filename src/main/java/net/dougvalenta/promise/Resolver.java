/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

/**
 *
 * @author Doug Valenta
 */
public interface Resolver<V> {
	
	/**
	 * Resolve the promise with a value
	 * 
	 * @param value to resolve the promise with
	 * @throws IllegalArgumentException if {@code value} is null
	 * @throws IllegalStateException if the promise has already been resolved
	 */
	public void resolve(V value);
	
	/**
	 * Resolve the promise exceptionally
	 * 
	 * @param throwable to resolve the promise with
	 * @throws IllegalArgumentException if {@code throwable} is null
	 * @throws IllegalStateException if the promise has already been resolved
	 */
	public void resolveExceptionally(Throwable throwable);
	
	/**
	 * Resolve the promise with a value if it has not already been resolved.
	 * 
	 * <p>
	 * If the promise has already been resolved, this method has no effect.
	 * 
	 * @param value to resolve the promise with
	 * @throws IllegalArgumentException if {@code value} is null
	 */
	public void resolveIfNotResolved(V value);
	
	/**
	 * Resolve the promise exceptionally if it has not already been resolved.
	 * 
	 * <p>
	 * If the promise has already been resolved, this method has no effect.
	 * 
	 * @param throwable to resolve the promise with
	 * @throws IllegalArgumentException if {@code throwable} is null
	 */
	public void resolveExceptionallyIfNotResolved(Throwable throwable);
	
	/**
	 * Check whether the promise has already been resolved.
	 * 
	 * @return {@code true} if the promise has already been resolved, otherwise 
	 * {@code false}
	 */
	public boolean isResolved();
	
}
