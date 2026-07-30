package com.tilog.domain.report.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilog.domain.report.dto.WeeklySummaryData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

@Converter
public class WeeklySummaryDataConverter implements AttributeConverter<WeeklySummaryData, String> {

    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(WeeklySummaryData data) {
        if (data == null) return null;
        try {
            return om.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("WeeklySummaryData 직렬화 실패", e);
        }
    }

    @Override
    public WeeklySummaryData convertToEntityAttribute(String json) {
        if (!StringUtils.hasText(json)) return null;
        try {
            return om.readValue(json, WeeklySummaryData.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("WeeklySummaryData 역직렬화 실패", e);
        }
    }
}