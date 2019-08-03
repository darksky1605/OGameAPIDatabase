package darksky.ogameapidatabasefx.database.entities;

import java.time.Instant;

/**
 * A superclass for entities which are stored in the database. Planet, Moon,
 * HighscoreEntity. HighscoreEntity itself is a superclass of Player and
 * Alliance
 * 
 * @author Dark Sky
 *
 */
public class Entity {

	private int id;
	private String name;
	private Instant insertedOn;
	private Instant lastUpdate;
	private boolean deleted;
	
	public Entity(){}

	public Entity(int id, String name, Instant insertedOn, Instant lastUpdate, boolean deleted) {
		super();
		this.id = id;
		this.name = name;
		this.insertedOn = insertedOn;
		this.lastUpdate = lastUpdate;
		this.deleted = deleted;
	}

	public int getId() {
		return id;
	}

	public Entity setId(int id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Entity setName(String name) {
		this.name = name;
		return this;
	}

	public Instant getInsertedOn() {
		return insertedOn;
	}

	public Entity setInsertedOn(Instant insertedOn) {
		this.insertedOn = insertedOn;
		return this;
	}

	public Instant getLastUpdate() {
		return lastUpdate;
	}

	public Entity setLastUpdate(Instant lastUpdate) {
		this.lastUpdate = lastUpdate;
		return this;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public Entity setDeleted(boolean deleted) {
		this.deleted = deleted;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (deleted ? 1231 : 1237);
		result = prime * result + id;
		result = prime * result + ((insertedOn == null) ? 0 : insertedOn.hashCode());
		result = prime * result + ((lastUpdate == null) ? 0 : lastUpdate.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Entity))
			return false;
		Entity other = (Entity) obj;
		if (deleted != other.deleted)
			return false;
		if (id != other.id)
			return false;
		if (insertedOn == null) {
			if (other.insertedOn != null)
				return false;
		} else if (!insertedOn.equals(other.insertedOn))
			return false;
		if (lastUpdate == null) {
			if (other.lastUpdate != null)
				return false;
		} else if (!lastUpdate.equals(other.lastUpdate))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
