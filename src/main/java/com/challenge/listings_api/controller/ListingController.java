package com.challenge.listings_api.controller;

import com.challenge.listings_api.dto.ClusterDTO;
import com.challenge.listings_api.dto.ListingDetailsDTO;
import com.challenge.listings_api.dto.PagedResponse;
import com.challenge.listings_api.repository.ListingSummaryProjection;
import com.challenge.listings_api.service.ListingService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
public class ListingController {

    private final ListingService service;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/listings")
    public PagedResponse<ListingSummaryProjection> getListings(
            @RequestParam(required = false) @PositiveOrZero Integer min_rooms,
            @RequestParam(required = false) @PositiveOrZero Integer max_rooms,
            @RequestParam(required = false) @PositiveOrZero Double min_price,
            @RequestParam(required = false) @PositiveOrZero Double max_price,
            @RequestParam(required = false) @Pattern(regexp = "sale|rent") String listing_type,
            @RequestParam(required = false) @Positive Double min_area,
            @RequestParam(required = false) @Positive Double max_area,
            @RequestParam(required = false) Integer min_floor,
            @RequestParam(required = false) Integer max_floor,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double min_lat,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double max_lat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double min_lon,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double max_lon,
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "10") @Min(1) @Max(500) Integer limit) {
        validateRanges(min_price, max_price, min_rooms, max_rooms, min_area, max_area);

        return service.searchListings(after,
                min_rooms, max_rooms, min_price, max_price,
                listing_type, min_area, max_area, min_floor, max_floor,
                tags, min_lat, max_lat, min_lon, max_lon,
                limit
        );
    }

    @GetMapping("/listings/id/{id}")
    public ListingDetailsDTO getListing(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/listings/clusters")
    public List<ClusterDTO> getClusters(
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double min_lat,
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double max_lat,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double min_lon,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double max_lon,
            @RequestParam(required = false) Integer min_rooms,
            @RequestParam(required = false) Integer max_rooms,
            @RequestParam(required = false) Double min_price,
            @RequestParam(required = false) Double max_price,
            @RequestParam(required = false) String listing_type,
            @RequestParam(required = false) Double min_area,
            @RequestParam(required = false) Double max_area,
            @RequestParam(required = false) Integer min_floor,
            @RequestParam(required = false) Integer max_floor,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) Integer max_clusters
    ) {
        validateRanges(min_price, max_price, min_rooms, max_rooms, min_area, max_area);
        if (min_lat > max_lat || min_lon > max_lon) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid BBox coordinates");
        }
        return service.getClusters(
                min_rooms, max_rooms, min_price, max_price, listing_type,
                min_area, max_area, min_floor, max_floor, tags,
                min_lat, max_lat, min_lon, max_lon, max_clusters);
    }

    private void validateRanges(Double minPrice, Double maxPrice,
                                Integer minRooms, Integer maxRooms,
                                Double minArea, Double maxArea) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_price cannot be greater than max_price");
        if (minRooms != null && maxRooms != null && minRooms > maxRooms)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_rooms cannot be greater than max_rooms");
        if (minArea != null && maxArea != null && minArea > maxArea)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_area cannot be greater than max_area");
    }
}