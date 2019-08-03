package darksky.ogameapidatabasefx.database.entities;

import java.util.List;

/**
 * An interface for wrapper classes which contain an entity and the associated highscores
 * 
 * @author Dark Sky
 *
 */
public interface IHighscoreEntity{
	public Entity getEntity();
	public List<Highscore> getHighscores();
}
