package darksky.ogameapidatabasefx.application.gui;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import darksky.ogameapidatabasefx.database.databasemanagement.OGameAPIDatabase;
import darksky.ogameapidatabasefx.database.statistics.StatisticsReader;
import darksky.ogameapidatabasefx.database.statistics.XYDistribution;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class PMDistributionTask extends
		Task<Map<OGameAPIDatabase, List<Image>>> {

	final List<OGameAPIDatabase> m_controllers;
	final LocalDate m_from;
	final LocalDate m_to;
	final File m_saveFolderFile;

	public PMDistributionTask(List<OGameAPIDatabase> controllers,
			LocalDate from, LocalDate to, File saveFolderFile) {
		super();
		m_controllers = controllers;
		m_from = from;
		m_to = to;
		m_saveFolderFile = saveFolderFile;
	}

	@Override
	protected Map<OGameAPIDatabase, List<Image>> call() throws Exception {

		Map<OGameAPIDatabase, List<Image>> returnMap = new HashMap<>();

		final int maxProgress = m_controllers.size();
		int progress = 0;

		for (OGameAPIDatabase controller : m_controllers) {
			List<Image> images = new ArrayList<>();
			StatisticsReader reader = controller.getStatisticsReader();
			try {
				List<XYDistribution> pdistributions;
				List<XYDistribution> mdistributions;
				if (LocalDate.MAX.equals(m_to)) {
					pdistributions = Collections.singletonList(reader
							.getNewestPlanetDistribution());

					mdistributions = Collections.singletonList(reader
							.getNewestMoonDistribution());
				} else {
					pdistributions = reader
							.getPlanetDistributions(m_from, m_to);

					mdistributions = reader.getMoonDistributions(m_from, m_to);
				}
				assert pdistributions.size() == mdistributions.size();
				for (int i = 0; i < pdistributions.size(); ++i) {
					XYDistribution pd = pdistributions.get(i);
					XYDistribution md = mdistributions.get(i);
					Image distributionImage = generatePlanetMoonDistributionImage(
							pd, md);

					if (m_saveFolderFile != null) {
						final String fileName = controller.getServerPrefix()
								+ "_PMDistribution_" + pd.getDate() + ".png";
						final File saveFile = new File(m_saveFolderFile,
								fileName);

						RenderedImage renderedImage = SwingFXUtils.fromFXImage(
								distributionImage, null);
						try {
							if (!ImageIO.write(renderedImage, "png", saveFile)) {
								Util.getLogger().severe(
										"Could not write file "
												+ m_saveFolderFile);
							}
						} catch (IOException e) {
							Util.getLogger().log(Level.SEVERE,
									this.getClass().getName(), e);
						}
					} else {
						images.add(distributionImage);
					}
				}
			} catch (Exception e) {
				Util.getLogger()
						.log(Level.SEVERE, this.getClass().getName(), e);
			}
			returnMap.putIfAbsent(controller, images);
			updateProgress(++progress, maxProgress);
		}

		return returnMap;
	}

	private Image generatePlanetMoonDistributionImage(
			XYDistribution planetDistribution,
			XYDistribution moonDistribution) throws SQLException {

		if ((planetDistribution == null && moonDistribution == null)
				|| ((planetDistribution == null || moonDistribution == null) && !planetDistribution
						.getDate().equals(moonDistribution.getDate()))) {
			return new WritableImage(0, 0);
		}

		final int pixelPerGalaxy = 20;
		final int pixelPerSystem = 2;
		final int galaxies = planetDistribution.getGalaxies();
		final int systems = planetDistribution.getSystems();

		final int separatorHeight = 8;

		final int imageWidth = pixelPerSystem * systems;
		final int imageHeight = 2 * pixelPerGalaxy * galaxies + separatorHeight;

		WritableImage image = new WritableImage(imageWidth, imageHeight);
		PixelWriter writer = image.getPixelWriter();

		if (planetDistribution != null) {
			int[][] pdistri = planetDistribution.getDistribution();

			// planet distribution
			for (int galaxy = 0; galaxy < galaxies; ++galaxy) {
				for (int system = 0; system < systems; ++system) {
					int value = pdistri[galaxy][system];
					Color c = Color.rgb(value == 0 ? 255 : 32,
							255 - 16 * value, 255 - 16 * value);
					for (int ps = 0; ps < pixelPerSystem; ++ps) {
						for (int pg = 0; pg < pixelPerGalaxy; ++pg) {
							writer.setColor(system * pixelPerSystem + ps,
									galaxy * pixelPerGalaxy + pg, c);
						}
					}
				}
			}

			// separator rectangle

			for (int y = pixelPerGalaxy * galaxies; y < pixelPerGalaxy
					* galaxies + separatorHeight; ++y) {
				for (int x = 0; x < imageWidth; ++x) {
					writer.setColor(x, y, Color.BROWN);
				}
			}
		}
		if (moonDistribution != null) {

			int[][] mdistri = moonDistribution.getDistribution();

			// moon distribution

			for (int galaxy = 0; galaxy < galaxies; ++galaxy) {
				for (int system = 0; system < systems; ++system) {
					int value = mdistri[galaxy][system];
					Color c = Color.rgb(value == 0 ? 255 : 32,
							255 - 16 * value, 255 - 16 * value);
					for (int ps = 0; ps < pixelPerSystem; ++ps) {
						for (int pg = 0; pg < pixelPerGalaxy; ++pg) {
							writer.setColor(
									system * pixelPerSystem + ps,
									(galaxies * pixelPerGalaxy + separatorHeight)
											+ galaxy * pixelPerGalaxy + pg, c);
						}
					}
				}
			}
		}

		return image;
	}

}
