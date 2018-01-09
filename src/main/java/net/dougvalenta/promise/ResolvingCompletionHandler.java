/*
 * Copyright 2017 Doug Valenta.
 * Licensed under the MIT license: https://opensource.org/licenses/MIT
 */
package net.dougvalenta.promise;

import java.nio.channels.CompletionHandler;

/**
 *
 * @author Doug Valenta
 */
final class ResolvingCompletionHandler<V> implements CompletionHandler<V, Void> {

	private final Resolver<V> resolver;
	
	ResolvingCompletionHandler(final Resolver<V> resolver) {
		this.resolver = resolver;
	}
	
	@Override
	public void completed(final V value, final Void attachment) {
		resolver.resolve(value);
	}

	@Override
	public void failed(final Throwable throwable, final Void attachment) {
		resolver.resolveExceptionally(throwable);
	}
	
}
