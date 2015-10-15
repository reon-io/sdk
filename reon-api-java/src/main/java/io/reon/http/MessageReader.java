package io.reon.http;

import java.io.IOException;
import java.io.InputStream;

import static io.reon.http.Message.CRLFx2;

public class MessageReader {
	protected final InputStream input;
	private byte[] buffer = new byte[8192];

	public MessageReader(InputStream is) {
		if(!is.markSupported()) throw new IllegalArgumentException("Mark is not supported!");
		input = is;
	}

	protected String readHeader() throws IOException {
		int length;
		StringBuilder sb = new StringBuilder();
		input.mark(buffer.length);
		while ((length = input.read(buffer)) != -1) {
			// find double EOLs
			String result = new String(buffer, 0, length);
			int idx = result.indexOf(CRLFx2);
			if (idx >= 0) {
				sb.append(result.substring(0, idx));
				idx += 4;
				input.reset();
				input.skip(idx);
				break;
			} else {
				sb.append(result);
				input.mark(buffer.length);
			}
		}
		return sb.toString();
	}
}
