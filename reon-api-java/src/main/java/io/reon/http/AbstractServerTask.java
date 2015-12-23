package io.reon.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class AbstractServerTask implements Runnable {
	private final io.reon.net.Connection conn;
	private RequestReader reader;
	private MessageWriter writer;

	public AbstractServerTask(io.reon.net.Connection conn) {
		this.conn = conn;
	}

	protected abstract HttpService matchServicePath(String path);

	protected abstract Response authorize(Request request) throws HttpException;

	@Override
	public void run() {
		try {
			reader = new RequestReader(conn.getInputStream());
			writer = new MessageWriter(conn.getOutputStream());
			Request request;
			Response response;
			String requestId = null;
			boolean keepAlive = true;
			try {
				while (keepAlive) {
					try {
						request = reader.read();
					} catch (IOException e) {
						throw new HttpBadRequestException(e.getMessage(), e);
					}
					if (request != null) {
						requestId = request.getId();
						response = authorize(request);
						if (response == null) {
							HttpService serv = matchServicePath(request.getURI().getPath());
							if (serv != null) {
								if (request.isContinueExpected()) {
									writer.write(ResponseBuilder
									.startWith(StatusCode.CONTINUE)
									.build());
								}
								response = serv.service(request);
							}
							else response = ResponseBuilder.notFound().build();
						}
						response.setId(requestId);
						writer.write(response);
						keepAlive = !request.shouldClose() && !response.shouldClose();
						// make sure request body has been read
						if (keepAlive) request.readBody();
					} else keepAlive = false;
				}
			} catch (HttpException e) {
				writer.write(ResponseBuilder
						.error(e)
						.withBody(pre(getStackTrace(e)))
						.build());
			} catch (IOException e) {
				writer.write(ResponseBuilder
						.internalError(e)
						.withBody(pre(getStackTrace(e)))
						.build());
			} finally {
				conn.close();
			}
		} catch (IOException iex) {
			iex.printStackTrace();
		}
	}

	private static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	private static String pre(String text) {
		return "<pre>"+text+"</pre>";
	}
}
