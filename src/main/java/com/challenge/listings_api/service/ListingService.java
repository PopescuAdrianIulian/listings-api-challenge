package com.challenge.listings_api.service;

import com.challenge.listings_api.dto.ClusterDTO;
import com.challenge.listings_api.dto.ListingDetailsDTO;
import com.challenge.listings_api.entity.Listing;
import com.challenge.listings_api.repository.ListingRepository;
import com.challenge.listings_api.repository.ListingSpecifications;
import com.challenge.listings_api.repository.ListingSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsabil pentru logica de business a anunțurilor imobiliare.
 * Implementează strategii de optimizare pentru seturi de date masive (1M+ înregistrări),
 * utilizând proiecții JPA și agregări native în baza de date.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListingService {

    private final ListingRepository repository;

    /**
     * Căutare avansată cu filtrare dinamică.
     * OPTIMIZARE: Folosește interfața ListingSummaryProjection pentru a genera un SQL selectiv.
     * Astfel, se evită încărcarea câmpurilor mari (descriere, titlu) în memorie,
     * reducând latența de transfer și consumul de RAM.
     */
    public List<ListingSummaryProjection> searchListings(
            Integer minRooms, Integer maxRooms, Double minPrice, Double maxPrice,
            String type, Double minArea, Double maxArea, Integer minFloor, Integer maxFloor,
            String tags, Double minLat, Double maxLat, Double minLon, Double maxLon,
            int limit) {

        // Construire specificație dinamică bazată pe parametrii primiți
        var spec = ListingSpecifications.filterBy(minRooms, maxRooms, minPrice, maxPrice, type,
                minArea, maxArea, minFloor, maxFloor, tags, minLat, maxLat, minLon, maxLon);

        // Execuție query cu sortare fixă după ID (cerință benchmark) și limitare la nivel de DB
        return repository.findBy(spec, q -> q
                .as(ListingSummaryProjection.class)
                .sortBy(Sort.by("id").ascending())
                .limit(limit)
                .all());
    }

    /**
     * Recuperarea detaliilor complete pentru un anunț specific.
     * Aruncă ResponseStatusException (404) dacă ID-ul nu este valid.
     */
    public ListingDetailsDTO getById(String id) {
        Listing listing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Listing with ID " + id + " not found"));
        return mapToDetails(listing);
    }

    /**
     * Implementare Clustering prin Agregare Nativă (Database-Side).
     * * STRATEGIE: "Pushing logic to the data".
     * În loc să procesăm 1M de puncte în Java (ceea ce ar cauza latențe de 20s+),
     * lăsăm MySQL să calculeze grupurile folosind indexul geografic.
     * * @return O listă de clustere ce conțin coordonatele medii (Centroid) și densitatea (Count).
     */
    public List<ClusterDTO> getClusters(
            Integer minRooms, Integer maxRooms, Double minPrice, Double maxPrice,
            String type, Double minArea, Double maxArea, Integer minFloor, Integer maxFloor,
            String tags, Double minLat, Double maxLat, Double minLon, Double maxLon,
            int maxClusters) {

        // 1. VALIDARE DEFENSIVĂ: Prevenim Full Table Scan.
        // Fără un Bounding Box definit, agregarea pe 1M de rânduri este ineficientă semantic și tehnic.
        if (minLat == null || maxLat == null || minLon == null || maxLon == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parametrii geografici (min_lat, max_lat, min_lon, max_lon) sunt obligatorii pentru clustering.");
        }

        // 2. Definirea granularității grilei.
        // Calculăm o dimensiune de tip rădăcină pătrată pentru a distribui clusterele simetric.
        int gridSize = (int) Math.sqrt(maxClusters);
        if (gridSize < 1) gridSize = 1;

        // 3. Execuție Native Query.
        // Baza de date face munca grea (Filtrare -> Grupare -> Calcul Medii) în sub 300ms.
        var nativeResults = repository.getClustersNative(
                minLat, maxLat, minLon, maxLon,
                type, minRooms, maxRooms, minPrice, maxPrice,
                gridSize, maxClusters
        );

        // 4. Mapare rezultate către DTO-uri pentru prezentare
        return nativeResults.stream()
                .map(p -> new ClusterDTO(p.getLat(), p.getLon(), p.getCount()))
                .collect(Collectors.toList());
    }

    /**
     * Helper pentru transformarea entității JPA în DTO-ul de detalii.
     * Utilizează pattern-ul Builder generat de Lombok.
     */
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