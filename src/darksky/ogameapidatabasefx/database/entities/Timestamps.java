package darksky.ogameapidatabasefx.database.entities;

import java.time.Instant;

public class Timestamps {

	private Instant updateTimestamp;
	private Instant serverDataTimestamp;
	private Instant playersTimestamp;
	private Instant universeTimestamp;
	private Instant alliancesTimestamp;
	private Instant highscore10Timestamp;
	private Instant highscore11Timestamp;
	private Instant highscore12Timestamp;
	private Instant highscore13Timestamp;
	private Instant highscore14Timestamp;
	private Instant highscore15Timestamp;
	private Instant highscore16Timestamp;
	private Instant highscore17Timestamp;
	private Instant highscore20Timestamp;
	private Instant highscore21Timestamp;
	private Instant highscore22Timestamp;
	private Instant highscore23Timestamp;
	private Instant highscore24Timestamp;
	private Instant highscore25Timestamp;
	private Instant highscore26Timestamp;
	private Instant highscore27Timestamp;

	public Timestamps(Instant updateTimestamp, Instant serverDataTimestamp, Instant playersTimestamp, Instant universeTimestamp,
			Instant alliancesTimestamp, Instant highscore10Timestamp, Instant highscore11Timestamp, Instant highscore12Timestamp,
			Instant highscore13Timestamp, Instant highscore14Timestamp, Instant highscore15Timestamp, Instant highscore16Timestamp,
			Instant highscore17Timestamp, Instant highscore20Timestamp, Instant highscore21Timestamp, Instant highscore22Timestamp,
			Instant highscore23Timestamp, Instant highscore24Timestamp, Instant highscore25Timestamp, Instant highscore26Timestamp,
			Instant highscore27Timestamp) {
		super();
		this.updateTimestamp = updateTimestamp;
		this.serverDataTimestamp = serverDataTimestamp;
		this.playersTimestamp = playersTimestamp;
		this.universeTimestamp = universeTimestamp;
		this.alliancesTimestamp = alliancesTimestamp;
		this.highscore10Timestamp = highscore10Timestamp;
		this.highscore11Timestamp = highscore11Timestamp;
		this.highscore12Timestamp = highscore12Timestamp;
		this.highscore13Timestamp = highscore13Timestamp;
		this.highscore14Timestamp = highscore14Timestamp;
		this.highscore15Timestamp = highscore15Timestamp;
		this.highscore16Timestamp = highscore16Timestamp;
		this.highscore17Timestamp = highscore17Timestamp;
		this.highscore20Timestamp = highscore20Timestamp;
		this.highscore21Timestamp = highscore21Timestamp;
		this.highscore22Timestamp = highscore22Timestamp;
		this.highscore23Timestamp = highscore23Timestamp;
		this.highscore24Timestamp = highscore24Timestamp;
		this.highscore25Timestamp = highscore25Timestamp;
		this.highscore26Timestamp = highscore26Timestamp;
		this.highscore27Timestamp = highscore27Timestamp;
	}

	public Instant getUpdateTimestamp() {
		return updateTimestamp;
	}

	public Instant getServerDataTimestamp() {
		return serverDataTimestamp;
	}

	public Instant getPlayersTimestamp() {
		return playersTimestamp;
	}

	public Instant getUniverseTimestamp() {
		return universeTimestamp;
	}

	public Instant getAlliancesTimestamp() {
		return alliancesTimestamp;
	}	

	public void setUpdateTimestamp(Instant updateTimestamp) {
		this.updateTimestamp = updateTimestamp;
	}

	public void setServerDataTimestamp(Instant serverDataTimestamp) {
		this.serverDataTimestamp = serverDataTimestamp;
	}

	public void setPlayersTimestamp(Instant playersTimestamp) {
		this.playersTimestamp = playersTimestamp;
	}

	public void setUniverseTimestamp(Instant universeTimestamp) {
		this.universeTimestamp = universeTimestamp;
	}

	public void setAlliancesTimestamp(Instant alliancesTimestamp) {
		this.alliancesTimestamp = alliancesTimestamp;
	}

	public void setHighscore10Timestamp(Instant highscore10Timestamp) {
		this.highscore10Timestamp = highscore10Timestamp;
	}

	public void setHighscore11Timestamp(Instant highscore11Timestamp) {
		this.highscore11Timestamp = highscore11Timestamp;
	}

	public void setHighscore12Timestamp(Instant highscore12Timestamp) {
		this.highscore12Timestamp = highscore12Timestamp;
	}

	public void setHighscore13Timestamp(Instant highscore13Timestamp) {
		this.highscore13Timestamp = highscore13Timestamp;
	}

	public void setHighscore14Timestamp(Instant highscore14Timestamp) {
		this.highscore14Timestamp = highscore14Timestamp;
	}

	public void setHighscore15Timestamp(Instant highscore15Timestamp) {
		this.highscore15Timestamp = highscore15Timestamp;
	}

	public void setHighscore16Timestamp(Instant highscore16Timestamp) {
		this.highscore16Timestamp = highscore16Timestamp;
	}

	public void setHighscore17Timestamp(Instant highscore17Timestamp) {
		this.highscore17Timestamp = highscore17Timestamp;
	}

	public void setHighscore20Timestamp(Instant highscore20Timestamp) {
		this.highscore20Timestamp = highscore20Timestamp;
	}

	public void setHighscore21Timestamp(Instant highscore21Timestamp) {
		this.highscore21Timestamp = highscore21Timestamp;
	}

	public void setHighscore22Timestamp(Instant highscore22Timestamp) {
		this.highscore22Timestamp = highscore22Timestamp;
	}

	public void setHighscore23Timestamp(Instant highscore23Timestamp) {
		this.highscore23Timestamp = highscore23Timestamp;
	}

	public void setHighscore24Timestamp(Instant highscore24Timestamp) {
		this.highscore24Timestamp = highscore24Timestamp;
	}

	public void setHighscore25Timestamp(Instant highscore25Timestamp) {
		this.highscore25Timestamp = highscore25Timestamp;
	}

	public void setHighscore26Timestamp(Instant highscore26Timestamp) {
		this.highscore26Timestamp = highscore26Timestamp;
	}

	public void setHighscore27Timestamp(Instant highscore27Timestamp) {
		this.highscore27Timestamp = highscore27Timestamp;
	}
	
	public Timestamps setHighscoreTimestamp(int category, int type, Instant timestamp) {
		if (category < 1 || category > 2 || type < 0 || type > 7) {
			throw new IllegalArgumentException(
					"category and or type not in range");
		}

		if (category == 1) {
			switch (type) {
			case 0:
				setHighscore10Timestamp(timestamp);
				break;
			case 1:
				setHighscore11Timestamp(timestamp);
				break;
			case 2:
				setHighscore12Timestamp(timestamp);
				break;
			case 3:
				setHighscore13Timestamp(timestamp);
				break;
			case 4:
				setHighscore14Timestamp(timestamp);
				break;
			case 5:
				setHighscore15Timestamp(timestamp);
				break;
			case 6:
				setHighscore16Timestamp(timestamp);
				break;
			case 7:
				setHighscore17Timestamp(timestamp);
				break;
			default:
				throw new IllegalArgumentException(
						"type not in range");
			}
		} else {
			switch (type) {
			case 0:
				setHighscore20Timestamp(timestamp);
				break;
			case 1:
				setHighscore21Timestamp(timestamp);
				break;
			case 2:
				setHighscore22Timestamp(timestamp);
				break;
			case 3:
				setHighscore23Timestamp(timestamp);
				break;
			case 4:
				setHighscore24Timestamp(timestamp);
				break;
			case 5:
				setHighscore25Timestamp(timestamp);
				break;
			case 6:
				setHighscore26Timestamp(timestamp);
				break;
			case 7:
				setHighscore27Timestamp(timestamp);
				break;
			default:
				throw new IllegalArgumentException(
						"type not in range");
			}
		}
		return this;
	}

	public Instant getHighscoreTimestamp(int category, int type) {
		if (category < 1 || category > 2 || type < 0 || type > 7) {
			throw new IllegalArgumentException(
					"Error in DatabaseInformation#getHighscoreTimestamp. category and or type not in range");
		}

		if (category == 1) {
			switch (type) {
			case 0:
				return this.getHighscore10Timestamp();
			case 1:
				return this.getHighscore11Timestamp();
			case 2:
				return this.getHighscore12Timestamp();
			case 3:
				return this.getHighscore13Timestamp();
			case 4:
				return this.getHighscore14Timestamp();
			case 5:
				return this.getHighscore15Timestamp();
			case 6:
				return this.getHighscore16Timestamp();
			case 7:
				return this.getHighscore17Timestamp();
			default:
				throw new IllegalArgumentException(
						"Error in DatabaseInformation#getHighscoreTimestamp. type not in range");
			}
		} else {
			switch (type) {
			case 0:
				return this.getHighscore20Timestamp();
			case 1:
				return this.getHighscore21Timestamp();
			case 2:
				return this.getHighscore22Timestamp();
			case 3:
				return this.getHighscore23Timestamp();
			case 4:
				return this.getHighscore24Timestamp();
			case 5:
				return this.getHighscore25Timestamp();
			case 6:
				return this.getHighscore26Timestamp();
			case 7:
				return this.getHighscore27Timestamp();
			default:
				throw new IllegalArgumentException(
						"Error in DatabaseInformation#getHighscoreTimestamp. type not in range");
			}
		}

	}
	
	

	public Instant getHighscore10Timestamp() {
		return highscore10Timestamp;
	}

	public Instant getHighscore11Timestamp() {
		return highscore11Timestamp;
	}

	public Instant getHighscore12Timestamp() {
		return highscore12Timestamp;
	}

	public Instant getHighscore13Timestamp() {
		return highscore13Timestamp;
	}

	public Instant getHighscore14Timestamp() {
		return highscore14Timestamp;
	}

	public Instant getHighscore15Timestamp() {
		return highscore15Timestamp;
	}

	public Instant getHighscore16Timestamp() {
		return highscore16Timestamp;
	}

	public Instant getHighscore17Timestamp() {
		return highscore17Timestamp;
	}

	public Instant getHighscore20Timestamp() {
		return highscore20Timestamp;
	}

	public Instant getHighscore21Timestamp() {
		return highscore21Timestamp;
	}

	public Instant getHighscore22Timestamp() {
		return highscore22Timestamp;
	}

	public Instant getHighscore23Timestamp() {
		return highscore23Timestamp;
	}

	public Instant getHighscore24Timestamp() {
		return highscore24Timestamp;
	}

	public Instant getHighscore25Timestamp() {
		return highscore25Timestamp;
	}

	public Instant getHighscore26Timestamp() {
		return highscore26Timestamp;
	}

	public Instant getHighscore27Timestamp() {
		return highscore27Timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alliancesTimestamp == null) ? 0 : alliancesTimestamp.hashCode());
		result = prime * result + ((highscore10Timestamp == null) ? 0 : highscore10Timestamp.hashCode());
		result = prime * result + ((highscore11Timestamp == null) ? 0 : highscore11Timestamp.hashCode());
		result = prime * result + ((highscore12Timestamp == null) ? 0 : highscore12Timestamp.hashCode());
		result = prime * result + ((highscore13Timestamp == null) ? 0 : highscore13Timestamp.hashCode());
		result = prime * result + ((highscore14Timestamp == null) ? 0 : highscore14Timestamp.hashCode());
		result = prime * result + ((highscore15Timestamp == null) ? 0 : highscore15Timestamp.hashCode());
		result = prime * result + ((highscore16Timestamp == null) ? 0 : highscore16Timestamp.hashCode());
		result = prime * result + ((highscore17Timestamp == null) ? 0 : highscore17Timestamp.hashCode());
		result = prime * result + ((highscore20Timestamp == null) ? 0 : highscore20Timestamp.hashCode());
		result = prime * result + ((highscore21Timestamp == null) ? 0 : highscore21Timestamp.hashCode());
		result = prime * result + ((highscore22Timestamp == null) ? 0 : highscore22Timestamp.hashCode());
		result = prime * result + ((highscore23Timestamp == null) ? 0 : highscore23Timestamp.hashCode());
		result = prime * result + ((highscore24Timestamp == null) ? 0 : highscore24Timestamp.hashCode());
		result = prime * result + ((highscore25Timestamp == null) ? 0 : highscore25Timestamp.hashCode());
		result = prime * result + ((highscore26Timestamp == null) ? 0 : highscore26Timestamp.hashCode());
		result = prime * result + ((highscore27Timestamp == null) ? 0 : highscore27Timestamp.hashCode());
		result = prime * result + ((playersTimestamp == null) ? 0 : playersTimestamp.hashCode());
		result = prime * result + ((serverDataTimestamp == null) ? 0 : serverDataTimestamp.hashCode());
		result = prime * result + ((universeTimestamp == null) ? 0 : universeTimestamp.hashCode());
		result = prime * result + ((updateTimestamp == null) ? 0 : updateTimestamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Timestamps other = (Timestamps) obj;
		if (alliancesTimestamp == null) {
			if (other.alliancesTimestamp != null)
				return false;
		} else if (!alliancesTimestamp.equals(other.alliancesTimestamp))
			return false;
		if (highscore10Timestamp == null) {
			if (other.highscore10Timestamp != null)
				return false;
		} else if (!highscore10Timestamp.equals(other.highscore10Timestamp))
			return false;
		if (highscore11Timestamp == null) {
			if (other.highscore11Timestamp != null)
				return false;
		} else if (!highscore11Timestamp.equals(other.highscore11Timestamp))
			return false;
		if (highscore12Timestamp == null) {
			if (other.highscore12Timestamp != null)
				return false;
		} else if (!highscore12Timestamp.equals(other.highscore12Timestamp))
			return false;
		if (highscore13Timestamp == null) {
			if (other.highscore13Timestamp != null)
				return false;
		} else if (!highscore13Timestamp.equals(other.highscore13Timestamp))
			return false;
		if (highscore14Timestamp == null) {
			if (other.highscore14Timestamp != null)
				return false;
		} else if (!highscore14Timestamp.equals(other.highscore14Timestamp))
			return false;
		if (highscore15Timestamp == null) {
			if (other.highscore15Timestamp != null)
				return false;
		} else if (!highscore15Timestamp.equals(other.highscore15Timestamp))
			return false;
		if (highscore16Timestamp == null) {
			if (other.highscore16Timestamp != null)
				return false;
		} else if (!highscore16Timestamp.equals(other.highscore16Timestamp))
			return false;
		if (highscore17Timestamp == null) {
			if (other.highscore17Timestamp != null)
				return false;
		} else if (!highscore17Timestamp.equals(other.highscore17Timestamp))
			return false;
		if (highscore20Timestamp == null) {
			if (other.highscore20Timestamp != null)
				return false;
		} else if (!highscore20Timestamp.equals(other.highscore20Timestamp))
			return false;
		if (highscore21Timestamp == null) {
			if (other.highscore21Timestamp != null)
				return false;
		} else if (!highscore21Timestamp.equals(other.highscore21Timestamp))
			return false;
		if (highscore22Timestamp == null) {
			if (other.highscore22Timestamp != null)
				return false;
		} else if (!highscore22Timestamp.equals(other.highscore22Timestamp))
			return false;
		if (highscore23Timestamp == null) {
			if (other.highscore23Timestamp != null)
				return false;
		} else if (!highscore23Timestamp.equals(other.highscore23Timestamp))
			return false;
		if (highscore24Timestamp == null) {
			if (other.highscore24Timestamp != null)
				return false;
		} else if (!highscore24Timestamp.equals(other.highscore24Timestamp))
			return false;
		if (highscore25Timestamp == null) {
			if (other.highscore25Timestamp != null)
				return false;
		} else if (!highscore25Timestamp.equals(other.highscore25Timestamp))
			return false;
		if (highscore26Timestamp == null) {
			if (other.highscore26Timestamp != null)
				return false;
		} else if (!highscore26Timestamp.equals(other.highscore26Timestamp))
			return false;
		if (highscore27Timestamp == null) {
			if (other.highscore27Timestamp != null)
				return false;
		} else if (!highscore27Timestamp.equals(other.highscore27Timestamp))
			return false;
		if (playersTimestamp == null) {
			if (other.playersTimestamp != null)
				return false;
		} else if (!playersTimestamp.equals(other.playersTimestamp))
			return false;
		if (serverDataTimestamp == null) {
			if (other.serverDataTimestamp != null)
				return false;
		} else if (!serverDataTimestamp.equals(other.serverDataTimestamp))
			return false;
		if (universeTimestamp == null) {
			if (other.universeTimestamp != null)
				return false;
		} else if (!universeTimestamp.equals(other.universeTimestamp))
			return false;
		if (updateTimestamp == null) {
			if (other.updateTimestamp != null)
				return false;
		} else if (!updateTimestamp.equals(other.updateTimestamp))
			return false;
		return true;
	}

	
}
