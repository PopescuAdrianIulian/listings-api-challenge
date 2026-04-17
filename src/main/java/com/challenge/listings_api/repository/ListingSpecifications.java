package com.challenge.listings_api.repository;

import com.challenge.listings_api.entity.Listing;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Această clasă folosește JPA Criteria API pentru a construi interogări SQL dinamice.
 * Este soluția ideală atunci când avem mulți parametri de filtrare opționali.
 */
public class ListingSpecifications {

    public static Specification<Listing> filterBy(
            Integer minRooms, Integer maxRooms,
            Double minPrice, Double maxPrice,
            String type,
            Double minArea, Double maxArea,
            Integer minFloor, Integer maxFloor,
            String tags,
            Double minLat, Double maxLat, Double minLon, Double maxLon
    ) {
        // Lambda (root, query, cb) reprezintă:
        // root -> tabelul din DB (Listing)
        // query -> interogarea în sine
        // cb (CriteriaBuilder) -> utilitarul cu care construim condiții (WHERE, AND, etc.)
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- FILTRE DE BAZĂ ---
            // Folosim predicates.add() doar dacă parametrul nu este null.
            if (minRooms != null) predicates.add(cb.greaterThanOrEqualTo(root.get("rooms"), minRooms));
            if (maxRooms != null) predicates.add(cb.lessThanOrEqualTo(root.get("rooms"), maxRooms));

            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));

            if (type != null) predicates.add(cb.equal(root.get("listingType"), type));

            if (minArea != null) predicates.add(cb.greaterThanOrEqualTo(root.get("areaSqm"), minArea));
            if (maxArea != null) predicates.add(cb.lessThanOrEqualTo(root.get("areaSqm"), maxArea));

            if (minFloor != null) predicates.add(cb.greaterThanOrEqualTo(root.get("floor"), minFloor));
            if (maxFloor != null) predicates.add(cb.lessThanOrEqualTo(root.get("floor"), maxFloor));

            // --- BOUNDING BOX (GEOLOCAȚIE) ---
            // Verificăm dacă proprietatea se află în interiorul coordonatelor geografice primite.
            if (minLat != null) predicates.add(cb.greaterThanOrEqualTo(root.get("lat"), minLat));
            if (maxLat != null) predicates.add(cb.lessThanOrEqualTo(root.get("lat"), maxLat));
            if (minLon != null) predicates.add(cb.greaterThanOrEqualTo(root.get("lon"), minLon));
            if (maxLon != null) predicates.add(cb.lessThanOrEqualTo(root.get("lon"), maxLon));

            // --- FILTRARE DUPĂ TAG-URI (LOGICĂ "AND") ---
            // Cerința spune că dacă primim "pool,garden", anunțul trebuie să le aibă pe AMBELE.
            if (tags != null && !tags.trim().isEmpty()) {
                // Split-uim string-ul de la virgule pentru a procesa fiecare tag individual.
                for (String tag : tags.split(",")) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        // Deoarece coloana "tags" stochează un string JSON (ex: ["pool","garden"]),
                        // căutăm tag-ul între ghilimele pentru a evita potriviri parțiale (ex: "art" în "garden").
                        predicates.add(cb.like(root.get("tags"), "%\"" + trimmedTag + "\"%"));
                    }
                }
            }

            // La final, combinăm toate predicatele colectate folosind operatorul logic AND.
            // Dacă lista e goală, va returna pur și simplu "SELECT * FROM listings".
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}