package com.weather.platform.backend.collection.service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PrecipitationTextParser {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+(\\.[0-9]+)?)");

    private PrecipitationTextParser() {
    }

    static BigDecimal parse(String value) {
        if (value == null) {
            return null;
        }
        if (value.contains("강수없음") || value.contains("미만")) {
            return BigDecimal.ZERO;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(value);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }
}
