package com.javaweb.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    Nam("Nam"),
    Nữ("Nữ"),
    Khác("Khác"),
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (Gender gender : Gender.values()) {
            if (gender.value.equalsIgnoreCase(value.trim()) || gender.name().equalsIgnoreCase(value.trim())) {
                return gender;
            }
        }
        return null;
    }
}
