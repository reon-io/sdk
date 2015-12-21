package io.reon;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashSet;

import io.reon.auth.TokenAuth;
import io.reon.http.HttpClient;
import io.reon.http.MimeTypes;
import io.reon.http.RequestBuilder;
import io.reon.http.Response;

public class WebBinder extends Binder {

	private static final String LOG_TAG = WebBinder.class.getSimpleName();

	private final IBinder delegate;
	private final String host;
	private final String prefix;
	private final TokenAuth token;
	private final HttpClient client;
	private final HashSet<DeathRecipient> deathRecipients;
	private boolean alive;

	public WebBinder(IBinder delegate, String host, String prefix, TokenAuth token) throws IOException {
		this.delegate = delegate;
		this.host = host;
		this.prefix = prefix;
		this.token = token;
		LocalSocket ls = new LocalSocket();
		ls.connect(new LocalSocketAddress(HttpClient.DEFAULT_SERVER_ADDR));
		client = new HttpClient(new LocalSocketConnection(ls));
		deathRecipients = new HashSet<>(4);
		alive = true;
	}

	@Override
	public String getInterfaceDescriptor() {
		try {
			return delegate.getInterfaceDescriptor();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	private synchronized void died() {
		if(alive) {
			alive = false;
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					for(DeathRecipient dr : deathRecipients) dr.binderDied();
					deathRecipients.clear();
				}
			},"BinderDeathNotifier").start();
		}
	}

	@Override
	public boolean pingBinder() {
		try {
			Response response = client.send(RequestBuilder
					.post(prefix + "/ping")
					.withHost(host)
					.build());
			if(response.isOK()) return true;
			else died();
		} catch (IOException e) {
			died();
		}
		return false;
	}

	@Override
	public boolean isBinderAlive() {
		return alive;
	}

	@Override
	public IInterface queryLocalInterface(String s) {
		return null;
	}

	@Override
	public void dump(FileDescriptor fileDescriptor, String[] strings) {
		try {
			delegate.dump(fileDescriptor, strings);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void dumpAsync(FileDescriptor fileDescriptor, String[] strings) {
		try {
			delegate.dumpAsync(fileDescriptor, strings);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean onTransact(int opcode, Parcel req, Parcel resp, int flags) throws RemoteException {
		if(!alive) throw new DeadObjectException();
		try {
			Response response = client.send(RequestBuilder
					.post(prefix + "/transact/" + opcode + "/" + flags)
					.withHost(host)
					.withAuth(token.response(TokenAuth.TOKEN_AUTH))
					.withContentType(MimeTypes.MIME_APPLICATION_OCTET_STREAM)
					.withBody(req.marshall())
					.build());
			if(response.isOK()) {
				byte[] body = response.getBody();
				if (body!=null) resp.unmarshall(body,0,body.length);
			} else {
				Log.e(LOG_TAG, "Response: " + response.getStatusCode().toString());
				died();
				throw new RemoteException(response.getStatusCode().toString());
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Transact error", e);
			died();
			throw new RemoteException(e.getMessage());
		}
		return true;
	}

	@Override
	public synchronized void linkToDeath(DeathRecipient deathRecipient, int i) {
		deathRecipients.add(deathRecipient);
	}

	@Override
	public synchronized boolean unlinkToDeath(DeathRecipient deathRecipient, int i) {
		return deathRecipients.remove(deathRecipient);
	}
}

