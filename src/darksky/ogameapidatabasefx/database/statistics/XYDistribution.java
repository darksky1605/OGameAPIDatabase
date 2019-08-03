package darksky.ogameapidatabasefx.database.statistics;
//package darksky.ogameapidatabase.database.statistics;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a two-dimensional distribution
 * 
 * @author Dark Sky
 *
 */
public class XYDistribution {

	private LocalDate m_date = null;
	private int[][] m_distribution = null;
	private int m_galaxies;
	private int m_systems;

	public XYDistribution(LocalDate date, int galaxies, int systems) {
		m_date = date;
		m_galaxies = galaxies;
		m_systems = systems;
		m_distribution = new int[galaxies][systems];
	}

	//
	/**
	 * Set the distribution from a string. Missing cells will be set to zero.
	 * 
	 * @param input
	 *            expected form:
	 *            "x-value y-value cell-value, x-value y-value cell-value, ..."
	 */
	public void setFromString(String input) {
		Objects.requireNonNull(input);
		//System.out.println(input);
		String tmp1[] = input.split(",");
		for (String entry : tmp1) {
			String tmp2[] = entry.split(" ");
			assert tmp2.length == 3;
			int g = Integer.parseInt(tmp2[0]);
			int s = Integer.parseInt(tmp2[1]);
			int c = Integer.parseInt(tmp2[2]);
			m_distribution[g - 1][s - 1] = c;
		}
	}

	public LocalDate getDate() {
		return m_date;
	}

	public int getGalaxies() {
		return m_galaxies;
	}

	public int getSystems() {
		return m_systems;
	}

	public int[][] getDistribution() {
		return m_distribution;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_date == null) ? 0 : m_date.hashCode());
		result = prime * result + Arrays.hashCode(m_distribution);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof XYDistribution))
			return false;
		XYDistribution other = (XYDistribution) obj;
		if (m_date == null) {
			if (other.m_date != null)
				return false;
		} else if (!m_date.equals(other.m_date))
			return false;
		if (!Arrays.deepEquals(m_distribution, other.m_distribution))
			return false;
		return true;
	}

}
