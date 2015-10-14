package io.reon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reon.http.Response;
import io.reon.http.ResponseBuilder;

public class ResponseWriter extends AbstractWriter {

	public ResponseWriter(OutputStream os) throws IOException {
		super(os);
	}

	public void write(Response response) throws IOException {
		if (!isClosed() && response!=null) {
			if (response.getBody() instanceof InputStream) writeInputStream(response);
			else writeObject(response);
		}
	}

	private void writeObject(Response response) throws IOException {
		if (response.getBody() != null) {
			byte[] body = response.getBody().toString().getBytes();
			response.setLength(body.length);
			os.write(response.toString().getBytes());
			os.write(body);
		} else {
			response.setLength(0);
			os.write(response.toString().getBytes());
		}
	}

	private void writeInputStream(Response response) throws IOException {
		InputStream is = (InputStream) response.getBody();
		final boolean chunked = !response.hasLength();
		if (chunked) ResponseBuilder.with(response).withChunks();
		os.write(response.toString().getBytes());
		copy(is, os, chunked);
		is.close();
	}

	public synchronized void close(Response r) throws IOException {
		close();
		if (r != null) r.onClose();
	}
}
