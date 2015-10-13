package io.reon;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.io.StringWriter;

import io.reon.http.Connection;
import io.reon.http.HttpBadRequestException;
import io.reon.http.HttpException;
import io.reon.http.HttpInternalErrorException;
import io.reon.http.Request;
import io.reon.http.RequestBuilder;
import io.reon.http.Response;
import io.reon.http.ResponseBuilder;

public class RequestTask implements Runnable {

	private final Connection conn;

	private final RequestProcessor processor;

	public RequestTask(Connection socket, RequestProcessor processor) {
		this.conn = socket;
		this.processor = processor;
	}

	private static final int BUFFER_SIZE = 512;

	private static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	private void writeErrorResponse(String requestId, HttpException ex) {
		try {
			ResponseWriter rw = new ResponseWriter(conn.getOutputStream());
			rw.write(ResponseBuilder.error(ex).withId(requestId).withBody(preformatted(getStackTrace(ex))).build());
			rw.close();
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	private static String preformatted(String text) {
		return "<pre>"+text+"</pre>";
	}

	@Override
	public void run() {
		String requestId = null;
		boolean keepAlive = true;
		try {
			PushbackInputStream inputStream;
			try {
				inputStream = new PushbackInputStream(conn.getInputStream(), BUFFER_SIZE);
			} catch (IOException e) {
				throw new HttpBadRequestException(e.getMessage(), e);
			}
			while (keepAlive) {
				Request request;
				try {
					request = readRequest(inputStream);
				} catch (IOException e) {
					throw new HttpBadRequestException(e.getMessage(), e);
				}
				if (request != null) {
					requestId = request.getId();
					writeResponse(processor.processRequest(
							RequestBuilder.req(request).withBody(inputStream).build()));
					keepAlive = request.isKeptAlive();
					// make sure request body has been read
					if (keepAlive) request.readBody();
				} else keepAlive = false;
			}
		} catch (HttpException e) {
			writeErrorResponse(requestId, e);
		} catch (IOException e) {
			writeErrorResponse(requestId, new HttpInternalErrorException(e.getMessage(), e));
		} finally {
			closeConnection();
		}
	}

	private void writeResponse(Response response) throws IOException {
		ResponseWriter rw = new ResponseWriter(conn.getOutputStream());
		try {
			rw.write(response);
		} finally {
			rw.close(response);
		}
	}

	private Request readRequest(PushbackInputStream is) throws IOException, HttpBadRequestException {
		int length;
		byte[] buffer = new byte[BUFFER_SIZE];
		StringBuilder sb = new StringBuilder();
		while ((length = is.read(buffer)) != -1) {
			// find double EOLs
			String result = new String(buffer, 0, length);
			int idx = result.indexOf("\r\n\r\n");
			if (idx >= 0) {
				sb.append(result.substring(0, idx));
				idx += 4;
				int len = length - idx;
				if (len>0) is.unread(buffer, idx, len);
				break;
			} else {
				sb.append(result);
			}
		}
		return Request.parse(sb.toString());
	}

	private void closeConnection() {
		if (conn != null) {
			try {
				conn.getOutputStream().close();
			} catch (IOException e) {
				// ignore any errors when closing output stream
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (IOException e) {
				// ignore
				e.printStackTrace();
			}
		}
	}

}
