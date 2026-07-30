package com.tilog.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TargetJob {
    BACKEND("백엔드 개발자"),
    FRONTEND("프론트엔드 개발자"),
    FULLSTACK("풀스택 개발자"),
    MOBILE_ANDROID("안드로이드 앱 개발자"),
    MOBILE_IOS("iOS 앱 개발자"),
    DATA_ENGINEER("데이터 엔지니어"),
    AI_ML_ENGINEER("AI / ML 엔지니어"),
    INFRA_DEVOPS("인프라 / DevOps 엔지니어"),
    GAME_DEVELOPER("게임 개발자"),
    EMBEDDED("임베디드 / IoT 개발자"),
    PRODUCT_MANAGER("기획자 / PM / PO"),
    ETC("기타 / 미정");

    private final String label;

    TargetJob(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static TargetJob from(String value) {
        for (TargetJob j : values()) {
            if (j.name().equalsIgnoreCase(value) || j.label.equals(value)) {
                return j;
            }
        }
        return null;
    }
}
