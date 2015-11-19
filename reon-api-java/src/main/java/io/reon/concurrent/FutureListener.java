package io.reon.concurrent;

import java.util.EventListener;

public interface FutureListener<V> extends EventListener {
	void onSuccess(V result);
	void onFailure(Throwable e);
	void onCancel();
}