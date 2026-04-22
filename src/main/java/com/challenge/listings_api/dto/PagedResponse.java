package com.challenge.listings_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private String  nextCursor;
    private boolean hasMore;
}