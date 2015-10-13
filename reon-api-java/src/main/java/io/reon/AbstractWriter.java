package io.reon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reon.http.Response;

public abstract class AbstractWriter {
	public static final int BUFFER_LENGTH = 32 * 1024;
	protected final OutputStream os;
	private boolean closed = false;

	public AbstractWriter(OutputStream os) {
		this.os = os;
	}

	public static long copy(InputStream from, OutputStream to, boolean chunkEncode) throws IOException {
		byte[] buf = new byte[BUFFER_LENGTH];
		long total = 0;
		while (true) {
			int r = -1;
			boolean tryAgain = true;
			while (tryAgain) {
				try {
					r = from.read(buf);
				} catch (IOException e) {
					if (e.getMessage().startsWith("Try again")) continue;
					e.printStackTrace();
				}
				tryAgain = false;
			}
			if (r == -1 || Thread.interrupted()) {
				if (chunkEncode) to.write("0\r\n\r\n".getBytes());
				break;
			}
			if(chunkEncode) {
				to.write((Integer.toHexString(r)+"\r\n").getBytes());
			}
			to.write(buf, 0, r);
			if(chunkEncode) {
				to.write("\r\n".getBytes());
			}
			total += r;
		}
		return total;
	}

	public void close() throws IOException {
		if (!isClosed()) {
			os.flush();
			closed = true;
		}
	}

	public boolean isClosed() {
		return closed;
	}
}
