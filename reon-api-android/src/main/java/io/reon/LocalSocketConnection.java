package io.reon;

import android.net.LocalSocket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reon.http.Connection;

public class LocalSocketConnection implements Connection {
	private final LocalSocket localSocket;
	private InputStream is;

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
		return localSocket.isClosed();
	}

	@Override
	public void close() throws IOException {
		localSocket.close();
	}
}
