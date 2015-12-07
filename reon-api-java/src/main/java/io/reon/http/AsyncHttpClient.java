package io.reon.http;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import io.reon.concurrent.ListenableExecutorService;
import io.reon.concurrent.ListenableFuture;

public class AsyncHttpClient implements Closeable {

	private final io.reon.net.Connection conn;
	private final ListenableExecutorService executorService;
	private final MessageWriter writer;
	private final ResponseReader reader;

	public AsyncHttpClient(io.reon.net.Connection conn, ExecutorService executorService) throws IOException {
		this.conn = conn;
		this.executorService = new ListenableExecutorService(executorService);
		writer = new MessageWriter(conn.getOutputStream());
		reader = new ResponseReader(conn.getInputStream());
	}

	private void write(Request r) throws IOException {
		synchronized(writer) {
			writer.write(r);
		}
	}

	private Response read() throws IOException {
		synchronized (reader) {
			return reader.read();
		}
	}

	public ListenableFuture<Response> send(final Request request) {
		return executorService.submit(new Callable<Response>() {
			@Override
			public Response call() throws Exception {
				write(request);
				return read();
			}
		});
	}

	@Override
	public void close() throws IOException {
		conn.close();
	}
}
