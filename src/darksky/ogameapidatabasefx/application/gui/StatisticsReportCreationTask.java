package darksky.ogameapidatabasefx.application.gui;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.stream.Collectors;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.statistics.ServerActivityState;
import darksky.ogameapidatabasefx.database.statistics.ServerHistory;
import javafx.concurrent.Task;

class StatisticsReportCreationTask extends Task<List<String>> {

	private List<OGameAPIDatabase> m_controllerList = null;
	private LocalDate m_dateFrom = null;
	private LocalDate m_dateTo = null;
	private Path m_saveFolderPath = null;
	private ResourceBundle m_resources = null;

	private static NumberFormat nf = NumberFormat
			.getNumberInstance(Locale.GERMANY);

	StatisticsReportCreationTask(List<OGameAPIDatabase> controllerList,
			LocalDate dateFrom, LocalDate dateTo, Path saveFolderPath,
			ResourceBundle resources) {
		super();
		m_controllerList = Objects.requireNonNull(controllerList);
		m_dateFrom = Objects.requireNonNull(dateFrom);
		m_dateTo = Objects.requireNonNull(dateTo);
		m_saveFolderPath = Objects.requireNonNull(saveFolderPath);
		m_resources = resources;
	}

	@Override
	protected List<String> call() throws Exception {
		final int controllers = m_controllerList.size();
		final int maxProgress = controllers;
		int progress = 0;

		final List<ServerHistory> historyList = new ArrayList<ServerHistory>(
				controllers);
		final List<String> failedReports = new ArrayList<String>();

		// create single reports for each server
		for (OGameAPIDatabase controller : m_controllerList) {

			try {
				ServerHistory history = controller.getStatisticsReader()
						.generateServerHistory(m_dateFrom, m_dateTo);
				if (history != null) {
					historyList.add(history);
					saveServerHistoryToFile(history,
							m_saveFolderPath.resolve(controller
									.getServerPrefix()
									+ "_report_"
									+ m_dateFrom
									+ "_"
									+ m_dateTo.minusDays(1)
									+ ".txt"));
				}
			} catch (SQLException | IOException e) {
				Util.getLogger()
						.log(Level.SEVERE, this.getClass().getName(), e);
				failedReports.add(controller.getServerPrefix());
			}

			this.updateProgress(++progress, maxProgress);
		}

		try {
			saveCombinedReportToFile(
					historyList,
					m_saveFolderPath.resolve("combined_report_" + m_dateFrom
							+ "_" + m_dateTo.minusDays(1) + ".txt"));
		} catch (IOException e) {
			Util.getLogger().log(Level.SEVERE, this.getClass().getName(), e);
		}

		this.updateProgress(++progress, maxProgress);

		return failedReports;
	}

	private void saveCombinedReportToFile(List<ServerHistory> historyList,
			Path savePath) throws IOException {

		final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(
				Locale.getDefault());
		final DecimalFormat df = new DecimalFormat("#0.00", otherSymbols);

		try (Writer w = Files.newBufferedWriter(savePath,
				Charset.forName("UTF-8"), StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {

			Collections.sort(historyList, (h1, h2) -> {
				int p1 = h1.getNewState().getNumberOfPlayers();
				int a1 = h1.getNewState().getNumberOfActivePlayers();
				int p2 = h2.getNewState().getNumberOfPlayers();
				int a2 = h2.getNewState().getNumberOfActivePlayers();
				return (100.0 * a1 / p1 > 100.0 * a2 / p2) ? -1 : 1;
			});

			final StringBuilder activityRankingText = new StringBuilder();

			historyList.forEach(h -> {
				final int pnew = h.getNewState().getNumberOfPlayers();
				final int anew = h.getNewState().getNumberOfActivePlayers();
				final double rationew = 100.0 * anew / pnew;
				final int pold = h.getOldState().getNumberOfPlayers();
				final int aold = h.getOldState().getNumberOfActivePlayers();
				final double ratioold = 100.0 * aold / pold;
				final double diff = rationew - ratioold;

				final String[] tmp = h.getNewState().getServerPrefix()
						.split("-");
				String servername = ServerNameConstants.serverToName
						.getOrDefault(tmp[0], "");
				if (servername.equals("")) {
					servername = "uni" + tmp[0].substring(1);
				}
				servername += "-" + tmp[1];

				activityRankingText.append(String.format(
						"%14s : %5s%% (%1s%5s%%)%n", servername,
						df.format(rationew), (diff >= 0 ? "+" : "-"),
						df.format(Math.abs(diff))));
			});
			activityRankingText.append("\n\n");
			w.append(activityRankingText);

			Collections.sort(
					historyList,
					(h1, h2) -> {
						return Integer.compare(h2.getNumberOfRelocations(),
								h1.getNumberOfRelocations());
					});

			final StringBuilder relocationRankingText = new StringBuilder();

			historyList.forEach(h -> {
				final String[] tmp = h.getNewState().getServerPrefix()
						.split("-");
				String servername = ServerNameConstants.serverToName
						.getOrDefault(tmp[0], "");
				if (servername.equals("")) {
					servername = "uni" + tmp[0].substring(1);
				}
				servername += "-" + tmp[1];

				final int u = h.getNumberOfRelocations();
				final int up = h.getNumberOfRelocatedPlayers();

				relocationRankingText.append(String.format("%14s : %6s %6s%n",
						servername, nf.format(u), nf.format(up)));
			});

			relocationRankingText.append("\n\n");
			w.append(relocationRankingText);

			final StringBuilder totalChangesText = new StringBuilder();
			final List<ServerActivityState> states = historyList.stream()
					.map(h -> h.getNewState()).collect(Collectors.toList());
			final int totalPlayers = states.stream()
					.mapToInt(s -> s.getNumberOfPlayers()).sum();
			final int totalActivePlayers = states.stream()
					.mapToInt(s -> s.getNumberOfActivePlayers()).sum();
			final int totalInactivePlayers = states.stream()
					.mapToInt(s -> s.getNumberOfInactivePlayers()).sum();
			final int totalVModePlayers = states.stream()
					.mapToInt(s -> s.getNumberOfVModePlayers()).sum();
			final int totalLongtimeInactivePlayers = states.stream()
					.mapToInt(s -> s.getNumberOfLongtimeInactivePlayers())
					.sum();
			final int totalNotDeletablePlayers = states.stream()
					.mapToInt(s -> s.getNumberOfNotDeletablePlayers()).sum();

			totalChangesText.append(String.format("%s:%n",
					m_resources.getString("total")));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("players"), nf.format(totalPlayers)));
			totalChangesText.append(String.format("%s : %s (%5s%%)%n",
					m_resources.getString("active"),
					nf.format(totalActivePlayers),
					df.format(100.0 * totalActivePlayers / totalPlayers)));
			totalChangesText.append(String.format("%s : %s (%5s%%)%n",
					m_resources.getString("vacationmode"),
					nf.format(totalVModePlayers),
					df.format(100.0 * totalVModePlayers / totalPlayers)));
			totalChangesText.append(String.format("%s : %s (%5s%%)%n",
					m_resources.getString("inactive"),
					nf.format(totalInactivePlayers),
					df.format(100.0 * totalInactivePlayers / totalPlayers)));
			totalChangesText.append(String.format(
					">28 %s : %s (%5s%%)%n",
					m_resources.getString("inactive"),
					nf.format(totalLongtimeInactivePlayers),
					df.format(100.0 * totalLongtimeInactivePlayers
							/ totalPlayers)));
			totalChangesText
					.append(String.format(
							">35 %s : %s (%5s%%)%n",
							m_resources.getString("inactive"),
							nf.format(totalNotDeletablePlayers),
							df.format(100.0 * totalNotDeletablePlayers
									/ totalPlayers)));
			totalChangesText.append("\n\n");

			final int ahc = historyList.stream()
					.mapToInt(h -> h.getNumberOfAllianceHomepageChanges())
					.sum();
			final int alc = historyList.stream()
					.mapToInt(h -> h.getNumberOfAllianceLogoChanges()).sum();
			final int amc = historyList.stream()
					.mapToInt(h -> h.getNumberOfAllianceMemberChanges()).sum();
			final int anc = historyList.stream()
					.mapToInt(h -> h.getNumberOfAllianceNameChanges()).sum();
			final int aoc = historyList.stream()
					.mapToInt(h -> h.getNumberOfAllianceOpenChanges()).sum();
			final int atc = historyList.stream()
					.mapToInt(h -> h.getNumberOfAllianceTagChanges()).sum();
			final int adp = historyList.stream()
					.mapToInt(h -> h.getNumberOfAutoDeletedPlayers()).sum();
			final int da = historyList.stream()
					.mapToInt(h -> h.getNumberOfDeletedAlliances()).sum();
			final int dm = historyList.stream()
					.mapToInt(h -> h.getNumberOfDeletedMoons()).sum();
			final int dplanets = historyList.stream()
					.mapToInt(h -> h.getNumberOfDeletedPlanets()).sum();
			final int dplayers = historyList.stream()
					.mapToInt(h -> h.getNumberOfDeletedPlayers()).sum();
			final int mnc = historyList.stream()
					.mapToInt(h -> h.getNumberOfMoonNameChanges()).sum();
			final int na = historyList.stream()
					.mapToInt(h -> h.getNumberOfNewAlliances()).sum();
			final int nm = historyList.stream()
					.mapToInt(h -> h.getNumberOfNewMoons()).sum();
			final int nplanets = historyList.stream()
					.mapToInt(h -> h.getNumberOfNewPlanets()).sum();
			final int nplayers = historyList.stream()
					.mapToInt(h -> h.getNumberOfNewPlayers()).sum();
			final int planetnc = historyList.stream()
					.mapToInt(h -> h.getNumberOfPlanetNameChanges()).sum();
			final int playernc = historyList.stream()
					.mapToInt(h -> h.getNumberOfPlayerNameChanges()).sum();
			final int psc = historyList.stream()
					.mapToInt(h -> h.getNumberOfPlayerStatusChanges()).sum();
			final int rp = historyList.stream()
					.mapToInt(h -> h.getNumberOfRelocatedPlayers()).sum();
			final int r = historyList.stream()
					.mapToInt(h -> h.getNumberOfRelocations()).sum();

			totalChangesText.append(String.format("%s%n",
					m_resources.getString("changes")));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("newplayers"), nf.format(nplayers)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("deletedplayers"),
					nf.format(dplayers)));
			totalChangesText.append(String.format("%s, %s: %s%n",
					m_resources.getString("deletedplayers"),
					m_resources.getString("inactive"), nf.format(adp)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("playernames"), nf.format(playernc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("statuschanges"), nf.format(psc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("relocatedplayers"), nf.format(rp)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("newalliances"), nf.format(na)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("deletedalliances"), nf.format(da)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("alliancenames"), nf.format(anc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("tags"), nf.format(atc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("alliancechanges"), nf.format(amc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("homepages"), nf.format(ahc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("logos"), nf.format(alc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("applications"), nf.format(aoc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("newplanets"), nf.format(nplanets)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("deletedplanets"),
					nf.format(dplanets)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("planetnames"), nf.format(planetnc)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("relocations"), nf.format(r)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("newmoons"), nf.format(nm)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("deletedmoons"), nf.format(dm)));
			totalChangesText.append(String.format("%s : %s%n",
					m_resources.getString("moonnames"), nf.format(mnc)));

			w.append(totalChangesText);

			w.flush();
		}
	}

	private void saveServerHistoryToFile(ServerHistory history, Path savePath)
			throws IOException {
		Objects.requireNonNull(history);
		Objects.requireNonNull(savePath);

		final DecimalFormat df = new DecimalFormat("##.##");

		final ServerActivityState oldState = history.getOldState();
		final int oldNumberOfPlayers = oldState.getNumberOfPlayers();
		final int oldNumberOfVModePlayers = oldState.getNumberOfVModePlayers();
		final int oldNumberOfInactivePlayers = oldState
				.getNumberOfInactivePlayers();
		final int oldNumberOfLongtimeInactivePlayers = oldState
				.getNumberOfLongtimeInactivePlayers();
		final int oldNumberOfNotDeletablePlayers = oldState
				.getNumberOfNotDeletablePlayers();

		final ServerActivityState newState = history.getNewState();
		final int newNumberOfPlayers = newState.getNumberOfPlayers();
		final int newNumberOfVModePlayers = newState.getNumberOfVModePlayers();
		final int newNumberOfInactivePlayers = newState
				.getNumberOfInactivePlayers();
		final int newNumberOfLongtimeInactivePlayers = newState
				.getNumberOfLongtimeInactivePlayers();
		final int newNumberOfNotDeletablePlayers = newState
				.getNumberOfNotDeletablePlayers();

		StringBuilder reportText = new StringBuilder();
		reportText
				.append(String.format("%s %s%n", m_resources
						.getString("history"), history.getOldState()
						.getServerPrefix()));

		reportText.append(oldState.getDateTime() + " -> "
				+ newState.getDateTime() + "\n\n");
		reportText.append(String.format("%s:%n",
				m_resources.getString("oldactivity")));
		reportText.append(String.format("%s : %d%n",
				m_resources.getString("players"), oldNumberOfPlayers));
		reportText
				.append(String.format(
						"%s : %d (%s%%)%n",
						m_resources.getString("vacationmode"),
						oldNumberOfVModePlayers,
						df.format(100.0 * oldNumberOfVModePlayers
								/ oldNumberOfPlayers)));
		reportText.append(String.format(
				"%s : %d (%s%%)%n",
				m_resources.getString("inactive"),
				oldNumberOfInactivePlayers,
				df.format(100.0 * oldNumberOfInactivePlayers
						/ oldNumberOfPlayers)));
		reportText.append(String.format(
				">28 %s : %d (%s%%)%n",
				m_resources.getString("inactive"),
				oldNumberOfLongtimeInactivePlayers,
				df.format(100.0 * oldNumberOfLongtimeInactivePlayers
						/ oldNumberOfPlayers)));
		reportText.append(String.format(
				">35 %s : %d (%s%%)%n%n",
				m_resources.getString("inactive"),
				oldNumberOfNotDeletablePlayers,
				df.format(100.0 * oldNumberOfNotDeletablePlayers
						/ oldNumberOfPlayers)));

		reportText.append(oldState.getPlayerActivityMap().entrySet() + "\n\n");

		reportText.append(String.format("%s:%n",
				m_resources.getString("newactivity")));

		reportText.append(String.format("%s : %d%n",
				m_resources.getString("players"), newNumberOfPlayers));
		reportText
				.append(String.format(
						"%s : %d (%s%%)%n",
						m_resources.getString("vacationmode"),
						newNumberOfVModePlayers,
						df.format(100.0 * newNumberOfVModePlayers
								/ newNumberOfPlayers)));
		reportText.append(String.format(
				"%s : %d (%s%%)%n",
				m_resources.getString("inactive"),
				newNumberOfInactivePlayers,
				df.format(100.0 * newNumberOfInactivePlayers
						/ newNumberOfPlayers)));
		reportText.append(String.format(
				">28 %s : %d (%s%%)%n",
				m_resources.getString("inactive"),
				newNumberOfLongtimeInactivePlayers,
				df.format(100.0 * newNumberOfLongtimeInactivePlayers
						/ newNumberOfPlayers)));
		reportText.append(String.format(
				">35 %s : %d (%s%%)%n%n",
				m_resources.getString("inactive"),
				newNumberOfNotDeletablePlayers,
				df.format(100.0 * newNumberOfNotDeletablePlayers
						/ newNumberOfPlayers)));

		reportText.append(newState.getPlayerActivityMap().entrySet() + "\n\n");

		reportText.append(String.format("%s%n",
				m_resources.getString("changes")));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("newplayers"),
				nf.format(history.getNumberOfNewPlayers())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("deletedplayers"),
				nf.format(history.getNumberOfDeletedPlayers())));
		reportText.append(String.format("%s, %s: %s%n",
				m_resources.getString("deletedplayers"),
				m_resources.getString("inactive"),
				nf.format(history.getNumberOfAutoDeletedPlayers())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("playernames"),
				nf.format(history.getNumberOfPlayerNameChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("statuschanges"),
				nf.format(history.getNumberOfPlayerStatusChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("relocatedplayers"),
				nf.format(history.getNumberOfRelocatedPlayers())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("newalliances"),
				nf.format(history.getNumberOfNewAlliances())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("deletedalliances"),
				nf.format(history.getNumberOfDeletedAlliances())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("alliancenames"),
				nf.format(history.getNumberOfAllianceNameChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("tags"),
				nf.format(history.getNumberOfAllianceTagChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("alliancechanges"),
				nf.format(history.getNumberOfAllianceMemberChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("homepages"),
				nf.format(history.getNumberOfAllianceHomepageChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("logos"),
				nf.format(history.getNumberOfAllianceLogoChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("applications"),
				nf.format(history.getNumberOfAllianceOpenChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("newplanets"),
				nf.format(history.getNumberOfNewPlanets())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("deletedplanets"),
				nf.format(history.getNumberOfDeletedPlanets())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("playernames"),
				nf.format(history.getNumberOfPlanetNameChanges())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("relocations"),
				nf.format(history.getNumberOfRelocations())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("newmoons"),
				nf.format(history.getNumberOfNewMoons())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("deletedmoons"),
				nf.format(history.getNumberOfDeletedMoons())));
		reportText.append(String.format("%s : %s%n",
				m_resources.getString("moonnames"),
				nf.format(history.getNumberOfMoonNameChanges())));

		reportText.append("\n\n");

		try (Writer w = Files.newBufferedWriter(savePath,
				Charset.forName("UTF-8"), StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			w.append(reportText);
			w.flush();
		}
	}

}
