package com.javaweb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AiRecommendationResponse {
    @JsonProperty("semantic_query")
    private String semanticQuery;

    @JsonProperty("filters")
    private Filters filters;

    @JsonProperty("personalized_headline")
    private String personalizedHeadline;

    @JsonProperty("reason_badges")
    private Map<String, String> reasonBadges;

    /**
     * Lời khuyên an toàn / phòng tránh chấn thương đối với các vùng cơ/khớp yếu của người dùng
     */
    @JsonProperty("injury_safety_advice")
    private String injurySafetyAdvice;

    @Getter
    @Setter
    public static class Filters {
        @JsonProperty("target_sports")
        private List<String> targetSports;

        @JsonProperty("categories")
        private List<String> categories;

        @JsonProperty("price_max")
        private BigDecimal priceMax;

        @JsonProperty("exclude_product_ids")
        private List<String> excludeProductIds;

        @JsonProperty("exclude_categories")
        private List<String> excludeCategories;

        /**
         * Mở rộng: Trình độ mục tiêu (Beginner, Intermediate, Advanced)
         */
        @JsonProperty("fitness_level")
        private String fitnessLevel;

        /**
         * Mở rộng: Mục đích sử dụng / Công năng thích hợp (Bảo vệ khớp gối, Chạy marathon, Phục hồi cơ...)
         */
        @JsonProperty("suitable_for")
        private List<String> suitableFor;
    }
}
