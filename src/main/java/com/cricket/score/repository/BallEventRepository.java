package com.cricket.score.repository;

import com.cricket.score.model.BallEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BallEventRepository extends JpaRepository<BallEvent, Long> {
    List<BallEvent> findByMatchIdOrderByTimestampDesc(Long matchId);
    List<BallEvent> findByMatchIdAndInningsNumberOrderByTimestampDesc(Long matchId, int inningsNumber);
    List<BallEvent> findByMatchIdAndInningsNumberOrderByTimestampAsc(Long matchId, int inningsNumber);
    List<BallEvent> findByMatchIdOrderByTimestampDesc(Long matchId, Pageable pageable);
}
