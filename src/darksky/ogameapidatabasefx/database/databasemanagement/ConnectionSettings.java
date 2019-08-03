package darksky.ogameapidatabasefx.database.databasemanagement;

public class ConnectionSettings {

	private int connectTimeoutSec = 20;
	private int readTimeoutSec = 60;
	private int retries = 5;

	public ConnectionSettings(int connectTimeoutSec, int readTimeoutSec, int retries) {
		setConnectTimeoutSec(connectTimeoutSec);
		setReadTimeoutSec(readTimeoutSec);
		setRetries(retries);
	}

	public int getConnectTimeoutSec() {
		return connectTimeoutSec;
	}

	public void setConnectTimeoutSec(int connectTimeoutSec) {
		if (connectTimeoutSec > 0)
			this.connectTimeoutSec = connectTimeoutSec;
	}

	public int getReadTimeoutSec() {
		return readTimeoutSec;
	}

	public void setReadTimeoutSec(int readTimeoutSec) {
		if (readTimeoutSec > 0)
			this.readTimeoutSec = readTimeoutSec;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		if (retries > 0)
			this.retries = retries;
	}

	@Override
	public String toString() {
		return "ConnectionSettings [connectTimeoutSec=" + connectTimeoutSec + ", readTimeoutSec=" + readTimeoutSec
				+ ", retries=" + retries + "]";
	}

}
