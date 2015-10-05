package io.reon;

import android.content.Context;
import io.reon.http.HttpServiceUnavailableException;

public abstract class AppEndpoint extends Endpoint {
	public AppEndpoint(WebAppContext context) {
		super(context);
	}

	protected Context getContext() {
		return ((WebAppContext)getWebContext()).getContext();
	}

	@Override
	protected ActualParam matchActualParam(FormalParam fp, InvocationContext ic) {
		ActualParam ap = super.matchActualParam(fp, ic);
		if (ap == null) {
			Class<?> fpClazz;
			try {
				fpClazz = classForName(fp.getTypeName());
			} catch (ClassNotFoundException e) {
				throw new HttpServiceUnavailableException(e.getMessage(), e);
			}
			if (fp.getName().equals(getServiceName())) {
				ap = new ActualParam(fpClazz, getServiceObject());
			} else if (Context.class.equals(fpClazz))
				ap = new ActualParam(Context.class, getContext());
		}
		return  ap;
	}
}
