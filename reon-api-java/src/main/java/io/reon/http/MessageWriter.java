package io.reon.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.reon.http.Message.CRLF;
import static io.reon.http.Message.CRLFx2;

public class MessageWriter {
	public static final int BUFFER_LENGTH = 32 * 1024;
	private final OutputStream os;
	private boolean closed = false;

	public MessageWriter(OutputStream os) throws IOException {
		this.os = os;
	}

	public void write(Message message) throws IOException {
		if (message != null) {
			if (message.getBodyAsInputStream() != null && message.getContentLenght()<0)
				message.setChunked();
			os.write(message.toString().getBytes());
			InputStream is = message.getBodyAsInputStream();
			if (is != null) copy(is, os, message.isChunked());
			os.flush();
			if (!message.isKeptAlive()) os.close();
			message.onClose();
		}
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
				if (chunkEncode) to.write(("0" + CRLFx2).getBytes());
				break;
			}
			if(chunkEncode) {
				to.write((Integer.toHexString(r)+CRLF).getBytes());
			}
			to.write(buf, 0, r);
			if(chunkEncode) {
				to.write(CRLF.getBytes());
			}
			total += r;
		}
		return total;
	}

}
