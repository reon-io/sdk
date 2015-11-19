package io.reon.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketConnection implements Connection {
	private static final int BUFFER_SIZE = 2048;
	private final Socket socket;
	private volatile InputStream is;

	public SocketConnection(Socket socket) {
		this.socket = socket;
	}
	@Override
	public InputStream getInputStream() throws IOException {
		if (is == null) is = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
		return is;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}
}
