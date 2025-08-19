package com.example.taskmanager.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class Sanitize {
    private static final Safelist SAFE = Safelist.basic(); // allow <b>,<i>,<ul> etc; or use Safelist.none()

    public static String html(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, SAFE);
    }
}