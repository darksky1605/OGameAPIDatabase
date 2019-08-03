package darksky.ogameapidatabasefx.database.entities;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author darksky
 *
 */
public class SolarSystem {
	private final int galaxy;
	private final int system;
	private final ArrayList<SystemPosition> positions;

	public SolarSystem(int galaxy, int system) {
		this.galaxy = galaxy;
		this.system = system;
		this.positions = new ArrayList<SystemPosition>(15);
	}

	public SolarSystem addSystemPosition(SystemPosition systemPosition) {
		Objects.requireNonNull(systemPosition);
		this.positions.add(systemPosition);
		return this;
	}

	public int getGalaxy() {
		return galaxy;
	}

	public int getSystem() {
		return system;
	}

	public ArrayList<SystemPosition> getPositions() {
		return positions;
	}
}
