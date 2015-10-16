package io.reon;

import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

public abstract class WebService extends android.app.Service {
	public final static String EXTRA_TARGET = "uri";

	@Override
	public final IBinder onBind(Intent intent) {
		if (intent.hasExtra(EXTRA_TARGET)) {
			try {
				return new WebBinder(getBinder(), intent.getStringExtra(EXTRA_TARGET));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return getBinder();
	}

	protected abstract IBinder getBinder();

}
