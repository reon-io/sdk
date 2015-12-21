package io.reon;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import io.reon.auth.TokenAuth;

public abstract class WebService extends android.app.Service {

	private static final String LOG_TAG = WebService.class.getSimpleName();

	public final static String EXTRA_TARGET = "io.reon.uri";

	public final static String EXTRA_TOKEN = "io.reon.token";

	@Override
	public final IBinder onBind(Intent intent) {
		if (intent.hasExtra(EXTRA_TARGET) && intent.hasExtra(EXTRA_TOKEN)) {
			try {
				String realm = getPackageName();
				String target = intent.getStringExtra(EXTRA_TARGET);
				return new WebBinder(getBinder(), target,
						"/" + realm + "/" + getClass().getSimpleName(),
						new TokenAuth(intent.getStringExtra(EXTRA_TOKEN), realm));
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error obtaining web binder", e);
				return null;
			}
		}
		return getBinder();
	}

	protected abstract IBinder getBinder();

}
