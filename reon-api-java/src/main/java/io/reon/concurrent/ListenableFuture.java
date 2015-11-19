package io.reon.concurrent;

import java.util.concurrent.Future;

// This is a kind of future you can listen to
public interface ListenableFuture<V> extends Future<V> {
	void setListener(FutureListener<V> l); // only one listener
}
