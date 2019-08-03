package darksky.ogameapidatabasefx.database.entities;

import java.time.Instant;

/**
 * @author darksky
 *
 */
public class Player extends Entity{

	private String playerStatus;
	private int allianceId = 999999;		

	public Player(int id, String name, Instant insertedOn, Instant lastUpdate, boolean deleted,
			String playerStatus, int allianceId) {
		super(id, name, insertedOn, lastUpdate, deleted);
		this.playerStatus = playerStatus;
		this.allianceId = allianceId;
	}

	public String getPlayerStatus() {
		return playerStatus;
	}

	public Player setPlayerStatus(String playerStatus) {
		this.playerStatus = playerStatus;
		return this;
	}
	
	public int getAllianceId(){
		return allianceId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((playerStatus == null) ? 0 : playerStatus.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (playerStatus == null) {
			if (other.playerStatus != null)
				return false;
		} else if (!playerStatus.equals(other.playerStatus))
			return false;
		return true;
	}


}
