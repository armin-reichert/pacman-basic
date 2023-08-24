package de.amr.games.pacman.event;

/**
 * @author Armin Reichert
 */
public enum GameEventType {
  BONUS_ACTIVATED,
  BONUS_EATEN,
  BONUS_EXPIRED,
  CREDIT_ADDED,
  EXTRA_LIFE_WON,
  GAME_STATE_CHANGED,
  GHOST_EATEN,
  GHOST_ENTERS_HOUSE,
  GHOST_STARTS_RETURNING_HOME,
  HUNTING_PHASE_STARTED,
  INTERMISSION_STARTED,
  LEVEL_CREATED,
  LEVEL_STARTED,
  PAC_DIED,
  PAC_FOUND_FOOD,
  PAC_GETS_POWER,
  PAC_LOST_POWER,
  PAC_STARTS_LOSING_POWER,
  READY_TO_PLAY,
  STOP_ALL_SOUNDS,

  UNSPECIFIED_CHANGE;
}