package io.reon;

import android.content.ComponentName;
import android.content.Context;

public interface WebAppContext extends WebContext {
	Context getContext();
	Object bindService(ComponentName name);
}
