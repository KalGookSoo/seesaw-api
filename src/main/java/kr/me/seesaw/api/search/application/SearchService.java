package kr.me.seesaw.api.search.application;

import kr.me.seesaw.api.search.dto.SearchResponse;

public interface SearchService {

    SearchResponse search(String siteId, String keyword, int page, int limit);
}
