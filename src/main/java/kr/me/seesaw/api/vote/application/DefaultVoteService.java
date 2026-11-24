package kr.me.seesaw.api.vote.application;

import java.util.NoSuchElementException;

import kr.me.seesaw.api.vote.VoteService;
import kr.me.seesaw.api.vote.dto.VoteSummaryResponse;
import kr.me.seesaw.core.domain.article.ArticleRepository;
import kr.me.seesaw.core.domain.reply.ReplyRepository;
import kr.me.seesaw.core.domain.vote.Vote;
import kr.me.seesaw.core.domain.vote.VoteRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class DefaultVoteService implements VoteService {

    private final PrincipalProvider principalProvider;

    private final VoteRepository voteRepository;

    private final ArticleRepository articleRepository;

    private final ReplyRepository replyRepository;

    @Transactional(readOnly = true)
    @Override
    public VoteSummaryResponse getSummary(String referenceId) {
        validateReference(referenceId);
        String username = getAuthenticatedUsername();
        boolean liked = username != null && voteRepository.findByCreatedByAndReferenceId(username, referenceId)
                .map(Vote::isApproved)
                .orElse(false);
        return toSummary(referenceId, liked);
    }

    @Override
    public VoteSummaryResponse like(String referenceId) {
        validateReference(referenceId);
        String username = requireAuthenticatedUsername();
        voteRepository.findByCreatedByAndReferenceId(username, referenceId).ifPresentOrElse(
                vote -> vote.setApproved(true),
                () -> {
                    Vote vote = new Vote();
                    vote.setCreatedBy(username);
                    vote.setReferenceId(referenceId);
                    vote.setApproved(true);
                    voteRepository.save(vote);
                }
        );
        return toSummary(referenceId, true);
    }

    @Override
    public VoteSummaryResponse unlike(String referenceId) {
        validateReference(referenceId);
        String username = requireAuthenticatedUsername();
        voteRepository.findByCreatedByAndReferenceId(username, referenceId)
                .ifPresent(voteRepository::delete);
        return toSummary(referenceId, false);
    }

    private VoteSummaryResponse toSummary(String referenceId, boolean liked) {
        long count = voteRepository.countByReferenceIdAndApproved(referenceId, true);
        return VoteSummaryResponse.builder()
                .referenceId(referenceId)
                .count(count)
                .liked(liked)
                .build();
    }

    private void validateReference(String referenceId) {
        boolean articleExists = articleRepository.findById(referenceId).isPresent();
        boolean replyExists = replyRepository.findById(referenceId).isPresent();
        if (!articleExists && !replyExists) {
            throw new NoSuchElementException("좋아요 대상을 찾을 수 없습니다: " + referenceId);
        }
    }

    private String requireAuthenticatedUsername() {
        String username = getAuthenticatedUsername();
        if (username == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return username;
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = principalProvider.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

}
