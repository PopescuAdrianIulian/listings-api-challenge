package com.challenge.listings_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ListingSummaryDTO {
    private String id;
    private Integer rooms;

    @JsonProperty("area_sqm")
    private Double areaSqm;

    private Double price;

    @JsonProperty("listing_type")
    private String listingType;

    @JsonRawValue
    private String tags;

    private Double lat;
    private Double lon;
    private Integer floor;
}