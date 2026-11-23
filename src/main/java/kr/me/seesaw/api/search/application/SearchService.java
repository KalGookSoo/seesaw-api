package kr.me.seesaw.api.search.application;

import kr.me.seesaw.api.search.dto.SearchResponse;

public interface SearchService {

    SearchResponse search(String keyword, int limit);
}
