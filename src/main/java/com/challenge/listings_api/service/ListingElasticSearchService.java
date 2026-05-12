package com.challenge.listings_api.service;

import com.challenge.listings_api.dto.ListingDetailsDTO;
import com.challenge.listings_api.entity.ListingDocument;
import com.challenge.listings_api.repository.ListingElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListingElasticSearchService {

    private final ListingElasticsearchRepository elasticsearchRepository;

    public List<ListingDetailsDTO> findByTitle(String title) {
        log.info("Interogare Elasticsearch pentru titlul: {}", title);
        return elasticsearchRepository.findByTitleContains(title)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ListingDetailsDTO> findByListingType(String listingType) {
        log.info("Interogare Elasticsearch pentru tipul: {}", listingType);
        return elasticsearchRepository.findByListingType(listingType)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ListingDetailsDTO> findByPriceBetween(Double minPrice, Double maxPrice) {
        log.info("Interogare Elasticsearch pentru preț între {} și {}", minPrice, maxPrice);
        return elasticsearchRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ListingDetailsDTO> findByTags(String tag) {
        log.info("Interogare Elasticsearch cu tag {}", tag);
        return elasticsearchRepository.findByTags(tag)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertește entitatea de Elasticsearch în DTO-ul pentru API.
     */
    private ListingDetailsDTO mapToDTO(ListingDocument doc) {
        Double lat = null;
        Double lon = null;

        if (doc.getLocation() != null && doc.getLocation().contains(",")) {
            try {
                String[] parts = doc.getLocation().split(",");
                lat = Double.parseDouble(parts[0]);
                lon = Double.parseDouble(parts[1]);
            } catch (Exception e) {
                log.error("Eroare la parsarea locației pentru documentul {}", doc.getId());
            }
        }

        return ListingDetailsDTO.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .rooms(doc.getRooms())
                .areaSqm(doc.getAreaSqm())
                .price(doc.getPrice())
                .listingType(doc.getListingType())
                .tags(doc.getTags())
                .lat(lat)
                .lon(lon)
                .floor(doc.getFloor())
                .build();
    }
}