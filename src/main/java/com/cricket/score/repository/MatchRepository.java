package com.cricket.score.repository;

import com.cricket.score.model.Match;
import com.cricket.score.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(MatchStatus status);
    List<Match> findAllByOrderByStartTimeDesc();
}
