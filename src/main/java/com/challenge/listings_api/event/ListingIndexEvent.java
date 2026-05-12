package com.challenge.listings_api.event;

import com.challenge.listings_api.entity.Listing;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ListingIndexEvent extends ApplicationEvent {
    private final Listing listing;

    public ListingIndexEvent(Object source, Listing listing) {
        super(source);
        this.listing = listing;
    }
}