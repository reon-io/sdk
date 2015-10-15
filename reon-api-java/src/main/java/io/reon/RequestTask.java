package io.reon;

import io.reon.http.*;

public class RequestTask extends AbstractServerTask {

	private final RequestProcessor processor;

	public RequestTask(Connection socket, RequestProcessor processor) {
		super(socket);
		this.processor = processor;
	}

	@Override
	protected HttpService matchServicePath(String path) {
		return processor;
	}
}
