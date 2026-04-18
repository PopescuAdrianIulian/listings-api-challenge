package com.challenge.listings_api.repository;

import com.challenge.listings_api.entity.Listing;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ListingSpecifications {

    public static Specification<Listing> filterBy(
            Integer minRooms, Integer maxRooms,
            Double minPrice, Double maxPrice,
            String type,
            Double minArea, Double maxArea,
            Integer minFloor, Integer maxFloor,
            String tags,
            Double minLat, Double maxLat,
            Double minLon, Double maxLon
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minRooms != null) predicates.add(cb.greaterThanOrEqualTo(root.get("rooms"), minRooms));
            if (maxRooms != null) predicates.add(cb.lessThanOrEqualTo(root.get("rooms"), maxRooms));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (type     != null) predicates.add(cb.equal(root.get("listingType"), type));
            if (minArea  != null) predicates.add(cb.greaterThanOrEqualTo(root.get("areaSqm"), minArea));
            if (maxArea  != null) predicates.add(cb.lessThanOrEqualTo(root.get("areaSqm"), maxArea));
            if (minFloor != null) predicates.add(cb.greaterThanOrEqualTo(root.get("floor"), minFloor));
            if (maxFloor != null) predicates.add(cb.lessThanOrEqualTo(root.get("floor"), maxFloor));
            if (minLat   != null) predicates.add(cb.greaterThanOrEqualTo(root.get("lat"), minLat));
            if (maxLat   != null) predicates.add(cb.lessThanOrEqualTo(root.get("lat"), maxLat));
            if (minLon   != null) predicates.add(cb.greaterThanOrEqualTo(root.get("lon"), minLon));
            if (maxLon   != null) predicates.add(cb.lessThanOrEqualTo(root.get("lon"), maxLon));

            if (tags != null && !tags.trim().isEmpty()) {
                for (String tag : tags.split(",")) {
                    String t = tag.trim();
                    if (!t.isEmpty()) {
                        predicates.add(cb.like(root.get("tags"), "%\"" + t + "\"%"));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}