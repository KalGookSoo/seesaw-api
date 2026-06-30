package kr.me.seesaw.service;

import kr.me.seesaw.request.CreateSiteRequest;
import kr.me.seesaw.domain.Site;
import kr.me.seesaw.response.SiteResponse;

import java.io.IOException;
import java.util.List;

public interface SiteService {

    SiteResponse getSiteById(String id);

    SiteResponse getSiteByDomainName(String domainName);

    List<SiteResponse> getOwnSites(String username);

    SiteResponse createSite(CreateSiteRequest command) throws IOException;

    SiteResponse updateSite(String id, CreateSiteRequest command) throws IOException;

    void deleteSite(String id);

}
