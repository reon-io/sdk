package io.reon;

import android.content.ComponentName;
import android.content.Context;

public interface WebAppContext extends WebContext {
	public static final String EXTRA_AUTH = "io.reon.server.auth";
	public static final String EXTRA_APP = "io.reon.server.app";
	Context getContext();
	Object bindService(ComponentName name);
}
