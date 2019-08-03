package darksky.ogameapidatabasefx.database.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author darksky
 *
 */
public class AllianceData implements IHighscoreEntity{

	private Alliance alliance = null;
	private List<Highscore> highscores = Collections.emptyList();	
	private Collection<Player> members = Collections.emptySet();
	
	public AllianceData(Alliance alliance){
		this.alliance = alliance;
	}
	
	public AllianceData(Alliance alliance, List<Highscore> highscores, Collection<Player> members) {
		super();
		this.alliance = alliance;
		this.highscores = highscores;
		this.members = members;
	}
	
	public Entity getEntity(){
		return getAlliance();
	}
	
	public Alliance getAlliance() {
		return alliance;
	}
	public void setAlliance(Alliance alliance) {
		this.alliance = alliance;
	}
	public List<Highscore> getHighscores() {
		return highscores;
	}
	public void setHighscores(List<Highscore> highscores) {
		this.highscores = highscores;
	}
	public Collection<Player> getMembers() {
		return members;
	}
	public void setMembers(Collection<Player> members) {
		this.members = members;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alliance == null) ? 0 : alliance.hashCode());
		result = prime * result + ((highscores == null) ? 0 : highscores.hashCode());
		result = prime * result + ((members == null) ? 0 : members.hashCode());
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
		AllianceData other = (AllianceData) obj;
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
		if (members == null) {
			if (other.members != null)
				return false;
		} else if (!members.equals(other.members))
			return false;
		return true;
	}
	
	

}
