package io.reon.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Request extends Message {
	private final Method method;
	URI uri;
	private Map<String,String> parameterMap = null;

	public Request(String url) {
		this(Method.GET, url);
	}

	public Request(Method m, String url) {
		this(m, URI.create(url), HTTP_1_1, new Headers());
	}

	Request(Method method, URI uri, String protocolVersion, Headers headers) {
		super(protocolVersion, headers);
		this.cookies = Cookies.parse(headers);
		this.method = method;
		this.uri = uri;
	}

	public Method getMethod() {
		return method;
	}

	public URI getURI() {
		return uri;
	}

	public boolean isRedirected() {
		return getHeaders().findFirst(Headers.REQUEST.ORIGIN) != null;
	}

	public boolean isContinueExpected() {
		for(Headers.Header h: getHeaders().findAll(Headers.REQUEST.EXPECT)) {
			if (h.getValue().equalsIgnoreCase("100-continue")) return true;
		}
		return false;
	}

	public Map<String, String> getParameterMap() {
		if (parameterMap == null) {
			if (Method.POST.equals(method)) {
				parameterMap = decodeQueryString(getBodyAsString());
			} else {
				parameterMap = decodeQueryString(queryParams(uri.toString()));
			}
		}
		return parameterMap;
	}

	private static Map<String, String> decodeQueryString(String queryParamsStr) {
		if (queryParamsStr == null) {
			return new HashMap<String, String>();
		}
		String[] nameAndValues = queryParamsStr.split("&");
		Map<String, String> result = new HashMap<String, String>();
		for (String nameAndVal : nameAndValues) {
			if (!"".equals(nameAndVal)) {
				int idx = nameAndVal.indexOf("=");
				try {
					if(idx>0)
						result.put(nameAndVal.substring(0,idx),
								URLDecoder.decode(nameAndVal.substring(idx + 1), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	private static String queryParams(String url) {
		int i = url.indexOf("?");
		String queryParams;
		if (i == -1) {
			queryParams = "";
		} else {
			queryParams = url.substring(i + 1);
		}
		return queryParams;
	}

	public static Request parse(String req) throws HttpBadRequestException {
		if (req == null || req.length() == 0) return null;
		String[] lines = req.split("\\r\\n", 2);
		String[] segments = lines[0].split(" ");
		Method method = Method.findByName(segments[0]);
		if (method == null) throw new HttpBadRequestException("Invalid HTTP method: " + segments[0]);
		URI uri = URI.create(segments[1]);
		String protocolVersion = segments[2];
		Headers headers = Headers.parse(lines[1]);
		return new Request(method, uri, protocolVersion, headers);
	}

	public long readBody() throws IOException {
		return readBody(null);
	}

	public long readBody(final byte[] target) throws IOException {
		long len = getContentLenght();
		if (target != null && target.length < len) len = target.length;
		return readBody(target, len);
	}

	public long readBody(final byte[] target, long maxLength) throws IOException {
		if (maxLength <= 0 || body == null) return -1;
		long totalRead = 0;
		InputStream is = getBodyAsInputStream();
		while (totalRead < maxLength) {
			long bytesToRead = maxLength - totalRead;
			int len = BUFFER_LENGTH;
			if (len > bytesToRead) len = (int) bytesToRead;
			if (target != null) {
				int bytesRead = is.read(target, (int) totalRead, len);
				if (bytesRead < 0) break;
				totalRead += bytesRead;
			} else {
				long bytesSkipped = is.skip(maxLength);
				totalRead += bytesSkipped;
			}
		}
		return totalRead;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(method.toString());
		sb.append(' ');
		sb.append(uri.toString());
		sb.append(' ');
		sb.append(getProtocolVersion());
		sb.append(CRLF);
		if (headers != null) sb.append(headers.toString());
		sb.append(CRLF);
		return sb.toString();
	}
}
