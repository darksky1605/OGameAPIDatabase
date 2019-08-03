package darksky.ogameapidatabasefx.database.entities;

import java.time.Instant;

/**
 * 
 * @author darksky
 *
 */
public class Alliance extends Entity {

//	public static final int DEFAULT_ALLIANCE_ID = 999999;
//	public static final Alliance DEFAULT_ALLIANCE;
//
//	static {
//		Highscore h = new Highscore(Instant.ofEpochSecond(0));
//		DEFAULT_ALLIANCE = new Alliance(DEFAULT_ALLIANCE_ID, "", Instant.ofEpochSecond(0), Instant.ofEpochSecond(0),
//				false, h, "", "", "", true);
//	}

	private String allianceTag;
	private String homepage;
	private String logo;
	private boolean open;

	public Alliance(int id, String name, Instant insertedOn, Instant lastUpdate, boolean deleted, String allianceTag, String homepage, String logo, boolean open) {
		super(id, name, insertedOn, lastUpdate, deleted);
		this.allianceTag = allianceTag;
		this.homepage = homepage;
		this.logo = logo;
		this.open = open;
	}

	public String getAllianceTag() {
		return allianceTag;
	}

	public Alliance setAllianceTag(String allianceTag) {
		this.allianceTag = allianceTag;
		return this;
	}

	public String getHomepage() {
		return homepage;
	}

	public Alliance setHomepage(String homepage) {
		this.homepage = homepage;
		return this;
	}

	public String getLogo() {
		return logo;
	}

	public Alliance setLogo(String logo) {
		this.logo = logo;
		return this;
	}

	public boolean isOpen() {
		return open;
	}

	public Alliance setOpen(boolean open) {
		this.open = open;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((allianceTag == null) ? 0 : allianceTag.hashCode());
		result = prime * result + ((homepage == null) ? 0 : homepage.hashCode());
		result = prime * result + ((logo == null) ? 0 : logo.hashCode());
		result = prime * result + (open ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Alliance))
			return false;
		Alliance other = (Alliance) obj;
		if (allianceTag == null) {
			if (other.allianceTag != null)
				return false;
		} else if (!allianceTag.equals(other.allianceTag))
			return false;
		if (homepage == null) {
			if (other.homepage != null)
				return false;
		} else if (!homepage.equals(other.homepage))
			return false;
		if (logo == null) {
			if (other.logo != null)
				return false;
		} else if (!logo.equals(other.logo))
			return false;
		if (open != other.open)
			return false;
		return true;
	}

}
