package com.tetgame.modules.match.controller;

import com.tetgame.modules.match.entity.MatchRecord;
import com.tetgame.modules.match.entity.MatchPlayer;
import com.tetgame.modules.match.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("")
    public MatchRecord recordMatch(@RequestBody MatchRecord match, @RequestBody List<MatchPlayer> players) {
        return matchService.recordMatch(match, players);
    }

    @GetMapping("/me")
    public List<MatchRecord> myMatches(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return matchService.getMatchesForUser(userId);
    }

    @GetMapping("/{matchId}/players")
    public List<MatchPlayer> players(@PathVariable UUID matchId) {
        return matchService.getPlayersForMatch(matchId);
    }

    @GetMapping("/leaderboard/top")
    public List<Object[]> top(@RequestParam(defaultValue = "10") int n) {
        return matchService.getTopPlayers(n);
    }
}
