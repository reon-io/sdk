package io.reon;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public abstract class WebService extends android.app.Service {

	private static final String LOG_TAG = WebService.class.getSimpleName();

	public final static String EXTRA_TARGET = "uri";

	@Override
	public final IBinder onBind(Intent intent) {
		if (intent.hasExtra(EXTRA_TARGET)) {
			try {
				return new WebBinder(getBinder(), intent.getStringExtra(EXTRA_TARGET));
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error obtaining web binder", e);
				return null;
			}
		}
		return getBinder();
	}

	protected abstract IBinder getBinder();

}
