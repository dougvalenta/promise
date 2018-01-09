/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Doug Valenta
 */
public class ResolvablePromise_ResolverTest {
	
	private static final Executor SAME_THREAD_EXECUTOR = (runnable) -> runnable.run();
	
	@Test
	public void testResolve() {
		final AtomicBoolean didResolve = new AtomicBoolean();
		ResolvablePromise.newPromise((resolver) -> {
			Assert.assertFalse(resolver.isResolved());
			resolver.resolve(new Object());
			Assert.assertTrue(resolver.isResolved());
			didResolve.set(true);
		}, SAME_THREAD_EXECUTOR);
		Assert.assertTrue(didResolve.get());
	}
	
	@Test
	public void testResolveExceptionally() {
		final AtomicBoolean didResolveExceptionally = new AtomicBoolean();
		ResolvablePromise.newPromise((resolver) -> {
			Assert.assertFalse(resolver.isResolved());
			resolver.resolveExceptionally(new Throwable());
			Assert.assertTrue(resolver.isResolved());
			didResolveExceptionally.set(true);
		}, SAME_THREAD_EXECUTOR);
		Assert.assertTrue(didResolveExceptionally.get());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testResolve_WhenAlreadyResolvedWithValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
			resolver.resolve(new Object());
		}, SAME_THREAD_EXECUTOR);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testResolve_WhenAlreadyResolvedExceptionally() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
			resolver.resolve(new Object());
		}, SAME_THREAD_EXECUTOR);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testResolveExceptionally_WhenAlreadyResolvedWithValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
			resolver.resolveExceptionally(new Throwable());
		}, SAME_THREAD_EXECUTOR);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testResolveExceptionally_WhenAlreadyResolvedExceptionally() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
			resolver.resolveExceptionally(new Throwable());
		}, SAME_THREAD_EXECUTOR);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testResolve_WithNullValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(null);
		}, SAME_THREAD_EXECUTOR);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testResolveExceptionally_WithNullThrowable() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(null);
		}, SAME_THREAD_EXECUTOR);
	}
	
	@Test
	public void testResolveIfNotResolved_WhenNotResolved() {
		final AtomicBoolean didResolve = new AtomicBoolean();
		ResolvablePromise.newPromise((resolver) -> {
			Assert.assertFalse(resolver.isResolved());
			resolver.resolveIfNotResolved(new Object());
			Assert.assertTrue(resolver.isResolved());
			didResolve.set(true);
		}, SAME_THREAD_EXECUTOR);
		Assert.assertTrue(didResolve.get());
	}
	
	@Test
	public void testResolveExceptionallyIfNotResolved_WhenNotResolved() {
		final AtomicBoolean didResolve = new AtomicBoolean();
		ResolvablePromise.newPromise((resolver) -> {
			Assert.assertFalse(resolver.isResolved());
			resolver.resolveExceptionallyIfNotResolved(new Throwable());
			Assert.assertTrue(resolver.isResolved());
			didResolve.set(true);
		}, SAME_THREAD_EXECUTOR);
		Assert.assertTrue(didResolve.get());
	}
	
	@Test
	public void testResolveIfNotResolved_WhenResolvedWithValue() {
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(resolvedValue);
			resolver.resolveIfNotResolved(new Object());
		}, SAME_THREAD_EXECUTOR).then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testResolveExceptionallyIfNotResolved_WhenResolvedWithValue() {
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(resolvedValue);
			resolver.resolveExceptionallyIfNotResolved(new Throwable());
		}, SAME_THREAD_EXECUTOR).then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testResolveIfNotResolved_WhenResolvedExceptionally() {
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Throwable resolvedThrowable = new Throwable();
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(resolvedThrowable);
			resolver.resolveIfNotResolved(new Object());
		}, SAME_THREAD_EXECUTOR).thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			didConsume.set(true);
		});
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testResolveExceptionallyIfNotResolved_WhenResolvedExceptionally() {
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Throwable resolvedThrowable = new Throwable();
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(resolvedThrowable);
			resolver.resolveExceptionallyIfNotResolved(new Throwable());
		}, SAME_THREAD_EXECUTOR).thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			didConsume.set(true);
		});
		Assert.assertTrue(didConsume.get());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testResolveIfNotResolved_WithNullValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveIfNotResolved(null);
		}, SAME_THREAD_EXECUTOR);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testResolveExceptionallyIfNotResolved_WithNullValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionallyIfNotResolved(null);
		}, SAME_THREAD_EXECUTOR);
	}
	
}
