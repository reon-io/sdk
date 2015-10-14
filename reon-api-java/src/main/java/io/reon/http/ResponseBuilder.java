package io.reon.http;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResponseBuilder extends Builder<Response> {

	private ResponseBuilder(Response response) {
		that = response;
	}

	public static ResponseBuilder startWith(StatusCode statusCode) {
		return new ResponseBuilder(new Response(statusCode));
	}

	public static ResponseBuilder with(Response response) {
		return new ResponseBuilder(response);
	}

	public static ResponseBuilder ok() {
		return startWith(StatusCode.OK);
	}

	public static ResponseBuilder notFound() {
		return startWith(StatusCode.NOT_FOUND).withClose();
	}

	public static ResponseBuilder error(HttpException ex) {
		return startWith(ex.getStatusCode()).withClose();
	}

	public static ResponseBuilder methodNotAllowed() {
		return startWith(StatusCode.METHOD_NOT_ALLOWED).withClose();
	}

	public static ResponseBuilder unauthorized() {
		return startWith(StatusCode.UNAUTHORIZED).withClose();
	}

	public static ResponseBuilder serviceUnavailable() {
		return startWith(StatusCode.SERVICE_UNAVAILABLE).withClose();
	}

	public static ResponseBuilder internalError() {
		return startWith(StatusCode.INTERNAL_ERROR).withClose();
	}

	public static ResponseBuilder forbidden() {
		return startWith(StatusCode.FORBIDDEN);
	}

	public static ResponseBuilder found(String location) {
		return startWith(StatusCode.FOUND).withLocation(location);
	}

	private ResponseBuilder withLocation(String location) {
		return withUpdatedHeader(Headers.RESPONSE.LOCATION, location);
	}

	public ResponseBuilder withHeader(String name, String value) {
		that.getHeaders().add(name, value);
		return this;
	}

	public ResponseBuilder withUpdatedHeader(String name, String value) {
		that.getHeaders().update(name, value);
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

	public ResponseBuilder withReason(String reason) {
		that.setReason(reason);
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
		that.body = s;
		return withLength(s.getBytes().length);
	}

	public ResponseBuilder withBody(InputStream is) {
		that.body = is;
		return this;
	}

	public ResponseBuilder withBody(byte[] data) {
		return this.withBody(new ByteArrayInputStream(data)).withLength(data.length);
	}

	public ResponseBuilder withBody(JSONObject jsonObject) {
		that.body = jsonObject;
		return withContentType(MimeTypes.MIME_APPLICATION_JSON);
	}

	public ResponseBuilder withBody(JSONArray jsonObject) {
		that.body = jsonObject;
		return withContentType(MimeTypes.MIME_APPLICATION_JSON);
	}

	public ResponseBuilder withBody(Response r) {
		that = r;
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
		that.charset = charset;
		return this;
	}
}
