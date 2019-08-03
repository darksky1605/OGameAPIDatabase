package darksky.ogameapidatabasefx.database.statistics;

public class ServerHistory {

	private ServerActivityState m_oldState;
	private ServerActivityState m_newState;

	private int m_numberOfNewPlayers;
	private int m_numberOfDeletedPlayers;
	private int m_numberOfNewAlliances;
	private int m_numberOfDeletedAlliances;
	private int m_numberOfNewPlanets;
	private int m_numberOfDeletedPlanets;
	private int m_numberOfNewMoons;
	private int m_numberOfDeletedMoons;

	private int m_numberOfPlayerNameChanges;
	private int m_numberOfPlayerStatusChanges;
	private int m_numberOfAllianceNameChanges;
	private int m_numberOfAllianceTagChanges;
	private int m_numberOfAllianceHomepageChanges;
	private int m_numberOfAllianceLogoChanges;
	private int m_numberOfAllianceOpenChanges;
	private int m_numberOfAllianceMemberChanges;
	private int m_numberOfPlanetNameChanges;
	private int m_numberOfMoonNameChanges;
	private int m_numberOfRelocations;

	private int m_numberOfAutoDeletedPlayers;
	private int m_numberOfRelocatedPlayers;

	public ServerHistory(ServerActivityState oldState,
			ServerActivityState newState) {
		m_oldState = oldState;
		m_newState = newState;
	}

	void setNumberOfNewPlayers(int numberOfNewPlayers) {
		m_numberOfNewPlayers = numberOfNewPlayers;
	}

	void setNumberOfDeletedPlayers(int numberOfDeletedPlayers) {
		m_numberOfDeletedPlayers = numberOfDeletedPlayers;
	}

	void setNumberOfNewAlliances(int numberOfNewAlliances) {
		m_numberOfNewAlliances = numberOfNewAlliances;
	}

	void setNumberOfDeletedAlliances(int numberOfDeletedAlliances) {
		m_numberOfDeletedAlliances = numberOfDeletedAlliances;
	}

	void setNumberOfNewPlanets(int numberOfNewPlanets) {
		m_numberOfNewPlanets = numberOfNewPlanets;
	}

	void setNumberOfDeletedPlanets(int numberOfDeletedPlanets) {
		m_numberOfDeletedPlanets = numberOfDeletedPlanets;
	}

	void setNumberOfNewMoons(int numberOfNewMoons) {
		m_numberOfNewMoons = numberOfNewMoons;
	}

	void setNumberOfDeletedMoons(int numberOfDeletedMoons) {
		m_numberOfDeletedMoons = numberOfDeletedMoons;
	}

	void setNumberOfPlayerNameChanges(int numberOfPlayerNameChanges) {
		m_numberOfPlayerNameChanges = numberOfPlayerNameChanges;
	}

	void setNumberOfPlayerStatusChanges(int numberOfPlayerStatusChanges) {
		m_numberOfPlayerStatusChanges = numberOfPlayerStatusChanges;
	}

	void setNumberOfAllianceNameChanges(int numberOfAllianceNameChanges) {
		m_numberOfAllianceNameChanges = numberOfAllianceNameChanges;
	}

	void setNumberOfAllianceTagChanges(int numberOfAllianceTagChanges) {
		m_numberOfAllianceTagChanges = numberOfAllianceTagChanges;
	}

	void setNumberOfAllianceHomepageChanges(int numberOfAllianceHomepageChanges) {
		m_numberOfAllianceHomepageChanges = numberOfAllianceHomepageChanges;
	}

	void setNumberOfAllianceLogoChanges(int numberOfAllianceLogoChanges) {
		m_numberOfAllianceLogoChanges = numberOfAllianceLogoChanges;
	}

	void setNumberOfAllianceOpenChanges(int numberOfAllianceOpenChanges) {
		m_numberOfAllianceOpenChanges = numberOfAllianceOpenChanges;
	}

	void setNumberOfAllianceMemberChanges(int numberOfAllianceMemberChanges) {
		m_numberOfAllianceMemberChanges = numberOfAllianceMemberChanges;
	}

	void setNumberOfPlanetNameChanges(int numberOfPlanetNameChanges) {
		m_numberOfPlanetNameChanges = numberOfPlanetNameChanges;
	}

	void setNumberOfMoonNameChanges(int numberOfMoonNameChanges) {
		m_numberOfMoonNameChanges = numberOfMoonNameChanges;
	}

	void setNumberOfRelocations(int numberOfRelocations) {
		m_numberOfRelocations = numberOfRelocations;
	}

	void setNumberOfAutoDeletedPlayers(int numberOfAutoDeletedPlayers) {
		m_numberOfAutoDeletedPlayers = numberOfAutoDeletedPlayers;
	}

	void setNumberOfRelocatedPlayers(int numberOfRelocatedPlayers) {
		m_numberOfRelocatedPlayers = numberOfRelocatedPlayers;
	}

	public ServerActivityState getOldState() {
		return m_oldState;
	}

	public ServerActivityState getNewState() {
		return m_newState;
	}

	public int getNumberOfNewPlayers() {
		return m_numberOfNewPlayers;
	}

	public int getNumberOfDeletedPlayers() {
		return m_numberOfDeletedPlayers;
	}

	public int getNumberOfNewAlliances() {
		return m_numberOfNewAlliances;
	}

	public int getNumberOfDeletedAlliances() {
		return m_numberOfDeletedAlliances;
	}

	public int getNumberOfNewPlanets() {
		return m_numberOfNewPlanets;
	}

	public int getNumberOfDeletedPlanets() {
		return m_numberOfDeletedPlanets;
	}

	public int getNumberOfNewMoons() {
		return m_numberOfNewMoons;
	}

	public int getNumberOfDeletedMoons() {
		return m_numberOfDeletedMoons;
	}

	public int getNumberOfPlayerNameChanges() {
		return m_numberOfPlayerNameChanges;
	}

	public int getNumberOfPlayerStatusChanges() {
		return m_numberOfPlayerStatusChanges;
	}

	public int getNumberOfAllianceNameChanges() {
		return m_numberOfAllianceNameChanges;
	}

	public int getNumberOfAllianceTagChanges() {
		return m_numberOfAllianceTagChanges;
	}

	public int getNumberOfAllianceHomepageChanges() {
		return m_numberOfAllianceHomepageChanges;
	}

	public int getNumberOfAllianceLogoChanges() {
		return m_numberOfAllianceLogoChanges;
	}

	public int getNumberOfAllianceOpenChanges() {
		return m_numberOfAllianceOpenChanges;
	}

	public int getNumberOfAllianceMemberChanges() {
		return m_numberOfAllianceMemberChanges;
	}

	public int getNumberOfPlanetNameChanges() {
		return m_numberOfPlanetNameChanges;
	}

	public int getNumberOfMoonNameChanges() {
		return m_numberOfMoonNameChanges;
	}

	public int getNumberOfRelocations() {
		return m_numberOfRelocations;
	}

	public int getNumberOfAutoDeletedPlayers() {
		return m_numberOfAutoDeletedPlayers;
	}

	public int getNumberOfRelocatedPlayers() {
		return m_numberOfRelocatedPlayers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_newState == null) ? 0 : m_newState.hashCode());
		result = prime * result + m_numberOfAllianceHomepageChanges;
		result = prime * result + m_numberOfAllianceLogoChanges;
		result = prime * result + m_numberOfAllianceMemberChanges;
		result = prime * result + m_numberOfAllianceNameChanges;
		result = prime * result + m_numberOfAllianceOpenChanges;
		result = prime * result + m_numberOfAllianceTagChanges;
		result = prime * result + m_numberOfAutoDeletedPlayers;
		result = prime * result + m_numberOfDeletedAlliances;
		result = prime * result + m_numberOfDeletedMoons;
		result = prime * result + m_numberOfDeletedPlanets;
		result = prime * result + m_numberOfDeletedPlayers;
		result = prime * result + m_numberOfMoonNameChanges;
		result = prime * result + m_numberOfNewAlliances;
		result = prime * result + m_numberOfNewMoons;
		result = prime * result + m_numberOfNewPlanets;
		result = prime * result + m_numberOfNewPlayers;
		result = prime * result + m_numberOfPlanetNameChanges;
		result = prime * result + m_numberOfPlayerNameChanges;
		result = prime * result + m_numberOfPlayerStatusChanges;
		result = prime * result + m_numberOfRelocatedPlayers;
		result = prime * result + m_numberOfRelocations;
		result = prime * result
				+ ((m_oldState == null) ? 0 : m_oldState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ServerHistory))
			return false;
		ServerHistory other = (ServerHistory) obj;
		if (m_newState == null) {
			if (other.m_newState != null)
				return false;
		} else if (!m_newState.equals(other.m_newState))
			return false;
		if (m_numberOfAllianceHomepageChanges != other.m_numberOfAllianceHomepageChanges)
			return false;
		if (m_numberOfAllianceLogoChanges != other.m_numberOfAllianceLogoChanges)
			return false;
		if (m_numberOfAllianceMemberChanges != other.m_numberOfAllianceMemberChanges)
			return false;
		if (m_numberOfAllianceNameChanges != other.m_numberOfAllianceNameChanges)
			return false;
		if (m_numberOfAllianceOpenChanges != other.m_numberOfAllianceOpenChanges)
			return false;
		if (m_numberOfAllianceTagChanges != other.m_numberOfAllianceTagChanges)
			return false;
		if (m_numberOfAutoDeletedPlayers != other.m_numberOfAutoDeletedPlayers)
			return false;
		if (m_numberOfDeletedAlliances != other.m_numberOfDeletedAlliances)
			return false;
		if (m_numberOfDeletedMoons != other.m_numberOfDeletedMoons)
			return false;
		if (m_numberOfDeletedPlanets != other.m_numberOfDeletedPlanets)
			return false;
		if (m_numberOfDeletedPlayers != other.m_numberOfDeletedPlayers)
			return false;
		if (m_numberOfMoonNameChanges != other.m_numberOfMoonNameChanges)
			return false;
		if (m_numberOfNewAlliances != other.m_numberOfNewAlliances)
			return false;
		if (m_numberOfNewMoons != other.m_numberOfNewMoons)
			return false;
		if (m_numberOfNewPlanets != other.m_numberOfNewPlanets)
			return false;
		if (m_numberOfNewPlayers != other.m_numberOfNewPlayers)
			return false;
		if (m_numberOfPlanetNameChanges != other.m_numberOfPlanetNameChanges)
			return false;
		if (m_numberOfPlayerNameChanges != other.m_numberOfPlayerNameChanges)
			return false;
		if (m_numberOfPlayerStatusChanges != other.m_numberOfPlayerStatusChanges)
			return false;
		if (m_numberOfRelocatedPlayers != other.m_numberOfRelocatedPlayers)
			return false;
		if (m_numberOfRelocations != other.m_numberOfRelocations)
			return false;
		if (m_oldState == null) {
			if (other.m_oldState != null)
				return false;
		} else if (!m_oldState.equals(other.m_oldState))
			return false;
		return true;
	}
}
