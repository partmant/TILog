package com.tilog.domain.report.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilog.domain.report.dto.CumulativeStatsData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

@Converter
public class CumulativeStatsDataConverter implements AttributeConverter<CumulativeStatsData, String> {

    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CumulativeStatsData data) {
        if (data == null) return null;
        try {
            return om.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("CumulativeStatsData 직렬화 실패", e);
        }
    }

    @Override
    public CumulativeStatsData convertToEntityAttribute(String json) {
        if (!StringUtils.hasText(json)) return null;
        try {
            return om.readValue(json, CumulativeStatsData.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("CumulativeStatsData 역직렬화 실패", e);
        }
    }
}