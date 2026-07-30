package com.tilog.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CurrentStatus {
    JOB_SEEKER("취준생"),
    STUDENT("학생"),
    EMPLOYED("재직자"),
    CAREER_CHANGE("이직준비자"),
    FREELANCER("프리랜서");

    private final String label;

    CurrentStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static CurrentStatus from(String value) {
        for (CurrentStatus s : values()) {
            if (s.name().equalsIgnoreCase(value) || s.label.equals(value)) {
                return s;
            }
        }
        return null;
    }
}
