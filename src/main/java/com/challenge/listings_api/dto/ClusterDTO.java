package com.challenge.listings_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClusterDTO {
    private Double lat;
    private Double lon;
    private Integer count;
}