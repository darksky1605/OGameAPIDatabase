package darksky.ogameapidatabasefx.database.entities;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 
 * @author darksky
 *
 */
public class AccountData implements IHighscoreEntity{
	
	private Player player;
	private Optional<Alliance> alliance = Optional.ofNullable(null);
	private List<Highscore> highscores = Collections.emptyList();	
	private Map<Planet, List<Moon>> planetMap = Collections.emptyMap(); // the list contains deleted planets and deleted moons, too

	public AccountData(Player player){
		this.player = player;
	}

	public AccountData(Player player, Optional<Alliance> alliance, List<Highscore> highscores,
			Map<Planet, List<Moon>> planetMap) {
		super();
		this.player = player;
		this.alliance = alliance;
		this.highscores = highscores;
		this.planetMap = planetMap;
	}
	
	public Entity getEntity(){
		return getPlayer();
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Optional<Alliance> getAlliance() {
		return alliance;
	}

	public void setAlliance(Optional<Alliance> alliance) {
		this.alliance = alliance;
	}

	public List<Highscore> getHighscores() {
		return highscores;
	}

	public void setHighscores(List<Highscore> highscores) {
		this.highscores = highscores;
	}

	public Map<Planet, List<Moon>> getPlanetMap() {
		return planetMap;
	}

	public void setPlanetMap(Map<Planet, List<Moon>> planetMap) {
		this.planetMap = planetMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alliance == null) ? 0 : alliance.hashCode());
		result = prime * result + ((highscores == null) ? 0 : highscores.hashCode());
		result = prime * result + ((planetMap == null) ? 0 : planetMap.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
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
		AccountData other = (AccountData) obj;
		if (alliance == null) {
			if (other.alliance != null)
				return false;
		} else if (!alliance.equals(other.alliance))
			return false;
		if (highscores == null) {
			if (other.highscores != null)
				return false;
		} else if (!highscores.equals(other.highscores))
			return false;
		if (planetMap == null) {
			if (other.planetMap != null)
				return false;
		} else if (!planetMap.equals(other.planetMap))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}



}
