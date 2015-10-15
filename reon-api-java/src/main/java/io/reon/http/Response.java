package io.reon.http;

public class Response extends Message {

	private final StatusCode statusCode;

	private final String version;
	private String reason;

	String charset;

	Response(StatusCode statusCode) {
		this(HTTP_1_1, statusCode, null);
	}

	Response(String version, StatusCode statusCode, String reason) {
		super(new Headers());
		this.cookies = Cookies.parseServer(headers);
		this.version = version;
		this.charset = "UTF-8";
		this.statusCode = statusCode;
		this.reason = reason;
	}

	public boolean isOK() {
		return statusCode == StatusCode.OK;
	}

	public String getCharset() {
		return charset;
	}

	public StatusCode getStatusCode() {
		return statusCode;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	private void ensureProperContentType() {
		String contentType = getContentType();
		// if content type is text, make sure charset is specified
		if (contentType!=null && !contentType.contains("charset=") && isTextContent(contentType)) {
			setContentType(contentType + "; charset=" + getCharset());
		}
	}

	private static boolean isTextContent(String content) {
		return content.contains("text") || content.contains("xml") || content.contains("json");
	}

	@Override
	public String toString() {
		ensureProperContentType();
		StringBuilder sb = new StringBuilder();
		sb.append(version);
		sb.append(' ');
		sb.append(statusCode.toString());
		if (reason != null) {
			sb.append(" - ");
			sb.append(reason);
		}
		sb.append(super.toString());
		return sb.toString();
	}
}
