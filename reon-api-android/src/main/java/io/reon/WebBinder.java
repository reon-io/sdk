package io.reon;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.FileDescriptor;
import java.io.IOException;

import io.reon.http.Client;
import io.reon.http.MimeTypes;
import io.reon.http.RequestBuilder;
import io.reon.http.Response;

public class WebBinder implements IBinder {

	private static final java.lang.String SOCKET_ADDR = "/tmp/io.reon.server";
	private final IBinder delegate;
	private final String uri;
	private final Client client;
	private volatile boolean alive = true;

	public WebBinder(IBinder delegate, String uri) throws IOException {
		this.delegate = delegate;
		this.uri = uri;
		LocalSocket ls = new LocalSocket();
		ls.connect(new LocalSocketAddress(SOCKET_ADDR));
		client = new Client(new LocalSocketConnection(ls));
	}

	@Override
	public String getInterfaceDescriptor() throws RemoteException {
		return delegate.getInterfaceDescriptor();
	}

	private void died() {
		if(alive) {
			alive = false;
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean pingBinder() {
		try {
			Response response = client.send(RequestBuilder
					.post(uri + "/ping")
					.withKeepAlive()
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
	public void dump(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {
		delegate.dump(fileDescriptor, strings);
	}

	@Override
	public void dumpAsync(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {
		delegate.dumpAsync(fileDescriptor, strings);
	}

	@Override
	public boolean transact(int opcode, Parcel req, Parcel resp, int flags) throws RemoteException {
		try {
			Response response = client.send(RequestBuilder
					.post(uri + "/transact/" + opcode + "/" + flags)
					.withContentType(MimeTypes.MIME_APPLICATION_OCTET_STREAM)
					.withKeepAlive()
					.withBody(req.marshall())
					.build());
			if(response.isOK()) {
				byte[] body = response.getBody();
				if (body!=null) resp.unmarshall(body,0,body.length);
			} else {
				died();
				throw new RemoteException(response.getStatusCode().toString());
			}
		} catch (IOException e) {
			died();
			throw new RemoteException(e.getMessage());
		}
		return true;
	}

	@Override
	public void linkToDeath(DeathRecipient deathRecipient, int i) throws RemoteException {
		delegate.linkToDeath(deathRecipient, i);
	}

	@Override
	public boolean unlinkToDeath(DeathRecipient deathRecipient, int i) {
		return delegate.unlinkToDeath(deathRecipient, i);
	}
}

