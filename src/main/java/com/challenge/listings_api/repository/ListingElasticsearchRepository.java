package com.challenge.listings_api.repository;

import com.challenge.listings_api.entity.ListingDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingElasticsearchRepository extends ElasticsearchRepository<ListingDocument, String> {
    List<ListingDocument> findByTitleContains(String title);

    List<ListingDocument> findByListingType(String listingType);

    List<ListingDocument> findByPriceBetween(Double minPrice, Double maxPrice);

    List<ListingDocument> findByTags(String tag);

}