package io.reon;

import io.reon.http.*;

public class RequestTask extends AbstractServerTask {

	private final WebContext context;

	public RequestTask(Connection conn, WebContext context) {
		super(conn);
		this.context = context;
	}

	@Override
	protected HttpService matchServicePath(String path) {
		return context.getHttpService();
	}

	@Override
	protected Response authorize(Request request) throws HttpException {
		String authToken = request.getHeaders().get(Headers.REQUEST.AUTH);
		if (authToken == null) {
			if(context.getPackage() != "test") // allow testing without auth
				return ResponseBuilder.unauthorized()
						.withHeader(Headers.RESPONSE.WWW_AUTH, "Ticket realm=\""+context.getPackage()+"\"")
						.withKeepAlive().build();
		} else {
			if(!context.getAuthToken().equals(authToken))
				return ResponseBuilder.forbidden().withClose().build();
		}
		return null;
	}
}
