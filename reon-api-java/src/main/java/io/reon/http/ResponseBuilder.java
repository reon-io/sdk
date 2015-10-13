package io.reon.http;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResponseBuilder {
	private Response response;

	private ResponseBuilder(Response response) {
		this.response = response;
	}

	private static ResponseBuilder newWithStatusCode(StatusCode statusCode) {
		return new ResponseBuilder(new Response(statusCode));
	}

	public static ResponseBuilder resp(Response response) {
		return new ResponseBuilder(response);
	}

	public static ResponseBuilder ok() {
		return newWithStatusCode(StatusCode.OK);
	}

	public static ResponseBuilder notFound() {
		return newWithStatusCode(StatusCode.NOT_FOUND).withClose();
	}

	public static ResponseBuilder error(HttpException ex) {
		return newWithStatusCode(ex.getStatusCode()).withClose();
	}

	public static ResponseBuilder methodNotAllowed() {
		return newWithStatusCode(StatusCode.METHOD_NOT_ALLOWED).withClose();
	}

	public static ResponseBuilder serviceUnavailable() {
		return newWithStatusCode(StatusCode.SERVICE_UNAVAILABLE).withClose();
	}

	public static ResponseBuilder internalError() {
		return newWithStatusCode(StatusCode.INTERNAL_ERROR).withClose();
	}

	public static ResponseBuilder forbidden() {
		return newWithStatusCode(StatusCode.FORBIDDEN);
	}

	public ResponseBuilder withHeader(String name, String value) {
		response.getHeaders().add(name, value);
		return this;
	}

	public ResponseBuilder withUpdatedHeader(String name, String value) {
		response.getHeaders().update(name, value);
		return this;
	}

	public ResponseBuilder withId(String id) {
		if (id != null) withUpdatedHeader(Headers.X.REON_ID, id);
		return this;
	}

	public ResponseBuilder withKeepAlive() {
		return withUpdatedHeader(Headers.RESPONSE.CONNECTION, "keep-alive");
	}

	public ResponseBuilder withClose() {
		return withUpdatedHeader(Headers.RESPONSE.CONNECTION, "close");
	}

	public ResponseBuilder withCookie(Cookie cookie) {
		return withHeader(Headers.RESPONSE.SET_COOKIE, cookie.toString());
	}

	public ResponseBuilder withCookies(Cookies cookies) {
		for (Cookie cookie : cookies.all()) withCookie(cookie);
		return this;
	}

	public ResponseBuilder withContentType(String contentType) {
		return withUpdatedHeader(Headers.RESPONSE.CONTENT_TYPE, contentType);
	}

	public ResponseBuilder withContentTypeFrom(String filename) {
		return withContentType(MimeTypes.getMimeType(filename));
	}

	public ResponseBuilder withBody(File file) throws FileNotFoundException {
		return withBody(new FileInputStream(file)).withLength(file.length()).withContentType(file.getName());
	}

	public ResponseBuilder withBody(String s) {
		response.body = s;
		return withLength(s.getBytes().length);
	}

	public ResponseBuilder withBody(InputStream is) {
		response.body = is;
		return this;
	}

	public ResponseBuilder withBody(byte[] data) {
		return this.withBody(new ByteArrayInputStream(data)).withLength(data.length);
	}

	public ResponseBuilder withBody(JSONObject jsonObject) {
		response.body = jsonObject;
		return withContentType(MimeTypes.MIME_APPLICATION_JSON);
	}

	public ResponseBuilder withBody(JSONArray jsonObject) {
		response.body = jsonObject;
		return withContentType(MimeTypes.MIME_APPLICATION_JSON);
	}

	public ResponseBuilder withBody(Response r) {
		response = r;
		return this;
	}

	public ResponseBuilder withLength(long length) {
		return withUpdatedHeader(Headers.RESPONSE.CONTENT_LEN, Long.toString(length));
	}

	public ResponseBuilder withChunks() {
		return withTransferEncoding("chunked");
	}

	public ResponseBuilder withIdentity() {
		return withTransferEncoding("identity");
	}

	public ResponseBuilder withTransferEncoding(String value) {
		return withUpdatedHeader(Headers.RESPONSE.TRANSFER_ENC, value);
	}

	public ResponseBuilder withCharset(String charset) {
		response.charset = charset;
		return this;
	}

	public Response build() {
		return response;
	}
}
