package io.reon;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import io.reon.http.Headers;
import io.reon.http.HttpException;
import io.reon.http.Method;
import io.reon.http.MimeTypes;
import io.reon.http.Request;
import io.reon.http.Response;
import io.reon.http.ResponseBuilder;

public class IconEndpoint extends AppEndpoint {

	public IconEndpoint(WebAppContext context) {
		super(context);
	}

	@Override
	public boolean match(Method method, String uri) {
		return httpMethod().equals(method) && originalPath().equalsIgnoreCase(uri);
	}

	public Bitmap drawToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	private String quote(String text) {
		return "\"" + text.replaceAll("\"","\\\"") + "\"";
	}

	@Override
	public Response invoke(String uri, Request request) throws HttpException, IOException {
		PackageManager pm = getContext().getPackageManager();
		CharSequence label = pm.getApplicationLabel(getContext().getApplicationInfo());
		Drawable icon = pm.getApplicationIcon(getContext().getApplicationInfo());
		Bitmap bitmap = drawToBitmap(icon);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
		Response r;
		if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)) {
			r = ResponseBuilder.ok()
					.withHeader(Headers.RESPONSE.ETAG, quote(label.toString()))
					.withContentType(MimeTypes.MIME_IMAGE_PNG)
					.withLength(baos.size())
					.withBody(baos.toByteArray())
					.build();
		} else {
			r = ResponseBuilder.notFound().build();
		}
		baos.close();
		return r;
	}

	@Override
	protected Pattern getPattern() {
		return null;
	}

	@Override
	protected String originalPath() {
		return "/icon.png";
	}

	@Override
	protected Method httpMethod() {
		return Method.GET;
	}
}
