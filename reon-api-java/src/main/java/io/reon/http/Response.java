package io.reon.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Response {

	private final StatusCode statusCode;

	private String reason;

	private Headers headers;

	Object body;

	String charset;

	private OnCloseListener onCloseListener;
	private OnErrorListener onErrorListener;

	Response(StatusCode statusCode) {
		this.charset = "UTF-8";
		this.statusCode = statusCode;
	}

	public Object getBody() {
		return body;
	}

	public InputStream getBodyAsInputStream() {
		if (body instanceof InputStream) {
			return (InputStream) body;
		} else {
			return new ByteArrayInputStream(body.toString().getBytes());
		}
	}

	public Headers getHeaders() {
		if (headers == null) headers = new Headers();
		return headers;
	}

	public boolean hasLength() {
		return getHeaders().findFirst(Headers.RESPONSE.CONTENT_LEN) != null;
	}

	public String getCharset() {
		return charset;
	}

	public StatusCode getStatusCode() {
		return statusCode;
	}

	public String getContentType() {
		return getHeaders().get(Headers.RESPONSE.CONTENT_TYPE);
	}

	public String getTransferEncoding() {
		return getHeaders().get(Headers.RESPONSE.TRANSFER_ENC);
	}

	public void setLength(long length) {
		if (length < 0) getHeaders().remove(Headers.REQUEST.CONTENT_LEN);
		else getHeaders().update(Headers.RESPONSE.CONTENT_LEN, Long.toString(length));
	}

	public synchronized void setOnCloseListener(OnCloseListener listener) {
		onCloseListener = listener;
	}

	public synchronized void setOnErrorListener(OnErrorListener listener) {
		onErrorListener = listener;
	}

	public final synchronized void onClose() {
		if (onCloseListener != null) onCloseListener.onClose(this);
	}

	public final synchronized void onError(Throwable cause) {
		if (onErrorListener != null) onErrorListener.onError(this, cause);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(statusCode.toString());
		if (reason != null) {
			sb.append(" - ");
			sb.append(reason);
		}
		sb.append("\r\n");
		if (headers != null) sb.append(headers.toString());
		sb.append("\r\n");
		return sb.toString();
	}

}
