package android.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reon.net.Connection;
import io.reon.test.support.LocalWire;

public class LocalSocket {

	private Connection conn;

	public LocalSocket() {

	}

	LocalSocket(Connection conn) {
		this.conn =  conn;
	}

	public OutputStream getOutputStream() throws IOException {
		if (conn != null) {
			return conn.getOutputStream();
		}
		return null;
	}

	public InputStream getInputStream() throws IOException {
		if (conn != null) {
			return conn.getInputStream();
		}
		return null;
	}

	public FileDescriptor getFileDescriptor() {
        return new FileDescriptor();
    }

	public void shutdownOutput() throws IOException {
		if (conn != null) {
			conn.getOutputStream().close();
		}
	}

	public void close() throws IOException {
		if (conn != null) {
			conn.close();
		}
	}

	public int getSendBufferSize() throws IOException {
		return 1024;
	}

	public void connect(LocalSocketAddress localSocketAddress) throws IOException {
		conn = LocalWire.getInstance().connect();
	}
}
