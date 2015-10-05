package io.reon;

import android.net.LocalSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reon.http.Connection;

public class LocalSocketConnection implements Connection {
	private final LocalSocket localSocket;

	public LocalSocketConnection(LocalSocket localSocket) {
		this.localSocket = localSocket;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return localSocket.getInputStream();
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
