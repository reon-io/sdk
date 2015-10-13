package io.reon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reon.http.Request;

public class RequestWriter extends AbstractWriter {

	public RequestWriter(OutputStream os) {
		super(os);
	}

	public void write(Request request) throws IOException {
		if (!isClosed() && request!=null) {
			if (request.getBody() instanceof InputStream) writeInputStream(request);
			else writeObject(request);
		}
	}

	private void writeObject(Request request) throws IOException {
		if (request.getBody() != null) {
			byte[] body = request.getBody().toString().getBytes();
			request.setLength(body.length);
			os.write(request.toString().getBytes());
			os.write(body);
		} else {
			request.setLength(0);
			os.write(request.toString().getBytes());
		}
	}

	private void writeInputStream(Request request) throws IOException {

	}
}
