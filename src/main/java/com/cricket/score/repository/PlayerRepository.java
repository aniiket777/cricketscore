package com.cricket.score.repository;

import com.cricket.score.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamId(Long teamId);
    Optional<Player> findByNameIgnoreCase(String name);

    @Query("SELECT p FROM Player p ORDER BY p.totalRuns DESC")
    List<Player> findTopRunScorers();

    @Query("SELECT p FROM Player p ORDER BY p.totalWickets DESC")
    List<Player> findTopWicketTakers();
}
