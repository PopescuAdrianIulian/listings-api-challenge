package com.challenge.listings_api.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ListingDetailsDTO {
    private String id;
    private String title;
    private String description;
    private Integer rooms;
    private Double areaSqm;
    private Double price;
    private String listingType;
    private String tags;
    private Double lat;
    private Double lon;
    private Integer floor;
}