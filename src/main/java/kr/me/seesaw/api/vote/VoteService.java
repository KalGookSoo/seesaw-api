package kr.me.seesaw.api.vote;

import kr.me.seesaw.api.vote.dto.VoteSummaryResponse;

public interface VoteService {

    VoteSummaryResponse getSummary(String referenceId);

    VoteSummaryResponse like(String referenceId);

    VoteSummaryResponse unlike(String referenceId);

}
