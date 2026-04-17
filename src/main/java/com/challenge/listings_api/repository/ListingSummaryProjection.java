package com.challenge.listings_api.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Această proiecție îi spune lui Hibernate să genereze:
 * SELECT id, rooms, area_sqm, price, listing_type, tags, lat, lon, floor FROM listings...
 * În loc de SELECT * (care includea coloane de tip TEXT/MEDIUMTEXT foarte mari).
 */
public interface ListingSummaryProjection {
    String getId();
    Integer getRooms();

    @JsonProperty("area_sqm")
    Double getAreaSqm();

    Double getPrice();

    @JsonProperty("listing_type")
    String getListingType();

    @JsonRawValue
    String getTags();

    Double getLat();
    Double getLon();
    Integer getFloor();
}