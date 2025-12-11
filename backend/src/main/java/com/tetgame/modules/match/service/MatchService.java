package com.tetgame.modules.match.service;

import com.tetgame.modules.match.entity.MatchRecord;
import com.tetgame.modules.match.entity.MatchPlayer;

import java.util.List;
import java.util.UUID;

public interface MatchService {
    MatchRecord recordMatch(MatchRecord match, List<MatchPlayer> players);
    List<MatchRecord> getMatchesForUser(UUID userId);
    List<MatchPlayer> getPlayersForMatch(UUID matchId);
    List<Object[]> getTopPlayers(int limit);
}
