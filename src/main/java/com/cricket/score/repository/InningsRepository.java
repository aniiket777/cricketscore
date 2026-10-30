package com.cricket.score.repository;

import com.cricket.score.model.Innings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InningsRepository extends JpaRepository<Innings, Long> {
    List<Innings> findByMatchIdOrderByInningsNumberAsc(Long matchId);
    Optional<Innings> findByMatchIdAndInningsNumber(Long matchId, int inningsNumber);
}
