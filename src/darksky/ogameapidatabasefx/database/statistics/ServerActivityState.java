package darksky.ogameapidatabasefx.database.statistics;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

public class ServerActivityState {

	private String m_serverPrefix;
	private LocalDateTime m_dateTime;

	private int m_numberOfNotDeletablePlayers;
	private HashMap<String, Integer> m_playerActivityMap;

	ServerActivityState(String serverPrefix) {
		Objects.requireNonNull(serverPrefix);
		m_serverPrefix = serverPrefix;
		m_dateTime = LocalDateTime.now();
		m_playerActivityMap = new HashMap<String, Integer>();
	}

	void setDateTime(LocalDateTime date) {
		Objects.requireNonNull(date);
		m_dateTime = date;
	}

	void setNumberOfNotDeletablePlayers(int numberOfNotDeletablePlayers) {
		if (numberOfNotDeletablePlayers < 0) {
			throw new IllegalArgumentException(
					"numberOfNotDeletablePlayers must not be less than zero");
		}
		m_numberOfNotDeletablePlayers = numberOfNotDeletablePlayers;
	}

	/**
	 * 
	 * @param input
	 *            Expected form "key1=value1, key2=value2,....".
	 */
	void setPlayerActivityMapFromString(String input) {
		Objects.requireNonNull(input);

		m_playerActivityMap = new HashMap<String, Integer>();
		String[] pairs = input.split(",");
		String[] pair;
		for (String s : pairs) {
			pair = s.split("=");
			m_playerActivityMap.put(pair[0], Integer.parseInt(pair[1]));
		}
	}

	public String getServerPrefix() {
		return m_serverPrefix;
	}

	public LocalDateTime getDateTime() {
		return m_dateTime;
	}

	public int getNumberOfPlayers() {
		return m_playerActivityMap.values().stream()
				.mapToInt(Integer::intValue).sum();
	}

	public int getNumberOfActivePlayers() {
		int activePlayers = m_playerActivityMap.getOrDefault("active", 0);
		return activePlayers;
	}

	public int getNumberOfLongtimeInactivePlayers() {
		return m_playerActivityMap.entrySet().stream()
				.filter(e -> e.getKey().indexOf('I') > -1)
				.mapToInt(e -> e.getValue()).sum();
	}

	public int getNumberOfInactivePlayers() {
		return m_playerActivityMap.entrySet().stream()
				.filter(e -> !e.getKey().equals("active"))
				.filter(e -> e.getKey().toLowerCase().indexOf('i') > -1)
				.mapToInt(e -> e.getValue()).sum();
	}

	public int getNumberOfNotDeletablePlayers() {
		return m_numberOfNotDeletablePlayers;
	}

	public int getNumberOfVModePlayers() {
		return m_playerActivityMap.entrySet().stream()
				.filter(e -> !e.getKey().equals("active"))
				.filter(e -> e.getKey().indexOf('v') > -1)
				.mapToInt(e -> e.getValue()).sum();
	}

	public HashMap<String, Integer> getPlayerActivityMap() {
		return m_playerActivityMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_dateTime == null) ? 0 : m_dateTime.hashCode());
		result = prime * result + m_numberOfNotDeletablePlayers;
		result = prime
				* result
				+ ((m_playerActivityMap == null) ? 0 : m_playerActivityMap
						.hashCode());
		result = prime * result
				+ ((m_serverPrefix == null) ? 0 : m_serverPrefix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ServerActivityState))
			return false;
		ServerActivityState other = (ServerActivityState) obj;
		if (m_dateTime == null) {
			if (other.m_dateTime != null)
				return false;
		} else if (!m_dateTime.equals(other.m_dateTime))
			return false;
		if (m_numberOfNotDeletablePlayers != other.m_numberOfNotDeletablePlayers)
			return false;
		if (m_playerActivityMap == null) {
			if (other.m_playerActivityMap != null)
				return false;
		} else if (!m_playerActivityMap.equals(other.m_playerActivityMap))
			return false;
		if (m_serverPrefix == null) {
			if (other.m_serverPrefix != null)
				return false;
		} else if (!m_serverPrefix.equals(other.m_serverPrefix))
			return false;
		return true;
	}
}
