package io.reon.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Message {
	public static final String CRLF = "\r\n";
	public static final String CRLFx2 = "\r\n\r\n";
	public static final int BUFFER_LENGTH = 32 * 1024;
	public static final int IN_MEMORY_LIMIT = 2 * 1024 * 1024; // 2 MB
	public static final String HTTP_1_1 = "HTTP/1.1";
	public static final String CHUNKED = "chunked";
	public static final String IDENTITY = "identity";
	public static final String KEEP_ALIVE = "keep-alive";
	protected final Headers headers;
	protected Cookies cookies;
	Object body; // can be InputStream or byte[]
	private long contentLength = -1;
	private OnCloseListener onCloseListener;
	private OnErrorListener onErrorListener;

	public Message(Headers headers) {
		this.headers = headers;
	}

	public boolean isCached() {
		return  body != null && (body instanceof byte[]);
	}

	public long getContentLenght() {
		if (contentLength < 0 && headers != null) {
			String lenStr = headers.get(Headers.REQUEST.CONTENT_LEN);
			if (lenStr != null) contentLength = Long.parseLong(lenStr);
		}
		return contentLength;
	}

	public void setContentLength(long length) {
		if (length < 0) getHeaders().remove(Headers.REQUEST.CONTENT_LEN);
		else getHeaders().update(Headers.REQUEST.CONTENT_LEN, Long.toString(length));
		contentLength = length;
	}

	public void setContentType(String contentType) {
		getHeaders().update(Headers.REQUEST.CONTENT_TYPE, contentType);
	}

	public String getContentType() {
		return getHeaders().get(Headers.REQUEST.CONTENT_TYPE);
	}

	public boolean isKeptAlive() {
		return KEEP_ALIVE.equalsIgnoreCase(getHeaders().get(Headers.REQUEST.CONNECTION));
	}

	public boolean isChunked() {
		return CHUNKED.equals(getHeaders().get(Headers.REQUEST.TRANSFER_ENC));
	}

	public void setChunked() {
		getHeaders().update(Headers.REQUEST.TRANSFER_ENC, CHUNKED);
	}

	public Headers getHeaders() {
		return headers;
	}

	public Cookies getCookies() {
		return cookies;
	}

	public String getId() {
		return getHeaders().get(Headers.X.REON_ID);
	}

	public void setId(String id) {
		getHeaders().update(Headers.X.REON_ID, id);
	}

	public byte[] getBody() {
		if(isCached()) return (byte[])body;
		return null;
	}

	public InputStream getBodyAsInputStream() {
		if (isCached()) return new ByteArrayInputStream((byte[])body);
		// TODO create filter for input stream to monitor how many bytes has been read
		return (InputStream) body;
	}

	public String getBodyAsString() {
		if (isCached()) return new String((byte[])body);
		return null;
	}

	public void setBody(InputStream is) {
		body = is;
	}

	public void setBody(JSONObject obj) {
		setBody(obj.toString().getBytes());
		setContentType(MimeTypes.MIME_APPLICATION_JSON);
	}

	public void setBody(JSONArray obj) {
		setBody(obj.toString().getBytes());
		setContentType(MimeTypes.MIME_APPLICATION_JSON);
	}

	public void setBody(String s) {
		setBody(s.getBytes());
		setContentType(MimeTypes.MIME_TEXT_HTML);
	}

	public void setBody(byte[] obj) {
		body = obj;
		setContentLength(obj.length);
	}

	public JSONObject getBodyAsJSON() {
		JSONObject jsonBody = null;
		try {
			jsonBody = new JSONObject(getBodyAsString());
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		return jsonBody;
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

	public String getTransferEncoding() {
		return getHeaders().get(Headers.RESPONSE.TRANSFER_ENC);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(CRLF);
		if (headers != null) sb.append(headers.toString());
		sb.append(CRLF);
		return sb.toString();
	}
}
