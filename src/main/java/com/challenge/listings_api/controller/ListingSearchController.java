package com.challenge.listings_api.controller;

import com.challenge.listings_api.dto.ListingDetailsDTO;
import com.challenge.listings_api.service.ListingElasticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ListingSearchController {

    private final ListingElasticSearchService elasticSearchService;

    @GetMapping("/search/title")
    public ResponseEntity<List<ListingDetailsDTO>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(elasticSearchService.findByTitle(title));
    }

    @GetMapping("/search/type")
    public ResponseEntity<List<ListingDetailsDTO>> searchByType(@RequestParam String listingType) {
        return ResponseEntity.ok(elasticSearchService.findByListingType(listingType));
    }

    @GetMapping("/search/price")
    public ResponseEntity<List<ListingDetailsDTO>> searchByPrice(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        return ResponseEntity.ok(elasticSearchService.findByPriceBetween(minPrice, maxPrice));
    }

    @GetMapping("/search/tag")
    public ResponseEntity<List<ListingDetailsDTO>> searchByTag(@RequestParam String tag) {
        return ResponseEntity.ok(elasticSearchService.findByTags(tag));
    }
}