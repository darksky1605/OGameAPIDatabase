package darksky.ogameapidatabasefx.database.logs;

import java.time.Instant;

import darksky.ogameapidatabasefx.database.entities.Entity;

public class ChangeLogEntry<O extends Entity , V> {

	private final O owner;

	private final V oldValue;
	
	private final V newValue;
	
	private final Instant instant;

	ChangeLogEntry(O owner, Instant instant, V oldValue,
			V newValue) {
		super();
		this.owner = owner;
		this.instant = instant;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public O getOwner() {
		return owner;
	}

	public Instant getInstant() {
		return instant;
	}

	public V getOldValue() {
		return oldValue;
	}

	public V getNewValue() {
		return newValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instant == null) ? 0 : instant.hashCode());
		result = prime * result
				+ ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result
				+ ((oldValue == null) ? 0 : oldValue.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ChangeLogEntry))
			return false;
		ChangeLogEntry<?, ?> other = (ChangeLogEntry<?, ?>) obj;
		if (instant == null) {
			if (other.instant != null)
				return false;
		} else if (!instant.equals(other.instant))
			return false;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		} else if (!oldValue.equals(other.oldValue))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
}
