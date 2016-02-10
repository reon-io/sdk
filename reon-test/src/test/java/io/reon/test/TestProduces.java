package io.reon.test;

import io.reon.api.GET;
import io.reon.api.Produces;
import io.reon.http.MimeTypes;

public class TestProduces {
	@GET("/text/:text")
	public String getText(String text) {
		return text;
	}

	@GET("/pdf/:pdf")
	@Produces(MimeTypes.MIME_APPLICATION_PDF)
	@Deprecated
	public byte[] getPdf(String pdf) {
		return pdf.getBytes();
	}

	@GET("/custom/:custom")
	@Produces("something/unusual")
	public byte[] getCustom(String custom) {
		return custom.getBytes();
	}
}
