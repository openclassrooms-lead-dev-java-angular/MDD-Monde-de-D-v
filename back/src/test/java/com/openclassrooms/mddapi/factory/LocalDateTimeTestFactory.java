package com.openclassrooms.mddapi.factory;

import java.time.LocalDateTime;

public class LocalDateTimeTestFactory {
    public static LocalDateTime generateCreatedAt() {

        return LocalDateTime.of(
                2026,
                9,
                16,
                10,
                0
        );
    }

    public static LocalDateTime generateUpdatedAt() {

        return LocalDateTime.of(
                2026,
                9,
                16,
                11,
                0
        );
    }
}
