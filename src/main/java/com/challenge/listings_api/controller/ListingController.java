package com.challenge.listings_api.controller;

import com.challenge.listings_api.dto.ClusterDTO;
import com.challenge.listings_api.dto.ListingDetailsDTO;
import com.challenge.listings_api.dto.PagedResponse;
import com.challenge.listings_api.repository.ListingSummaryProjection;
import com.challenge.listings_api.service.ListingService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * Endpoint pentru monitorizare. Returnează 200 OK și un status JSON.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /**
     * Endpoint paginat pentru listări.
     * Body-ul este o listă simplă,
     * iar metadatele de paginare sunt trimise în Headers.
     */
    @GetMapping("/listings")
    public ResponseEntity<List<ListingSummaryProjection>> getListings(
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

        service.validateRanges(min_price, max_price, min_rooms, max_rooms, min_area, max_area);

        PagedResponse<ListingSummaryProjection> pagedResponse = service.searchListings(after,
                min_rooms, max_rooms, min_price, max_price,
                listing_type, min_area, max_area, min_floor, max_floor,
                tags, min_lat, max_lat, min_lon, max_lon,
                limit
        );

        return ResponseEntity.ok()
                .header("X-Next-Cursor", pagedResponse.getNextCursor())
                .header("X-Has-More", String.valueOf(pagedResponse.isHasMore()))
                .body(pagedResponse.getData());
    }

    /**
     * Returnează detaliile complete ale unui anunț.
     */
    @GetMapping("/listings/id/{id}")
    public ResponseEntity<ListingDetailsDTO> getListingById(@PathVariable String id) {
        ListingDetailsDTO details = service.getById(id);
        return ResponseEntity.ok(details);
    }

    /**
     * Returnează clusterele pentru vizualizarea pe hartă.
     */
    @GetMapping("/listings/clusters")
    public ResponseEntity<List<ClusterDTO>> getClusters(
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
            @RequestParam(defaultValue = "50") @Min(1) @Max(500) Integer max_clusters
    ) {
        service.validateRanges(min_price, max_price, min_rooms, max_rooms, min_area, max_area);

        if (min_lat > max_lat || min_lon > max_lon) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid BBox coordinates");
        }

        List<ClusterDTO> clusters = service.getClusters(
                min_rooms, max_rooms, min_price, max_price, listing_type,
                min_area, max_area, min_floor, max_floor, tags,
                min_lat, max_lat, min_lon, max_lon, max_clusters);

        return ResponseEntity.ok(clusters);
    }
}