package io.reon;

import android.net.LocalSocket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reon.net.Connection;

public class LocalSocketConnection implements Connection {
	private final LocalSocket localSocket;
	private InputStream is;

	private volatile boolean closed;

	public LocalSocketConnection(LocalSocket localSocket) {
		this.localSocket = localSocket;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		if (is == null) is = new BufferedInputStream(localSocket.getInputStream(), 2048);
		return is;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return localSocket.getOutputStream();
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() throws IOException {
		closed = true;
		localSocket.close();
	}
}
