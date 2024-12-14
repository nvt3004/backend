package com.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algolia.model.analytics.DailySearchesNoResults;
import com.algolia.model.analytics.GetNoResultsRateResponse;
import com.algolia.model.analytics.GetTopFilterAttribute;
import com.algolia.model.analytics.GetTopFiltersNoResultsValues;
import com.algolia.model.analytics.TopHit;
import com.algolia.model.analytics.TopSearch;
import com.errors.ResponseAPI;
import com.services.AlgoliaAnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AlgoliaAnalyticsController {

    @Autowired
    private AlgoliaAnalyticsService algoliaAnalyticsService;

    // API lấy danh sách top tìm kiếm
    @GetMapping("/top-searches")
    public ResponseEntity<ResponseAPI<List<TopSearch>>> getTopSearches() {
        ResponseAPI<List<TopSearch>> response = new ResponseAPI<>();
        try {
            List<TopSearch> topSearches = algoliaAnalyticsService.getTopSearches();
            response.setCode(200);
            response.setData(topSearches);
            response.setMessage("Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // API lấy danh sách tìm kiếm không có kết quả
    @GetMapping("/searches-no-results")
    public ResponseEntity<ResponseAPI<List<DailySearchesNoResults>>> getSearchesNoResults() {
        ResponseAPI<List<DailySearchesNoResults>> response = new ResponseAPI<>();
        try {
            List<DailySearchesNoResults> noResults = algoliaAnalyticsService.getSearchesNoResults();
            response.setCode(200);
            response.setData(noResults);
            response.setMessage("Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // API lấy danh sách kết quả tìm kiếm hàng đầu
    @GetMapping("/top-hits")
    public ResponseEntity<ResponseAPI<List<TopHit>>> getTopHits() {
        ResponseAPI<List<TopHit>> response = new ResponseAPI<>();
        try {
            List<TopHit> topHits = algoliaAnalyticsService.getTopHits();
            response.setCode(200);
            response.setData(topHits);
            response.setMessage("Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // API lấy tỷ lệ không có kết quả
    @GetMapping("/no-results-rate")
    public ResponseEntity<ResponseAPI<GetNoResultsRateResponse>> getNoResultsRate() {
        ResponseAPI<GetNoResultsRateResponse> response = new ResponseAPI<>();
        try {
            GetNoResultsRateResponse rateResponse = algoliaAnalyticsService.getNoResultsRate();
            response.setCode(200);
            response.setData(rateResponse);
            response.setMessage("Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // API lấy top các thuộc tính filter
    @GetMapping("/top-filter-attributes")
    public ResponseEntity<ResponseAPI<List<GetTopFilterAttribute>>> getTopFilterAttributes() {
        ResponseAPI<List<GetTopFilterAttribute>> response = new ResponseAPI<>();
        try {
            List<GetTopFilterAttribute> attributes = algoliaAnalyticsService.getTopFilterAttributes();
            response.setCode(200);
            response.setData(attributes);
            response.setMessage("Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // API lấy top filter không có kết quả
    @GetMapping("/top-filters-no-results")
    public ResponseEntity<ResponseAPI<List<GetTopFiltersNoResultsValues>>> getTopFiltersNoResults() {
        ResponseAPI<List<GetTopFiltersNoResultsValues>> response = new ResponseAPI<>();
        try {
            List<GetTopFiltersNoResultsValues> noResultsFilters = algoliaAnalyticsService.getTopFiltersNoResults();
            response.setCode(200);
            response.setData(noResultsFilters);
            response.setMessage("Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("Internal Server Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
