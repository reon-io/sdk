package io.reon.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ListenableExecutorService implements ExecutorService {
	private final ExecutorService executor;

	public ListenableExecutorService(ExecutorService service) {
		executor = service;
	}

	private class ListenableFutureImpl<V> implements ListenableFuture<V> {
		private final Future<V> future;
		private final CallableWithListener<V> callable;

		private ListenableFutureImpl(Future<V> future, CallableWithListener<V> callable) {
			this.future = future;
			this.callable = callable;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			boolean canceled = future.cancel(mayInterruptIfRunning);
			if (canceled) callable.invokeOnCancel();
			return canceled;
		}

		@Override
		public boolean isCancelled() {
			return future.isCancelled();
		}

		@Override
		public boolean isDone() {
			return future.isDone();
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			return future.get();
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return future.get(timeout, unit);
		}

		@Override
		public void setListener(FutureListener<V> l) {
			callable.setListener(l);
			if (l != null && isDone()) { // we are late to the party
				if (isCancelled()) callable.invokeOnCancel();
				if (callable.failed != null) callable.invokeOnFailure();
				else callable.invokeOnSuccess();
			}
		}

	}

	private class CallableWithListener<V> implements Callable<V>  {
		private final Callable<V> callable;
		private volatile FutureListener<V> listener;
		private volatile Throwable failed;
		private volatile V value;

		private CallableWithListener(Callable<V> callable) {
			this.callable = callable;
		}

		private void invokeOnCancel() {
			if (listener != null) listener.onCancel();
		}

		private void invokeOnSuccess() {
			if (listener != null) listener.onSuccess(value);
		}

		private void invokeOnFailure() {
			if (listener != null) listener.onFailure(failed);
		}

		public void setListener(FutureListener<V> l) {
			listener = l;
		}

		@Override
		public V call() throws Exception {
			try {
				value = callable.call();
				invokeOnSuccess();
			} catch (Throwable e) {
				failed = e;
				invokeOnFailure();
				throw e;
			}
			return value;
		}
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return executor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}

	@Override
	public <T> ListenableFuture<T> submit(Callable<T> task) {
		CallableWithListener<T> callable = new CallableWithListener<>(task);
		return new ListenableFutureImpl(executor.submit(callable), callable);
	}

	@Override
	public <T> ListenableFuture<T> submit(final Runnable task, final T result) {
		return submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				task.run();
				return result;
			}
		});
	}

	@Override
	public ListenableFuture<?> submit(Runnable task) {
		return submit(task, null);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return executor.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return executor.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return executor.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return executor.invokeAny(tasks, timeout, unit);
	}

	@Override
	public void execute(Runnable command) {
		executor.execute(command);
	}
}
