/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 *
 * @author Doug Valenta
 */
final class MockExecutor implements Executor {

	private final Queue<Runnable> runnables = new LinkedList<>();
	
	@Override
	public void execute(final Runnable runnable) {
		runnables.add(runnable);
	}
	
	@Deprecated
	Queue<Runnable> getRunnables() {
		return runnables;
	}
	
	void runNext() {
		runnables.remove().run();
	}
	
	void runStage() {
		final Queue<Runnable> stage = new LinkedList<>();
		while (!runnables.isEmpty()) {
			stage.add(runnables.remove());
		}
		while (!stage.isEmpty()) {
			stage.remove().run();
		}
	}
	
	void runAll() {
		while (!runnables.isEmpty()) {
			runnables.remove().run();
		}
	}
	
	@Deprecated
	boolean isEmpty() {
		return runnables.isEmpty();
	}
	
}
