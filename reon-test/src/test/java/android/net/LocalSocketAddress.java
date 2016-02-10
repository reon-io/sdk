package android.net;

import java.net.SocketAddress;

public class LocalSocketAddress extends SocketAddress {
	private final String name;

	public LocalSocketAddress() {
		this("test");
	}

	public LocalSocketAddress(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name is empty");
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(obj.toString());
	}

	@Override
	public String toString() {
		return name;
	}
}
