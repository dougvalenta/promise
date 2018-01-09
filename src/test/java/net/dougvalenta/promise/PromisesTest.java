/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Doug Valenta
 */
public class PromisesTest {

	@Test
	public void testResolved() {
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		final Promise<Object> promise = Promises.resolved(resolvedValue);
		Assert.assertEquals(ResolvedPromise.class, promise.getClass());
		promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertTrue(didConsume.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolved_WithNullValue() {
		Promises.resolved(null);
	}

	@Test
	public void testResolved_WithExecutor() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		final Promise<Object> promise = Promises.resolved(resolvedValue, executor);
		Assert.assertEquals(ResolvedPromise.class, promise.getClass());
		promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertFalse(didConsume.get());
		executor.runNext();
		Assert.assertTrue(didConsume.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolved_WithNullValue_WithExecutor() {
		Promises.resolved(null, new MockExecutor());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolved_WithNullExecutor() {
		Promises.resolved(new Object(), null);
	}

	@Test
	public void testResolvedExceptionally() {
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Throwable resolvedThrowable = new Throwable();
		final Promise<Object> promise = Promises.resolvedExceptionally(resolvedThrowable);
		Assert.assertEquals(ExceptionalPromise.class, promise.getClass());
		promise.thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			didConsume.set(true);
		});
		Assert.assertTrue(didConsume.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolvedExceptionally_WithNullThrowable() {
		Promises.resolvedExceptionally(null);
	}

	@Test
	public void testResolvedExceptionally_WithExecutor() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Throwable resolvedThrowable = new Throwable();
		final Promise<Object> promise = Promises.resolvedExceptionally(resolvedThrowable, executor);
		Assert.assertEquals(ExceptionalPromise.class, promise.getClass());
		promise.thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			didConsume.set(true);
		});
		Assert.assertFalse(didConsume.get());
		executor.runNext();
		Assert.assertTrue(didConsume.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolvedExceptionally_WithNullThrowable_WithExecutor() {
		Promises.resolvedExceptionally(null, new MockExecutor());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolvedExceptionally_WithNullExecutor() {
		Promises.resolvedExceptionally(new Throwable(), null);
	}

	@Test
	public void testResolvable() {
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		final Promise<Object> promise = Promises.resolvable((resolver) -> {
			resolver.resolve(resolvedValue);
		});
		Assert.assertEquals(ResolvablePromise.class, promise.getClass());
		promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertTrue(didConsume.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolvable_WithNullConsumer() {
		Promises.resolvable(null);
	}

	@Test
	public void testResolvable_WithExecutor() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Object resolvedValue = new Object();
		final Promise<Object> promise = Promises.resolvable((resolver) -> {
			resolver.resolve(resolvedValue);
		}, executor);
		Assert.assertEquals(ResolvablePromise.class, promise.getClass());
		promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolvable_WithNullConsumer_WithExecutor() {
		Promises.resolvable(null, new MockExecutor());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testResolvable_WithNullExecutor() {
		Promises.resolvable((resolver) -> {
			Assert.fail();
		}, null);
	}

	@Test
	public void test_IsUtilityClass() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		Assert.assertTrue(Modifier.isFinal(Promises.class.getModifiers()));
		Assert.assertEquals(1, Promises.class.getDeclaredConstructors().length);
		final Constructor<Promises> constructor = Promises.class.getDeclaredConstructor();
		Assert.assertFalse(constructor.isAccessible() || !Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
		constructor.setAccessible(false);
		for (final Method method : Promises.class.getMethods()) {
			Assert.assertFalse(!Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().equals(Promises.class));
		}
	}

}
