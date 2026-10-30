package com.cricket.score.repository;

import com.cricket.score.model.BatsmanScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatsmanScoreRepository extends JpaRepository<BatsmanScore, Long> {
    List<BatsmanScore> findByMatchIdAndInningsNumberOrderByBattingPositionAsc(Long matchId, int inningsNumber);
    Optional<BatsmanScore> findByMatchIdAndInningsNumberAndPlayerId(Long matchId, int inningsNumber, Long playerId);
    Optional<BatsmanScore> findByMatchIdAndInningsNumberAndIsOnStrikeTrue(Long matchId, int inningsNumber);
    Optional<BatsmanScore> findByMatchIdAndInningsNumberAndIsNonStrikerTrue(Long matchId, int inningsNumber);
}
