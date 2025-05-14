package org.oagi.score.gateway.http.common.model;

import java.util.Calendar;
import java.util.Date;

public record DateRangeCriteria(Date after, Date before) {

    public static DateRangeCriteria create(String expression) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        if (!expression.matches("\\[\\d*~\\d*]")) {
            throw new IllegalArgumentException("Invalid date range format. Expected format: [start~end]");
        }

        String[] parts = expression.substring(1, expression.length() - 1).split("~", -1); // Ensures empty parts are included
        Long afterEpoch = parts[0].isEmpty() ? null : Long.parseLong(parts[0]);
        Long beforeEpoch = (parts.length > 1 && !parts[1].isEmpty()) ? Long.parseLong(parts[1]) : null;

        return create(afterEpoch, beforeEpoch);
    }

    public static DateRangeCriteria create(Long afterEpoch, Long beforeEpoch) {
        Date after = null;
        if (afterEpoch != null) {
            after = new Date(afterEpoch);
        }
        Date before = null;
        if (beforeEpoch != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beforeEpoch);
            calendar.add(Calendar.DATE, 1);
            before = calendar.getTime();
        }

        return new DateRangeCriteria(after, before);
    }

}
