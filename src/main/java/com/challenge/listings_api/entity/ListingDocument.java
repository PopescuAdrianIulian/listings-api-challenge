package com.challenge.listings_api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String listingType;

    @Field(type = FieldType.Integer)
    private Integer rooms;

    @Field(type = FieldType.Double)
    private Double areaSqm;

    @Field(type = FieldType.Long)
    private Double price;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String tags;

    private String location;

    @Field(type = FieldType.Integer)
    private Integer floor;

    public static ListingDocument fromListing(com.challenge.listings_api.entity.Listing listing) {
        return ListingDocument.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .listingType(listing.getListingType())
                .rooms(listing.getRooms())
                .areaSqm(listing.getAreaSqm())
                .price(listing.getPrice())
                .tags(listing.getTags())
                .location(listing.getLat() + "," + listing.getLon())
                .floor(listing.getFloor())
                .build();
    }
}