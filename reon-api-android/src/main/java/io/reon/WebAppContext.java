package io.reon;

import android.content.ComponentName;
import android.content.Context;

public interface WebAppContext extends WebContext {
	String EXTRA_TOKEN = "io.reon.token";
	String EXTRA_REALM = "io.reon.realm";
	Context getContext();
	Object bindService(ComponentName name);
}
