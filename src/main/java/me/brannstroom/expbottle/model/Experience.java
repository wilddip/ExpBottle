package me.brannstroom.expbottle.model;

import org.bukkit.entity.Player;

/**
 * Utility class for handling player experience calculations based on vanilla
 * formulas.
 */
public class Experience {

	/**
	 * Calculate a player's total experience based on level and progress to next
	 * level.
	 * From Essentials.
	 * 
	 * @param player Player to calculate experience for.
	 * @return Total experience points of player.
	 */
	public static int getExp(Player player) {
		return getExpFromLevel(player.getLevel()) + Math.round(getExpToNext(player.getLevel()) * player.getExp());
	}

	/**
	 * Calculate total experience required to reach a level.
	 * From Essentials.
	 * 
	 * @param level Level to calculate.
	 * @return Total experience points required.
	 */
	public static int getExpFromLevel(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		} else if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		} else {
			return level * level + 6 * level;
		}
	}

	/**
	 * Calculate experience points needed to progress to the next level.
	 * From Essentials.
	 * 
	 * @param level Current level.
	 * @return Experience points needed.
	 */
	public static int getExpToNext(int level) {
		if (level >= 30) {
			return 9 * level - 158;
		} else if (level >= 15) {
			return 5 * level - 38;
		} else {
			return 2 * level + 7;
		}
	}

	/**
	 * Change a player's experience by a given amount.
	 * From Essentials.
	 * 
	 * @param player Player to modify experience for.
	 * @param amount Amount of experience to add (or subtract if negative).
	 */
	public static void changeExp(Player player, int amount) {
		int experience = getExp(player) + amount;
		if (experience < 0) {
			experience = 0;
		}
		setExp(player, experience);
	}

	/**
	 * Set a player's total experience.
	 * Based on Essentials but simplified.
	 * 
	 * @param player Player to set experience for.
	 * @param exp    Amount of experience points.
	 */
	private static void setExp(Player player, int exp) {
		player.setTotalExperience(0);
		player.setLevel(0);
		player.setExp(0);

		if (exp > 0) {
			player.giveExp(exp);
		}
	}

	/**
	 * Calculates level based on total experience.
	 * 
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 *      "One can determine how much experience has been collected to reach a
	 *      level using the equations:
	 * 
	 *      Total Experience = [Level]2 + 6[Level] (at levels 0-15)
	 *      2.5[Level]2 - 40.5[Level] + 360 (at levels 16-30)
	 *      4.5[Level]2 - 162.5[Level] + 2220 (at level 31+)"
	 * 
	 * @param exp the total experience
	 * 
	 * @return the level calculated
	 */
	public static double getLevelFromExp(long exp) {
		if (exp > 1395) {
			return (Math.sqrt(72 * exp - 54215) + 325) / 18;
		}
		if (exp > 315) {
			return Math.sqrt(40 * exp - 7839) / 10 + 8.1;
		}
		if (exp > 0) {
			return Math.sqrt(exp + 9) - 3;
		}
		return 0;
	}
}