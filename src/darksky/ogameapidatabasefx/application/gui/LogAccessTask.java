package darksky.ogameapidatabasefx.application.gui;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.util.Pair;
import darksky.ogameapidatabasefx.database.entities.Entity;
import darksky.ogameapidatabasefx.database.entities.Planet;
import darksky.ogameapidatabasefx.database.entities.Player;
import darksky.ogameapidatabasefx.database.logs.LogReader;

public class LogAccessTask extends
		Task<ObservableList<Pair<Entity, String>>> {

	final LogReader m_logReader;
	final int m_logType;
	final LocalDate m_from;
	final LocalDate m_to;
	final ResourceBundle m_resources;

	public LogAccessTask(LogReader logReader, int logType, LocalDate from,
			LocalDate to, ResourceBundle resources) {
		super();
		m_logReader = logReader;
		m_logType = logType;
		m_from = from;
		m_to = to;
		m_resources = resources;
	}

	@Override
	protected ObservableList<Pair<Entity, String>> call() throws Exception {
		ObservableList<Pair<Entity, String>> logList = FXCollections
				.observableArrayList();
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);

		switch (m_logType) {
		case 0:
			m_logReader
					.getPlayerStatusChanges(m_from, m_to, null)
					.stream()
					.filter(entry -> entry.getNewValue().getPlayerStatus().equals("i"))
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format(
								"%s : %-20s",
								LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(),
								actor.getName());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 1:
			m_logReader
					.getPlayerStatusChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format("%s : %-20s %6s -> %6s",
								LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(),
								entry.getOldValue().getPlayerStatus(), entry.getNewValue().getPlayerStatus());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 2:
			m_logReader
					.getAllianceMemberChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						boolean oldisdummy = !entry.getOldValue().isPresent();
						boolean newisdummy = !entry.getNewValue().isPresent();
						assert (oldisdummy && newisdummy) == false;
						String changeText;
						if (oldisdummy)
							changeText = String.format("%s : %20s",
									m_resources.getString("enteredalliance"),
									entry.getNewValue().get().getName());
						else if (newisdummy)
							changeText = String.format("%s : %20s",
									m_resources.getString("leftalliance"),
									entry.getOldValue().get().getName());
						else
							changeText = String.format("%s : %20s -> %20s",
									m_resources.getString("changedalliance"),
									entry.getOldValue().get().getName(), entry
											.getNewValue().get().getName());

						String logText = String.format("%s : %-20s - %s", LocalDateTime.ofInstant(entry
								.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(),
								changeText);

						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 3:
			m_logReader
					.getRelocations(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						Planet op = entry.getOldValue();
						Planet np = entry.getNewValue();
						String logText = String.format(
								"%s : %-20s [%d:%3d:%2d] -> [%d:%3d:%2d]",
								LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(),
								op.getGalaxy(), op.getSystem(),
								op.getPosition(), np.getGalaxy(),
								np.getSystem(), np.getPosition());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 4:
			m_logReader
					.getPlayerNameChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format("%s : %-20s -> %20s",
								LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(),
								entry.getOldValue().getName(), entry.getNewValue().getName());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 5:
			m_logReader
					.getAllianceNameChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format("%s : %-20s -> %20s",
								LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(),
								entry.getOldValue().getName(), entry.getNewValue().getName());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 6:
			m_logReader
					.getPlanetNameChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						Planet op = entry.getOldValue();
						Planet np = entry.getNewValue();
						String logText = String.format(
								"%s : %-20s [%d:%3d:%2d] %-20s -> %20s", LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor
										.getName(), np.getGalaxy(), np
										.getSystem(), np.getPosition(), op.getName(), np.getName());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 7:
			m_logReader
					.getMoonNameChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format(
								"%s : %-20s %-20s -> %20s", LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(), entry
										.getOldValue().getMoon().get().getName(), entry.getNewValue().getMoon().get().getName());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 8:
			m_logReader
					.getNewPlayers(m_from, m_to)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format("%s : %-20s", LocalDateTime.ofInstant(entry
								.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 9:
			m_logReader
					.getNewAlliances(m_from, m_to)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format("%s : %-20s", LocalDateTime.ofInstant(entry
								.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 10:
			m_logReader
					.getNewPlanets(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						Planet p = entry.getNewValue();
						String logText = String.format(
								"%s : %-20s [%d:%3d:%2d]", LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(), p
										.getGalaxy(), p.getSystem(), p
										.getPosition());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 11:
			m_logReader
					.getNewMoons(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						Planet p = entry.getNewValue();
						String logText = String.format(
								"%s : %-20s [%d:%3d:%2d]", LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(), p
										.getGalaxy(), p.getSystem(), p
										.getPosition());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 12:
			m_logReader
					.getDeletedPlayers(m_from, m_to)
					.stream()
					.map(entry -> {
						Player actor = entry.getOwner();
						String logText = String.format(
								"%s : %-20s [%6s]", LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(), actor
										.getPlayerStatus());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 13:
			m_logReader
					.getDeletedAlliances(m_from, m_to)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format("%s : %-20s", LocalDateTime.ofInstant(entry
								.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 14:
			m_logReader
					.getDeletedPlanets(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						Planet p = entry.getOldValue();
						String logText = String.format(
								"%s : %-20s [%d:%3d:%2d]", LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(), p
										.getGalaxy(), p.getSystem(), p
										.getPosition());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 15:
			m_logReader
					.getDeletedMoons(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						Planet p = entry.getNewValue();
						String logText = String.format(
								"%s : %-20s [%d:%3d:%2d]", LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(), p
										.getGalaxy(), p.getSystem(), p
										.getPosition());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 16:
			m_logReader
					.getAllianceTagChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format(
								"%s : %-20s %10s -> %10s", LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(), entry
										.getOldValue().getAllianceTag(), entry.getNewValue().getAllianceTag());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 17:
			m_logReader
					.getAllianceHomepageChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format("%s : %-20s %s -> %s",
								LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(),
								entry.getOldValue().getHomepage(), entry.getNewValue().getHomepage());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 18:
			m_logReader
					.getAllianceLogoChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						String logText = String.format("%s : %-20s %s -> %s",
								LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(),
								entry.getOldValue().getLogo(), entry.getNewValue().getLogo());
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 19:
			m_logReader
					.getAllianceOpenChanges(m_from, m_to, null)
					.stream()
					.map(entry -> {
						Entity actor = entry.getOwner();
						final String[] openstatus = {
								m_resources
										.getString("allianceapplicationpossibleno"),
								m_resources
										.getString("allianceapplicationpossibleyes") };
						String logText = String.format("%s : %-20s %s -> %s",
								LocalDateTime.ofInstant(entry
										.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(),
								(entry.getOldValue().isOpen() ? openstatus[1]
										: openstatus[0]),
								(entry.getNewValue().isOpen() ? openstatus[1]
										: openstatus[0]));
						return new Pair<Entity, String>(actor, logText);
					}).forEach(logList::add);
			break;
		case 20://pillory
			m_logReader
			.getPlayerStatusChanges(m_from, m_to, null)
			.stream()
			.filter(entry -> (entry.getNewValue().getPlayerStatus().indexOf('b') > -1 && entry.getOldValue().getPlayerStatus().lastIndexOf('b') == -1)
							|| entry.getNewValue().getPlayerStatus().indexOf('b') == -1 && entry.getOldValue().getPlayerStatus().lastIndexOf('b') > -1)
			.map(entry -> {
				Entity actor = entry.getOwner();
				String logText = String.format("%s : %-20s %6s -> %6s",
						LocalDateTime.ofInstant(entry
								.getInstant(), ZoneId.systemDefault()).toLocalDate(), actor.getName(),
						entry.getOldValue().getPlayerStatus(), entry.getNewValue().getPlayerStatus());
				return new Pair<Entity, String>(actor, logText);
			}).forEach(logList::add);
			break;		
		default:
			break;

		}

		return logList;
	}
}
