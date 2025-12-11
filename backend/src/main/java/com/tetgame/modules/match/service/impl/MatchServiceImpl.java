package com.tetgame.modules.match.service.impl;

import com.tetgame.modules.match.entity.MatchRecord;
import com.tetgame.modules.match.entity.MatchPlayer;
import com.tetgame.modules.match.repository.MatchPlayerRepository;
import com.tetgame.modules.match.repository.MatchRepository;
import com.tetgame.modules.match.service.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Service
public class MatchServiceImpl implements MatchService {
    private final MatchRepository matchRepository;
    private final MatchPlayerRepository matchPlayerRepository;

    private final EntityManager em;

    public MatchServiceImpl(MatchRepository matchRepository, MatchPlayerRepository matchPlayerRepository, EntityManager em) {
        this.matchRepository = matchRepository;
        this.matchPlayerRepository = matchPlayerRepository;
        this.em = em;
    }

    @Override
    @Transactional
    public MatchRecord recordMatch(MatchRecord match, List<MatchPlayer> players) {
        match.setStartedAt(match.getStartedAt() == null ? OffsetDateTime.now() : match.getStartedAt());
        match.setEndedAt(match.getEndedAt() == null ? OffsetDateTime.now() : match.getEndedAt());
        MatchRecord saved = matchRepository.save(match);
        for (MatchPlayer p : players) {
            p.setMatchId(saved.getId());
            matchPlayerRepository.save(p);
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchRecord> getMatchesForUser(UUID userId) {
        // find match ids by player repo then fetch matches
        List<MatchPlayer> players = matchPlayerRepository.findByUserId(userId);
        return matchRepository.findAllById(players.stream().map(MatchPlayer::getMatchId).distinct().toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchPlayer> getPlayersForMatch(UUID matchId) {
        return matchPlayerRepository.findByMatchId(matchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTopPlayers(int limit) {
        // Simple aggregation: top by total score
        Query q = em.createQuery("select mp.userId, sum(mp.score) as total from MatchPlayer mp group by mp.userId order by total desc");
        q.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows;
    }
}
