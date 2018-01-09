/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Doug Valenta
 */
public final class ResolvedPromiseTest {
	
	private static final Executor SAME_THREAD_EXECUTOR = (runnable) -> runnable.run();

	@Test
	public void testThen_WithConsumer() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		final Promise<Object> promise = new ResolvedPromise<>(resolvedValue, executor);
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testThen_WithNullConsumer() {
		new ResolvedPromise<>(new Object(), SAME_THREAD_EXECUTOR).then((Consumer<? super Object>)null);
	}
	
	@Test
	public void testThen_WithFunction() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		final Object bubbledValue = new Object();
		final Promise<Object> promise = new ResolvedPromise<>(resolvedValue, executor);
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			return bubbledValue;
		}).then((value) -> {
			Assert.assertEquals(bubbledValue, value);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testThen_WithNullFunction() {
		new ResolvedPromise<>(new Object(), SAME_THREAD_EXECUTOR).then((Function<? super Object, ?>)null);
	}
	
	@Test
	public void testThen_WithThrowingFunction() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		final Promise<Object> promise = new ResolvedPromise<>(resolvedValue, executor);
		final RuntimeException thrownException = new RuntimeException();
		final Promise<Void> returnedPromise = promise.then((Function<? super Object, Void>)(value) -> {
			Assert.assertEquals(resolvedValue, value);
			throw thrownException;
		}).thenCatch((throwable) -> {
			Assert.assertEquals(thrownException, throwable);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testThenCatch_WithConsumer() {
		final MockExecutor executor = new MockExecutor();
		final Promise<Object> promise = new ResolvedPromise<>(new Object(), executor);
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.fail();
		});
		Assert.assertEquals(promise, returnedPromise);
		executor.runAll();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testThenCatch_WithNullConsumer() {
		new ResolvedPromise<>(new Object(), SAME_THREAD_EXECUTOR).thenCatch((Consumer<? super Throwable>)null);
	}
	
	@Test
	public void testThenCatch_WithFunction() {
		final MockExecutor executor = new MockExecutor();
		final Promise<Object> promise = new ResolvedPromise<>(new Object(), executor);
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.fail();
			return new Object();
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testThenCatch_WithNullFunction() {
		new ResolvedPromise<>(new Object(), SAME_THREAD_EXECUTOR).thenCatch((Function<? super Throwable, ? extends Object>)null);
	}
	
	@Test
	public void testThenFinally_WithRunnable() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didRun = new AtomicBoolean();
		final Promise<Object> promise = new ResolvedPromise<>(new Object(), executor);
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			didRun.set(true);
		});
		Assert.assertEquals(promise, returnedPromise);
		Assert.assertFalse(didRun.get());
		executor.runAll();
		Assert.assertTrue(didRun.get());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testThenFinally_WithNullRunnable() {
		new ResolvedPromise<>(new Object(), SAME_THREAD_EXECUTOR).thenFinally((Runnable)null);
	}
	
	@Test
	public void testThenFinally_WithSupplier() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object suppliedValue = new Object();
		final Promise<Object> promise = new ResolvedPromise<>(new Object(), executor);
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			return suppliedValue;
		}).then((value) -> {
			Assert.assertEquals(suppliedValue, value);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testThenFinally_WithNullSupplier() {
		new ResolvedPromise<>(new Object(), SAME_THREAD_EXECUTOR).thenFinally((Supplier<?>)null);
	}
	
	@Test
	public void testThenFinally_WithThrowingSupplier() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final RuntimeException thrownException = new RuntimeException();
		final Promise<Object> promise = new ResolvedPromise<>(new Object(), executor);
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			throw thrownException;
		}).thenCatch((throwable) -> {
			Assert.assertEquals(thrownException, throwable);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}

}
