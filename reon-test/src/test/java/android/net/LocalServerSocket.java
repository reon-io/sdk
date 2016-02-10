package android.net;

import io.reon.net.Connection;
import io.reon.test.support.LocalWire;

import java.io.FileDescriptor;
import java.io.IOException;

public class LocalServerSocket {
	boolean closed = false;

	public LocalServerSocket(String name) {
	}

	public LocalSocket accept() throws IOException {
		if (closed) return null;
		try {
			Connection conn = LocalWire.getInstance().waitForConnection();
			if (closed) {
				LocalWire.getInstance().getServiceQueue().offer(conn);
				return null;
			}
			return new LocalSocket(conn);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	public void close() throws IOException {
		closed = true;
	}

	public FileDescriptor getFileDescriptor() {
		return new FileDescriptor();
	}
}
