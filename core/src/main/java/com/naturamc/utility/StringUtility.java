package com.naturamc.utility;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niklas Eicker
 */
public class StringUtility {

    public static List<String> color(List<String> list) {
        ArrayList<String> toReturn = new ArrayList(list);
        for (int i = 0; i < list.size(); i++) {
            toReturn.set(i, color(toReturn.get(i)));
        }
        return toReturn;
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int sec = seconds % 60;
        return (minutes < 10 ? "0" + String.valueOf(minutes) : String.valueOf(minutes))
                + ":" + (sec < 10 ? "0" + String.valueOf(sec) : String.valueOf(sec));
    }
}
