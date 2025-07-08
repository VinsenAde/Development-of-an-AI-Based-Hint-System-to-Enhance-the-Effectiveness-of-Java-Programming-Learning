//package com.thesis.java.javalearning.util;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import jakarta.persistence.AttributeConverter;
//import jakarta.persistence.Converter;
//import java.util.HashMap;
//import java.util.Map;
//
//@Converter
//public class HintCountConverter implements AttributeConverter<Map<String, Integer>, String> {
//    private static final ObjectMapper mapper = new ObjectMapper();
//
//    @Override
//    public String convertToDatabaseColumn(Map<String, Integer> map) {
//        if (map == null) return "{}";
//        try {
//            return mapper.writeValueAsString(map);
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Failed to convert Map to JSON", e);
//        }
//    }
//
//    @Override
//    public Map<String, Integer> convertToEntityAttribute(String json) {
//        if (json == null || json.isEmpty()) return new HashMap<>();
//        try {
//            return mapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
//        } catch (Exception e) {
//            return new HashMap<>();
//        }
//    }
//}
package com.thesis.java.javalearning.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

@Converter
public class HintCountConverter implements AttributeConverter<Map<String, Integer>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    // Convert Map to JSON string before saving to database
    @Override
    public String convertToDatabaseColumn(Map<String, Integer> map) {
        if (map == null) return "{}"; // Return empty JSON object if map is null
        try {
            return mapper.writeValueAsString(map); // Serialize the map into a JSON string
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert Map to JSON", e); // Handle any errors during conversion
        }
    }

    // Convert JSON string back to Map when reading from database
    @Override
    public Map<String, Integer> convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>(); // Return an empty map if the JSON is null or empty
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Integer>>() {}); // Deserialize JSON back into a map
        } catch (Exception e) {
            return new HashMap<>(); // Return an empty map if there is an error during deserialization
        }
    }
}
