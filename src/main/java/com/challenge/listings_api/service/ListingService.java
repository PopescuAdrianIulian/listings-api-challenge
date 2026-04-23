package com.challenge.listings_api.service;

import com.challenge.listings_api.dto.ClusterDTO;
import com.challenge.listings_api.dto.ListingDetailsDTO;
import com.challenge.listings_api.dto.PagedResponse;
import com.challenge.listings_api.entity.Listing;
import com.challenge.listings_api.exception.ResourceNotFoundException;
import com.challenge.listings_api.repository.ListingRepository;
import com.challenge.listings_api.repository.ListingSpecifications;
import com.challenge.listings_api.repository.ListingSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class ListingService {



    private final ListingRepository repository;

    public PagedResponse<ListingSummaryProjection> searchListings(
            String cursor,
            Integer minRooms, Integer maxRooms, Double minPrice, Double maxPrice,
            String type, Double minArea, Double maxArea,
            Integer minFloor, Integer maxFloor, String tags,
            Double minLat, Double maxLat, Double minLon, Double maxLon,
            int limit) {

        String effectiveCursor = (cursor != null) ? cursor : "";

        List<ListingSummaryProjection> results;

        if (tags == null || tags.isBlank()) {
            results = repository.searchAfterCursor(
                    effectiveCursor, minLat, maxLat, minLon, maxLon,
                    minPrice, maxPrice, minRooms, maxRooms, type, limit);
        } else {
            var spec = ListingSpecifications.filterBy(
                            minRooms, maxRooms, minPrice, maxPrice,
                            type, minArea, maxArea, minFloor, maxFloor,
                            tags, minLat, maxLat, minLon, maxLon)
                    .and((root, q, cb) -> {
                        Object cursorVal = effectiveCursor.isEmpty() ? 0L : Long.parseLong(effectiveCursor);
                        return cb.greaterThan(root.get("id"), (Comparable) cursorVal);
                    });

            results = repository.findBy(spec, q -> q
                    .as(ListingSummaryProjection.class)
                    .sortBy(Sort.by("id").ascending())
                    .limit(limit)
                    .all());
        }

        String nextCursor = results.isEmpty() ? null
                : results.get(results.size() - 1).getId();
        boolean hasMore = results.size() == limit;

        return new PagedResponse<>(results, nextCursor, hasMore);
    }

    @Cacheable(value = "listingDetails", key = "#id")
    public ListingDetailsDTO getById(String id) {
        Listing listing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing with ID " + id + " not found"));
        return mapToDetails(listing);
    }

    @Cacheable(
            value = "clusters",
            key = "T(java.util.Objects).hash(#minLat,#maxLat,#minLon,#maxLon,#minRooms,#maxRooms,#minPrice,#maxPrice,#type,#maxClusters," +
                    "#minArea,#maxArea,#minFloor,#maxFloor,#tags)"
    )
    public List<ClusterDTO> getClusters(
            Integer minRooms, Integer maxRooms, Double minPrice, Double maxPrice,
            String type, Double minArea, Double maxArea, Integer minFloor, Integer maxFloor,
            String tags, Double minLat, Double maxLat, Double minLon, Double maxLon,
            int maxClusters) {

        if (minLat == null || maxLat == null || minLon == null || maxLon == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parametrii geografici (min_lat, max_lat, min_lon, max_lon) sunt obligatorii pentru clustering.");
        }

        int gridSize = Math.max(1, (int) Math.sqrt(maxClusters));

        return repository.getClustersNative(
                        minLat, maxLat, minLon, maxLon,
                        type, minRooms, maxRooms, minPrice, maxPrice,
                        gridSize, maxClusters)
                .stream()
                .map(p -> new ClusterDTO(p.getLat(), p.getLon(), p.getCount()))
                .collect(Collectors.toList());
    }

    public void validateRanges(Double minPrice, Double maxPrice,
                                Integer minRooms, Integer maxRooms,
                                Double minArea, Double maxArea) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_price cannot be greater than max_price");
        if (minRooms != null && maxRooms != null && minRooms > maxRooms)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_rooms cannot be greater than max_rooms");
        if (minArea != null && maxArea != null && minArea > maxArea)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min_area cannot be greater than max_area");
    }

    private ListingDetailsDTO mapToDetails(Listing l) {
        return ListingDetailsDTO.builder()
                .id(l.getId())
                .title(l.getTitle())
                .description(l.getDescription())
                .rooms(l.getRooms())
                .areaSqm(l.getAreaSqm())
                .price(l.getPrice())
                .listingType(l.getListingType())
                .tags(l.getTags())
                .lat(l.getLat())
                .lon(l.getLon())
                .floor(l.getFloor())
                .build();
    }
}