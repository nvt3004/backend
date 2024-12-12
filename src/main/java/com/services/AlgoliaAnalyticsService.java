package com.services;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algolia.api.AnalyticsClient;
import com.algolia.model.analytics.DailySearchesNoResults;
import com.algolia.model.analytics.GetNoResultsRateResponse;
import com.algolia.model.analytics.GetSearchesNoResultsResponse;
import com.algolia.model.analytics.GetTopFilterAttribute;
import com.algolia.model.analytics.GetTopFilterAttributesResponse;
import com.algolia.model.analytics.GetTopFiltersNoResultsResponse;
import com.algolia.model.analytics.GetTopFiltersNoResultsValues;
import com.algolia.model.analytics.TopHit;
import com.algolia.model.analytics.TopHitsResponse;
import com.algolia.model.analytics.TopSearch;
import com.algolia.model.analytics.TopSearchesResponse;
import com.utils.IdApikeyAIgolia;

@Service
public class AlgoliaAnalyticsService {

	private static final Logger logger = Logger.getLogger(AlgoliaAnalyticsService.class.getName());
	private AnalyticsClient client;
	private String region = "us";
	// "us" - United States
	// "de" - Germany

	@Autowired
	public AlgoliaAnalyticsService(IdApikeyAIgolia idApikeyAIgolia) {
		// Khởi tạo SearchClient với SearchConfig
		client = new AnalyticsClient(idApikeyAIgolia.getApplicationId(), idApikeyAIgolia.getAdminApiKey(), region);
	}

	public List<TopSearch> getTopSearches() {
		TopSearchesResponse response = (TopSearchesResponse) client.getTopSearches("products");
		return response.getSearches();
	}

	public List<TopHit> getTopHits() {
		TopHitsResponse response = (TopHitsResponse) client.getTopHits("products");
		return response.getHits();
	}

	public GetNoResultsRateResponse getNoResultsRate() {
		GetNoResultsRateResponse response = client.getNoResultsRate("products");
		return response;
	}

	public List<DailySearchesNoResults> getSearchesNoResults() {
		GetSearchesNoResultsResponse response = client.getSearchesNoResults("products");
		return response.getSearches();
	}

	public List<GetTopFilterAttribute> getTopFilterAttributes() {
		GetTopFilterAttributesResponse response = client.getTopFilterAttributes("products");
		return response.getAttributes();
	}

	public List<GetTopFiltersNoResultsValues> getTopFiltersNoResults() {
		GetTopFiltersNoResultsResponse response = client.getTopFiltersNoResults("products");
		return response.getValues();
	}
}
