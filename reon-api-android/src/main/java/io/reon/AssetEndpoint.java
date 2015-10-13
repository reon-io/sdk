package io.reon;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import io.reon.http.Method;
import io.reon.http.Request;
import io.reon.http.Response;
import io.reon.http.ResponseBuilder;

public class AssetEndpoint extends AppEndpoint {

	public static final String REON_ASSETS_DIR = "reon";

	public AssetEndpoint(WebAppContext ctx) {
		super(ctx);
	}

	@Override
	protected Method httpMethod() {
		return Method.GET;
	}

	@Override
	protected String originalPath() {
		return "/"; // doesn't matter actually
	}

	@Override
	protected Pattern getPattern() {
		return null; // doesn't matter actually
	}

	@Override
	public boolean match(Method method, String uri) {
		if (Method.GET == method) {
			AssetManager assetManager = getContext().getAssets();
			try {
				assetManager.open(REON_ASSETS_DIR + uri).close();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	@Override
	public Response invoke(String uri, Request request) throws IOException {
		AssetManager assetManager = getContext().getAssets();
		InputStream is = assetManager.open(REON_ASSETS_DIR + uri);
		long length = getWebContext().getAssetInfo().getAssetLength(uri);
		return ResponseBuilder
				.ok()
				.withId(request.getId())
				.withContentTypeFrom(uri)
				.withLength(length)
				.withBody(is).build();
	}


}
