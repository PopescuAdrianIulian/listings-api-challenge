package com.challenge.listings_api.controller;

import com.challenge.listings_api.dto.ClusterDTO;
import com.challenge.listings_api.dto.ListingDetailsDTO;
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

/**
 * Controllerul gestionează cererile HTTP externe și direcționează fluxul de date.
 */
@RestController
@RequiredArgsConstructor
@Validated // CRITIC: Activează validarea adnotărilor (@Min, @Max, etc.) pe parametrii metodelor.
public class ListingController {

    private final ListingService service;

    /**
     * Endpoint obligatoriu conform cerinței pentru monitorizarea stării aplicației.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    /**
     * Endpoint pentru căutarea filtrată.
     * Returnează o listă de Proiecții (optimizare SQL) în loc de obiecte complete.
     */
    @GetMapping("/listings")
    public List<ListingSummaryProjection> getListings(
            // Validăm input-ul direct în semnătura metodei pentru a asigura integritatea datelor.
            @RequestParam(required = false) @PositiveOrZero Integer min_rooms,
            @RequestParam(required = false) @PositiveOrZero Integer max_rooms,
            @RequestParam(required = false) @PositiveOrZero Double min_price,
            @RequestParam(required = false) @PositiveOrZero Double max_price,
            @RequestParam(required = false) @Pattern(regexp = "sale|rent", message = "listing_type must be 'sale' or 'rent'") String listing_type,
            @RequestParam(required = false) @Positive Double min_area,
            @RequestParam(required = false) @Positive Double max_area,
            @RequestParam(required = false) Integer min_floor,
            @RequestParam(required = false) Integer max_floor,
            @RequestParam(required = false) String tags,
            // Coordonatele GPS sunt validate conform limitelor geografice reale.
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double min_lat,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double max_lat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double min_lon,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double max_lon,
            // Constrângere strictă conform cerinței: limita între 1 și 500 elemente.
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) Integer limit
    ) {
        // 1. Validare Logică: Verificăm cazurile pe care adnotările simple nu le pot prinde (ex: min > max).
        validateMinMaxRanges(min_price, max_price, min_rooms, max_rooms, min_area, max_area);

        // 2. Delegăm execuția către Service Layer.
        return service.searchListings(
                min_rooms, max_rooms, min_price, max_price,
                listing_type, min_area, max_area, min_floor,
                max_floor, tags, min_lat, max_lat, min_lon,
                max_lon, limit
        );
    }

    /**
     * Endpoint pentru vizualizarea detaliată a unui singur anunț.
     */
    @GetMapping("/listings/{id}")
    public ListingDetailsDTO getListing(@PathVariable String id) {
        return service.getById(id);
    }

    /**
     * Metodă privată pentru validarea corelațiilor între parametrii de tip interval.
     * Dacă logica e invalidă, aruncăm 400 Bad Request pentru a informa utilizatorul.
     */
    private void validateMinMaxRanges(Double minPrice, Double maxPrice, Integer minRooms, Integer maxRooms, Double minArea, Double maxArea) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_price cannot be greater than max_price");
        }
        if (minRooms != null && maxRooms != null && minRooms > maxRooms) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_rooms cannot be greater than max_rooms");
        }
        if (minArea != null && maxArea != null && minArea > maxArea) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_area cannot be greater than max_area");
        }
    }

    @GetMapping("/listings/clusters")
    public List<ClusterDTO> getClusters(
            // BBox este obligatoriu pentru clustering
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double min_lat,
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double max_lat,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double min_lon,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double max_lon,

            // Filtre opționale (aceleași ca la search)
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

            // Control cluster
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) Integer max_clusters
    ) {
        // Validăm intervalele
        validateMinMaxRanges(min_price, max_price, min_rooms, max_rooms, min_area, max_area);
        if (min_lat > max_lat || min_lon > max_lon) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid BBox coordinates");
        }

        return service.getClusters(
                min_rooms, max_rooms, min_price, max_price, listing_type,
                min_area, max_area, min_floor, max_floor, tags,
                min_lat, max_lat, min_lon, max_lon, max_clusters
        );
    }
}