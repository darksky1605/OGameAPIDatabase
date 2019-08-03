package darksky.ogameapidatabasefx.database.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 
 * @author darksky
 *
 */
public class ServerData {

	private Map<String, String> dataMap;

	ServerData(String domain, String name, int number, String language,
			String timezone, String timezoneOffset, String version, int speed, int speedFleet,
			int galaxies, int systems, boolean acs, boolean rapidFire,
			boolean defToTF, float debrisFactor, float repairFactor,
			int newbieProtectionLimit, int newbieProtectionHigh, int topScore,
			int bonusFields, boolean donutGalaxy, boolean donutSystem, float debrisFactorDef, 
			boolean wfEnabled, int wfMinimumRessLost, int wfMinimumLossPercentage, int wfBasicPercentageRepairable,
			float globalDeuteriumSaveFactor,
			int bashlimit, int probeCargo, int researchDurationDivisor, 
			int darkMatterNewAcount, int cargoHyperspaceTechMultiplier) {

		super();

		Objects.requireNonNull(domain);
		Objects.requireNonNull(name);
		Objects.requireNonNull(language);
		Objects.requireNonNull(timezone);
		Objects.requireNonNull(timezoneOffset);
		Objects.requireNonNull(version);

		dataMap = new HashMap<String, String>();

		dataMap.put("domain", domain);
		dataMap.put("name", name);
		dataMap.put("number", "" + number);
		dataMap.put("language", language);
		dataMap.put("timezone", timezone);
		dataMap.put("timezoneOffset", timezoneOffset);
		dataMap.put("version", version);
		dataMap.put("speed", "" + speed);
		dataMap.put("speedFleet", "" + speedFleet);
		dataMap.put("galaxies", "" + galaxies);
		dataMap.put("systems", "" + systems);
		dataMap.put("acs", "" + acs);
		dataMap.put("rapidFire", "" + rapidFire);
		dataMap.put("defToTF", "" + defToTF);
		dataMap.put("debrisFactor", "" + debrisFactor);
		dataMap.put("repairFactor", "" + repairFactor);
		dataMap.put("newbieProtectionLimit", "" + newbieProtectionLimit);
		dataMap.put("newbieProtectionHigh", "" + newbieProtectionHigh);
		dataMap.put("topScore", "" + topScore);
		dataMap.put("bonusFields", "" + bonusFields);
		dataMap.put("donutGalaxy", "" + donutGalaxy);
		dataMap.put("donutSystem", "" + donutSystem);
		dataMap.put("debrisFactorDef", "" + debrisFactorDef);
		dataMap.put("wfEnabled", "" + wfEnabled);
		dataMap.put("wfMinimumRessLost", "" + wfMinimumRessLost);
		dataMap.put("wfMinimumLossPercentage", "" + wfMinimumLossPercentage);
		dataMap.put("wfBasicPercentageRepairable", "" + wfBasicPercentageRepairable); 
		dataMap.put("globalDeuteriumSaveFactor", "" + globalDeuteriumSaveFactor);
		dataMap.put("bashlimit", "" + bashlimit);
		dataMap.put("probeCargo", "" + probeCargo);
		dataMap.put("researchDurationDivisor", "" + researchDurationDivisor);
		dataMap.put("darkMatterNewAcount", "" + darkMatterNewAcount);
		dataMap.put("cargoHyperspaceTechMultiplier", "" + cargoHyperspaceTechMultiplier);
	}

	public String getDomain() {
		return dataMap.get("domain");
	}

	public String getName() {
		return dataMap.get("name");
	}

	public int getNumber() {
		return Integer.parseInt(dataMap.get("number"));
	}

	public String getLanguage() {
		return dataMap.get("language");
	}

	public String getTimezone() {
		return dataMap.get("timezone");
	}

	public String getVersion() {
		return dataMap.get("version");
	}

	public int getSpeed() {
		return Integer.parseInt(dataMap.get("speed"));
	}

	public int getSpeedFleet() {
		return Integer.parseInt(dataMap.get("speedFleet"));
	}

	public int getGalaxies() {
		return Integer.parseInt(dataMap.get("galaxies"));
	}

	public int getSystems() {
		return Integer.parseInt(dataMap.get("systems"));
	}

	public boolean isAcs() {
		return Boolean.parseBoolean(dataMap.get("acs"));
	}

	public boolean isRapidFire() {
		return Boolean.parseBoolean(dataMap.get("rapidFire"));
	}

	public boolean isDefToTF() {
		return Boolean.parseBoolean(dataMap.get("defToTF"));
	}

	public float getDebrisFactor() {
		return Float.parseFloat(dataMap.get("debrisFactor"));
	}

	public float getRepairFactor() {
		return Float.parseFloat(dataMap.get("repairFactor"));
	}

	public int getNewbieProtectionLimit() {
		return Integer.parseInt(dataMap.get("newbieProtectionLimit"));
	}

	public int getNewbieProtectionHigh() {
		return Integer.parseInt(dataMap.get("newbieProtectionHigh"));
	}

	public int getTopScore() {
		return Integer.parseInt(dataMap.get("topScore"));
	}

	public int getBonusFields() {
		return Integer.parseInt(dataMap.get("bonusFields"));
	}

	public boolean isDonutGalaxy() {
		return Boolean.parseBoolean(dataMap.get("donutGalaxy"));
	}

	public boolean isDonutSystem() {
		return Boolean.parseBoolean(dataMap.get("donutSystem"));
	}
	
	public float getDebrisFactorDef() {
		return Float.parseFloat(dataMap.get("debrisFactorDef"));
	}
	
	public boolean isWfEnabled() {
		return Boolean.parseBoolean(dataMap.get("wfEnabled"));
	}
	
	public int getWfMinimumRessLost() {
		return Integer.parseInt(dataMap.get("wfMinimumRessLost"));
	}
	
	public int getWfMinimumLossPercentage() {
		return Integer.parseInt(dataMap.get("wfMinimumLossPercentage"));
	}
	
	public int getWfBasicPercentageRepairable() {
		return Integer.parseInt(dataMap.get("wfBasicPercentageRepairable"));
	}
	
	public float getGlobalDeuteriumSaveFactor(){
        return Float.parseFloat(dataMap.get("globalDeuteriumSaveFactor"));
	}
	
	public int getBashlimit() {
		return Integer.parseInt(dataMap.get("bashlimit"));
	}
	
	public int getProbeCargo() {
		return Integer.parseInt(dataMap.get("probeCargo"));
	}
	
	public int getResearchDurationDivisor() {
		return Integer.parseInt(dataMap.get("researchDurationDivisor"));
	}
	
	public int getDarkMatterNewAcount() {
		return Integer.parseInt(dataMap.get("darkMatterNewAcount"));
	}
	
	public int getCargoHyperspaceTechMultiplier() {
		return Integer.parseInt(dataMap.get("cargoHyperspaceTechMultiplier"));
	}

	public Map<String, String> getDataMap() {
		return dataMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataMap == null) ? 0 : dataMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ServerData))
			return false;
		ServerData other = (ServerData) obj;
		if (dataMap == null) {
			if (other.dataMap != null)
				return false;
		} else if (!dataMap.equals(other.dataMap))
			return false;
		return true;
	}
}
