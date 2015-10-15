package io.reon.http;

import java.io.IOException;
import java.io.InputStream;

public class ResponseReader extends MessageReader {

	public ResponseReader(InputStream is) {
		super(is);
	}

	public Response read() throws IOException {
		Response resp = parse(readHeader());
		if (resp == null) return null;
		return ResponseBuilder.with(resp).withBody(input).build();
	}

	public static Response parse(String req) throws HttpBadRequestException {
		if (req == null || req.length() == 0) return null;
		String[] lines = req.split("\\r\\n", 2);
		String[] segments = lines[0].split(" ");
		String protocolVersion = segments[0];
		StatusCode sc = StatusCode.byValue(Integer.parseInt(segments[1]));
		String reason = null;
		if(segments.length>=3) {
			reason = lines[0].substring(lines[0].indexOf(segments[2]));
		}
		Headers headers = Headers.parse(lines[1]);
		return new Response(protocolVersion, sc, reason);
	}
}
