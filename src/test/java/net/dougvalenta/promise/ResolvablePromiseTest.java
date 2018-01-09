/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Doug Valenta
 */
public final class ResolvablePromiseTest {
	
	private static final Executor SAME_THREAD_EXECUTOR = (runnable) -> runnable.run();

	@Test
	public void testThen_WithConsumer_AfterResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final Object resolvedValue = new Object();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(resolvedValue);
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThen_WithNullConsumer_AfterResolvingWithValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, SAME_THREAD_EXECUTOR).then((Consumer<? super Object>) null);
	}

	@Test
	public void testThen_WithConsumer_AfterResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.fail();
		});
		Assert.assertEquals(promise, returnedPromise);
		executor.runAll();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThen_WithNullConsumer_AfterResolvingExceptionally() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, SAME_THREAD_EXECUTOR).then((Consumer<? super Object>) null);
	}

	@Test
	public void testThen_WithConsumer_BeforeResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume1 = new AtomicBoolean();
		final AtomicBoolean didConsume2 = new AtomicBoolean();
		final Object resolvedValue = new Object();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume1.set(true);
		}).then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume2.set(true);
		}).thenCatch((throwable) -> {
			Assert.fail();
		});
		Assert.assertEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume1.get());
		Assert.assertFalse(didConsume2.get());
		resolver.resolve(resolvedValue);
		Assert.assertFalse(didConsume1.get());
		Assert.assertFalse(didConsume2.get());
		executor.runAll();
		Assert.assertTrue(didConsume1.get());
		Assert.assertTrue(didConsume2.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThen_WithNullConsumer_BeforeResolving() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		}, SAME_THREAD_EXECUTOR).then((Consumer<? super Object>)null);
	}
	
	@Test
	public void testThen_WithConsumer_BeforeResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.fail();
		});
		Assert.assertEquals(promise, returnedPromise);
		resolver.resolveExceptionally(new Throwable());
		executor.runAll();
	}
	
	@Test
	public void testThen_WithFunction_AfterResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final Object resolvedValue = new Object();
		final Object bubbledValue = new Object();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(resolvedValue);
		}, executor);
		executor.runAll();
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
	
	@Test(expected = IllegalArgumentException.class)
	public void testThen_WithNullFunction_AfterResolvingWithValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, SAME_THREAD_EXECUTOR).then((Function<? super Object, ? extends Object>)null);
	}
	
	@Test
	public void testThen_WithThrowingFunction_AfterResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final Object resolvedValue = new Object();
		final RuntimeException thrownException = new RuntimeException();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(resolvedValue);
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.then((Function<? super Object, Object>)(value) -> {
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
	public void testThen_WithFunction_AfterResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.fail();
			return new Object();
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThen_WithNullFunction_AfterResolvingExceptionally() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, SAME_THREAD_EXECUTOR).then((Function<? super Object, ?>)null);
	}
	
	@Test
	public void testThen_WithFunction_BeforeResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final Object resolvedValue = new Object();
		final Object bubbledValue = new Object();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			return bubbledValue;
		}).then((value) -> {
			Assert.assertEquals(bubbledValue, value);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolve(resolvedValue);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThen_WithNullFunction_BeforeResolving() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		}, SAME_THREAD_EXECUTOR).then((Function<? super Object, ?>)null);
	}
	
	@Test
	public void testThen_WithThrowingFunction_BeforeResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final Object resolvedValue = new Object();
		final RuntimeException thrownException = new RuntimeException();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.then((Function<? super Object, Object>)(value) -> {
			Assert.assertEquals(resolvedValue, value);
			throw thrownException;
		}).thenCatch((throwable) -> {
			Assert.assertEquals(thrownException, throwable);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolve(resolvedValue);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testThen_WithFunction_BeforeResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final Throwable resolvedThrowable = new Throwable();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.then((value) -> {
			Assert.fail();
			return new Object();
		}).thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolveExceptionally(resolvedThrowable);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testThenCatch_WithConsumer_AfterResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final Throwable resolvedThrowable = new Throwable();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(resolvedThrowable);
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			didConsume.set(true);
		});
		Assert.assertEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThenCatch_WithNullConsumer_AfterResolvingExceptionally() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, SAME_THREAD_EXECUTOR).then((Consumer<? super Object>) null);
	}

	@Test
	public void testThenCatch_WithConsumer_AfterResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.fail();
		});
		Assert.assertEquals(promise, returnedPromise);
		executor.runAll();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThenCatch_WithNullConsumer_AfterResolvingWithValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, SAME_THREAD_EXECUTOR).thenCatch((Consumer<? super Throwable>) null);
	}

	@Test
	public void testThenCatch_WithConsumer_BeforeResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume1 = new AtomicBoolean();
		final AtomicBoolean didConsume2 = new AtomicBoolean();
		final Throwable resolvedThrowable = new Throwable();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			didConsume1.set(true);
		}).thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			didConsume2.set(true);
		}).then((value) -> {
			Assert.fail();
		});
		Assert.assertEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume1.get());
		Assert.assertFalse(didConsume2.get());
		resolver.resolveExceptionally(resolvedThrowable);
		Assert.assertFalse(didConsume1.get());
		Assert.assertFalse(didConsume2.get());
		executor.runAll();
		Assert.assertTrue(didConsume1.get());
		Assert.assertTrue(didConsume2.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThenCatch_WithNullConsumer_BeforeResolving() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		},SAME_THREAD_EXECUTOR).then((Consumer<? super Object>)null);
	}
	
	@Test
	public void testThenCatch_WithConsumer_BeforeResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		promise.thenCatch((throwable) -> {
			Assert.fail();
		});
		resolver.resolve(new Object());
		executor.runAll();
	}
	
	@Test
	public void testThenCatch_WithFunction_AfterResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final Throwable resolvedThrowable = new Throwable();
		final Object bubbledValue = new Object();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(resolvedThrowable);
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
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
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenCatch_WithNullFunction_AfterResolvingExceptionally() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, SAME_THREAD_EXECUTOR).thenCatch((Function<? super Throwable, ? extends Object>)null);
	}
	
	@Test
	public void testThenCatch_WithThrowingFunction_AfterResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final Throwable resolvedThrowable = new Throwable();
		final RuntimeException thrownException = new RuntimeException();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(resolvedThrowable);
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.thenCatch((Function<? super Throwable, Object>)(throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
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
	public void testThenCatch_WithFunction_AfterResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.fail();
			return new Object();
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenCatch_WithNullFunction_AfterResolvingWithValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, SAME_THREAD_EXECUTOR).thenCatch((Function<? super Throwable, ?>)null);
	}
	
	@Test
	public void testThenCatch_WithFunction_BeforeResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final Throwable resolvedThrowable = new Throwable();
		final Object bubbledValue = new Object();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			return bubbledValue;
		}).then((value) -> {
			Assert.assertEquals(bubbledValue, value);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolveExceptionally(resolvedThrowable);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenCatch_WithNullFunction_BeforeResolving() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		}, SAME_THREAD_EXECUTOR).thenCatch((Function<? super Throwable, ?>)null);
	}
	
	@Test
	public void testThenCatch_WithThrowingFunction_BeforeResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final Throwable resolvedThrowable = new Throwable();
		final RuntimeException thrownException = new RuntimeException();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.thenCatch((Function<? super Throwable, Object>)(throwable) -> {
			Assert.assertEquals(resolvedThrowable, throwable);
			throw thrownException;
		}).thenCatch((throwable) -> {
			Assert.assertEquals(thrownException, throwable);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolveExceptionally(resolvedThrowable);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testThenCatch_WithFunction_BeforeResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final Object resolvedValue = new Object();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.thenCatch((throwable) -> {
			Assert.fail();
			return new Object();
		}).then((value) -> {
			Assert.assertEquals(resolvedValue, value);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolve(resolvedValue);
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testThenFinally_WithRunnable_AfterResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didRun = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			didRun.set(true);
		});
		Assert.assertEquals(promise, returnedPromise);
		Assert.assertFalse(didRun.get());
		executor.runAll();
		Assert.assertTrue(didRun.get());
	}
	
	@Test
	public void testThenFinally_WithRunnable_AfterResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didRun = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, executor);
		executor.runAll();
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			didRun.set(true);
		});
		Assert.assertEquals(promise, returnedPromise);
		Assert.assertFalse(didRun.get());
		executor.runAll();
		Assert.assertTrue(didRun.get());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenFinally_WithNullRunnable_AfterResolvingWithValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, SAME_THREAD_EXECUTOR).thenFinally((Runnable)null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenFinally_WithNullRunnable_AfterResolvingExceptionally() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, SAME_THREAD_EXECUTOR).thenFinally((Runnable)null);
	}
	
	@Test
	public void testThenFinally_WithRunnable_BeforeResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didRun = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			didRun.set(true);
		});
		Assert.assertEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didRun.get());
		resolver.resolve(new Object());
		Assert.assertFalse(didRun.get());
		executor.runAll();
		Assert.assertTrue(didRun.get());
	}
	
	@Test
	public void testThenFinally_WithRunnable_BeforeResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didRun = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			didRun.set(true);
		});
		Assert.assertEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didRun.get());
		resolver.resolveExceptionally(new Throwable());
		Assert.assertFalse(didRun.get());
		executor.runAll();
		Assert.assertTrue(didRun.get());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenFinally_WithNullRunnable_BeforeResolving() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		}, SAME_THREAD_EXECUTOR).thenFinally((Runnable)null);
	}
	
	@Test
	public void testThenFinally_WithSupplier_AfterResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, executor);
		executor.runAll();
		final Object bubbledValue = new Object();
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
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
	
	@Test
	public void testThenFinally_WithThrowingSupplier_AfterResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, executor);
		executor.runAll();
		final RuntimeException thrownException = new RuntimeException();
		final Promise<Object> returnedPromise = promise.thenFinally((Supplier<Object>)() -> {
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
	public void testThenFinally_WithSupplier_AfterResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, executor);
		executor.runAll();
		final Object bubbledValue = new Object();
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
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
	
	@Test
	public void testThenFinally_WithThrowingSupplier_AfterResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final Promise<Object> promise = ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, executor);
		executor.runAll();
		final RuntimeException thrownException = new RuntimeException();
		final Promise<Object> returnedPromise = promise.thenFinally((Supplier<Object>)() -> {
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
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenFinally_WithNullSupplier_AfterResolvingWithValue() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolve(new Object());
		}, SAME_THREAD_EXECUTOR).thenFinally((Supplier<?>)null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenFinally_WithNullSupplier_AfterResolvingExceptionally() {
		ResolvablePromise.newPromise((resolver) -> {
			resolver.resolveExceptionally(new Throwable());
		}, SAME_THREAD_EXECUTOR).thenFinally((Supplier<?>)null);
	}
	
	@Test
	public void testThenFinally_WithSupplier_BeforeResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Object bubbledValue = new Object();
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			return bubbledValue;
		}).then((value) -> {
			Assert.assertEquals(bubbledValue, value);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolve(new Object());
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testThenFinally_WithSupplier_BeforeResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final Object bubbledValue = new Object();
		final Promise<Object> returnedPromise = promise.thenFinally(() -> {
			return bubbledValue;
		}).then((value) -> {
			Assert.assertEquals(bubbledValue, value);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolveExceptionally(new Throwable());
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testThenFinally_WithThrowingSupplier_BeforeResolvingWithValue() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final RuntimeException thrownException = new RuntimeException();
		final Promise<Object> returnedPromise = promise.thenFinally((Supplier<Object>)() -> {
			throw thrownException;
		}).thenCatch((throwable) -> {
			Assert.assertEquals(thrownException, throwable);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolve(new Object());
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test
	public void testThenFinally_WithThrowingSupplier_BeforeResolvingExceptionally() {
		final MockExecutor executor = new MockExecutor();
		final AtomicBoolean didConsume = new AtomicBoolean();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, executor);
		executor.runAll();
		final Resolver<Object> resolver = resolverReference.get();
		final RuntimeException thrownException = new RuntimeException();
		final Promise<Object> returnedPromise = promise.thenFinally((Supplier<Object>)() -> {
			throw thrownException;
		}).thenCatch((throwable) -> {
			Assert.assertEquals(thrownException, throwable);
			didConsume.set(true);
		});
		Assert.assertNotEquals(promise, returnedPromise);
		executor.runAll();
		Assert.assertFalse(didConsume.get());
		resolver.resolveExceptionally(new Throwable());
		Assert.assertFalse(didConsume.get());
		executor.runAll();
		Assert.assertTrue(didConsume.get());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThenFinally_WithNullSupplier_BeforeResolving() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		}, SAME_THREAD_EXECUTOR).thenFinally((Supplier<?>)null);
	}
	
	@Test
	public void testThen_WithThrowingConsumers() {
		final RuntimeException thrownException0 = new RuntimeException();
		final RuntimeException thrownException1 = new RuntimeException();
		final RuntimeException thrownException2 = new RuntimeException();
		final AtomicReference<Resolver<Object>> resolverReference = new AtomicReference<>();
		final Promise<Object> promise = ResolvablePromise.newPromise(resolverReference::set, SAME_THREAD_EXECUTOR)
				.then((Consumer<? super Object>)(value) -> {
					throw thrownException0;
				}).then((Consumer<? super Object>)(value) -> {
					throw thrownException1;
				}).then((Consumer<? super Object>)(value) -> {
					throw thrownException2;
				});
		final Resolver<Object> resolver = resolverReference.get();
		try {
			resolver.resolve(new Object());
			Assert.fail();
		} catch (RuntimeException e) {
			Assert.assertEquals(thrownException0, e);
			final Throwable[] suppressed = e.getSuppressed();
			Assert.assertEquals(thrownException1, suppressed[0]);
			Assert.assertEquals(thrownException2, suppressed[1]);
		}
	}
	
	@Test(expected = IllegalStateException.class)
	public void testThen_WithThrowingQueue() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		}, SAME_THREAD_EXECUTOR, new ThrowingQueue<>(), new LinkedList<>()).then((value) -> {
			Assert.fail();
		});
	}
	
	@Test(expected = IllegalStateException.class)
	public void testThenCatch_WithThrowingQueue() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		}, SAME_THREAD_EXECUTOR, new LinkedList<>(), new ThrowingQueue<>()).thenCatch((throwable) -> {
			Assert.fail();
		});
	}
	
	@Test(expected = IllegalStateException.class)
	public void testThenFinally_WithThrowingQueue() {
		ResolvablePromise.newPromise((resolver) -> {
			// NOP
		}, SAME_THREAD_EXECUTOR, new ThrowingQueue<>(), new LinkedList<>()).thenFinally(() -> {
			Assert.fail();
		});
	}
	
}
