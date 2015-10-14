package io.reon.http;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class RequestBuilder extends Builder<Request> {

	private RequestBuilder(Request request) {
		that = request;
	}

	public static RequestBuilder with(Request request) {
		return new RequestBuilder(request);
	}

	public static RequestBuilder startWith(Method method, String uri) {
		return new RequestBuilder(new Request(method, uri)).withHost("localhost");
	}

	public static RequestBuilder get(String uri) {
		return startWith(Method.GET, uri);
	}

	public static RequestBuilder put(String uri) {
		return startWith(Method.PUT, uri);
	}

	public static RequestBuilder post(String uri) {
		return startWith(Method.POST, uri);
	}

	public static RequestBuilder delete(String uri) {
		return startWith(Method.DELETE, uri);
	}

	public static RequestBuilder head(String uri) {
		return startWith(Method.HEAD, uri);
	}

	public static RequestBuilder trace(String uri) {
		return startWith(Method.TRACE, uri);
	}

	public static RequestBuilder connect(String uri) {
		return startWith(Method.CONNECT, uri);
	}

	public static RequestBuilder options(String uri) {
		return startWith(Method.OPTIONS, uri);
	}

	public RequestBuilder withHeader(String name, String value) {
		that.getHeaders().add(name, value);
		return this;
	}

	public RequestBuilder withHeaders(String headers) {
		that.getHeaders().add(Headers.parse(headers));
		return this;
	}

	public RequestBuilder withUpdatedHeader(String name, String value) {
		that.getHeaders().update(name, value);
		return this;
	}

	public RequestBuilder withId(String id) {
		if (id != null) withUpdatedHeader(Headers.X.REON_ID, id);
		return this;
	}

	public RequestBuilder withKeepAlive() {
		return withUpdatedHeader(Headers.REQUEST.CONNECTION, "keep-alive");
	}

	public RequestBuilder withUserAgent(String agent) {
		return withUpdatedHeader(Headers.REQUEST.USER_AGENT, agent);
	}

	public RequestBuilder withHost(String host) {
		return withUpdatedHeader(Headers.REQUEST.HOST, host);
	}

	public RequestBuilder withFrom(String from) {
		return withUpdatedHeader(Headers.REQUEST.FROM, from);
	}

	public RequestBuilder withAuth(String auth) {
		return withUpdatedHeader(Headers.REQUEST.AUTH, auth);
	}

	public RequestBuilder withChunks() {
		return withUpdatedHeader(Headers.REQUEST.TRANSFER_ENC, "chunked");
	}

	public RequestBuilder withReferer(String referer) {
		return withUpdatedHeader(Headers.REQUEST.REFERER, referer);
	}

	public RequestBuilder withContentType(String contentType) {
		return withUpdatedHeader(Headers.REQUEST.CONTENT_TYPE, contentType);
	}

	public RequestBuilder withLength(long length) {
		return withUpdatedHeader(Headers.RESPONSE.CONTENT_LEN, Long.toString(length));
	}

	public RequestBuilder withCookie(Cookie cookie) {
		return withHeader(Headers.REQUEST.COOKIE, cookie.toString());
	}

	public RequestBuilder withCookies(Cookies cookies) {
		for (Cookie cookie : cookies.all()) withCookie(cookie);
		return this;
	}

	public RequestBuilder withBody(String s) {
		that.stringBody = s;
		if (that.getContentLenght()<0) withLength(s.getBytes().length);
		if (that.getHeaders().findFirst(Headers.REQUEST.CONTENT_TYPE)==null)
			withContentType(MimeTypes.MIME_TEXT_HTML);
		return this;
	}

	public RequestBuilder withBody(JSONObject json) {
		that.jsonBody = json;
		if (that.getContentLenght()<0) withLength(json.toString().getBytes().length);
		if (that.getHeaders().findFirst(Headers.REQUEST.CONTENT_TYPE)==null)
			withContentType(MimeTypes.MIME_APPLICATION_JSON);
		return this;
	}

	public RequestBuilder withRedirection(URI u) {
		if (u != null) {
			that.getHeaders().update(Headers.REQUEST.ORIGIN, that.getURI().toString());
			that.uri = u;
		}
		return this;
	}

	public RequestBuilder withBody(InputStream is) throws IOException {
		// always read body
		that.body = is;
		long length = that.getContentLenght();
		if (length > 0 && length <= that.IN_MEMORY_LIMIT) {
			that.cachedBody = new byte[(int) that.getContentLenght()];
			that.readBody(that.cachedBody);
			that.body = null;
		} else if(that.isChunked()) {
			that.body = new ChunkedInputStream(is);
		}
		return this;
	}

	public RequestBuilder withBody(File file) throws IOException {
		return withBody(new FileInputStream(file)).withLength(file.length()).withContentType(file.getName());
	}

}
