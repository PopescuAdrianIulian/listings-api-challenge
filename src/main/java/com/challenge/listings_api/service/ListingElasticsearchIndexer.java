package com.challenge.listings_api.service;

import com.challenge.listings_api.entity.ListingDocument;
import com.challenge.listings_api.event.ListingIndexEvent;
import com.challenge.listings_api.repository.ListingElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingElasticsearchIndexer {

    private final ListingElasticsearchRepository esRepository;

    @EventListener
    @Async
    public void onListingCreated(ListingIndexEvent event) {
        try {
            ListingDocument doc = ListingDocument.fromListing(event.getListing());
            esRepository.save(doc);
            log.info("Indexed listing: {}", event.getListing().getId());
        } catch (Exception e) {
            log.error("Error indexing listing: {}", e.getMessage());
        }
    }
}