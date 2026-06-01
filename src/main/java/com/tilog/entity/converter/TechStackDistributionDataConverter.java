package com.tilog.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilog.dto.report.TechStackDistributionData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

@Converter
public class
TechStackDistributionDataConverter implements AttributeConverter<TechStackDistributionData, String> {

    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(TechStackDistributionData data) {
        if (data == null) return null;
        try {
            return om.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("TechStackDistributionData 직렬화 실패", e);
        }
    }

    @Override
    public TechStackDistributionData convertToEntityAttribute(String json) {
        if (!StringUtils.hasText(json)) return null;
        try {
            return om.readValue(json, TechStackDistributionData.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("TechStackDistributionData 역직렬화 실패", e);
        }
    }
}