package io.reon.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class RequestReader extends MessageReader {

	public RequestReader(InputStream is) {
		super(is);
	}

	public Request read() throws IOException {
		Request req = parse(readHeader());
		if(req==null) return null;
		if(req.getContentLenght() > 0 || req.getContentType() != null)
			RequestBuilder.with(req).withBody(input);
		return req;
	}

	public static Request parse(String req) throws HttpBadRequestException {
		if (req == null || req.length() == 0) return null;
		String[] lines = req.split("\\r\\n", 2);
		String[] segments = lines[0].split(" ");
		Method method = Method.findByName(segments[0]);
		if (method == null) throw new HttpBadRequestException("Invalid HTTP method: " + segments[0]);
		URI uri = URI.create(segments[1]);
		String protocolVersion = segments[2];
		Headers headers = (lines.length > 1) ? Headers.parse(lines[1]) : new Headers();
		return new Request(method, uri, protocolVersion, headers);
	}

}
