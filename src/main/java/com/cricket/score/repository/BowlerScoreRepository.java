package com.cricket.score.repository;

import com.cricket.score.model.BowlerScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BowlerScoreRepository extends JpaRepository<BowlerScore, Long> {
    List<BowlerScore> findByMatchIdAndInningsNumberOrderByBowlingPositionAsc(Long matchId, int inningsNumber);
    Optional<BowlerScore> findByMatchIdAndInningsNumberAndPlayerId(Long matchId, int inningsNumber, Long playerId);
    Optional<BowlerScore> findByMatchIdAndInningsNumberAndIsCurrentBowlerTrue(Long matchId, int inningsNumber);
}
