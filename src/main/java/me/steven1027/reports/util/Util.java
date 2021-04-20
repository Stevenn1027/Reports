package me.steven1027.reports.util;

import net.kyori.text.TextComponent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

public class Util {

    public static TextComponent color(String text) {
        return LegacyComponentSerializer.legacy().deserialize(text, '&');
    }

}
