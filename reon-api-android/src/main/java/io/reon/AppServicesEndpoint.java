package io.reon;

import android.content.Context;

public class AppServicesEndpoint extends ServicesEndpoint {
	private final Context ctx;

	public AppServicesEndpoint(WebAppContext context) {
		super(context);
		ctx = context.getContext();
	}

	@Override
	protected String pkgName() {
		return ctx.getPackageName();
	}

	@Override
	protected String appName() {
		CharSequence charSequence = ctx.getApplicationInfo().loadLabel(ctx.getPackageManager());
		return charSequence.toString();
	}
}
