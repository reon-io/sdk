package io.reon;

import android.content.ComponentName;
import android.content.Context;

public interface WebAppContext extends WebContext {
	String EXTRA_TOKEN = "io.reon.server.token";
	String EXTRA_REALM = "io.reon.server.realm";
	Context getContext();
	Object bindService(ComponentName name);
}
