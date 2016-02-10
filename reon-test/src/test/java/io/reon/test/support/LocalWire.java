package io.reon.test.support;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import io.reon.net.Connection;


public class LocalWire {

	private static volatile LocalWire instance;

	private BlockingQueue<Connection> serviceQueue = new ArrayBlockingQueue<>(8);

	public static LocalWire getInstance() {
		synchronized (LocalWire.class) {
			if (instance == null) {
				instance = new LocalWire();
			}
			return instance;
		}
	}

	public BlockingQueue<Connection> getServiceQueue() {
		return serviceQueue;
	}

	public Connection connect() throws IOException {
		PipedOutputStream serversOutputStream = new PipedOutputStream();
		PipedInputStream clientsInputStream = new PipedInputStream(serversOutputStream);

		PipedOutputStream clientsOutputStream = new PipedOutputStream();
		PipedInputStream serversInputStream = new PipedInputStream(clientsOutputStream);

		serviceQueue.offer(new WiredConnection(serversInputStream, serversOutputStream));

		return new WiredConnection(clientsInputStream, clientsOutputStream);
	}

	public Connection waitForConnection() throws InterruptedException {
		Connection c = serviceQueue.poll(10, TimeUnit.SECONDS);
		if (c == null) {
			throw new InterruptedException("Timeout");
		}
		return c;
	}

	public class WiredConnection implements Connection {

		private final InputStream inputStream;
		private final OutputStream outputStream;
		private boolean closed = false;

		public WiredConnection(InputStream inputStream, OutputStream outputStream) {
			this.inputStream = inputStream;
			this.outputStream = outputStream;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return inputStream;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return outputStream;
		}

		@Override
		public boolean isClosed() {
			return closed;
		}

		@Override
		public void close() throws IOException {
			if (!closed) {
				inputStream.close();
				outputStream.close();
				closed = true;
			}
		}
	}

}
