package com.cricket.score.service;

import com.cricket.score.model.*;
import com.cricket.score.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class DataInitializerService implements CommandLineRunner {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private InningsRepository inningsRepository;

    @Autowired
    private BatsmanScoreRepository batsmanScoreRepository;

    @Autowired
    private BowlerScoreRepository bowlerScoreRepository;

    @Autowired
    private BallEventRepository ballEventRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (teamRepository.count() > 0) {
            return; // Already initialized
        }

        // 1. Create Teams
        Team ind = teamRepository.save(new Team("India", "IND", "🇮🇳", "#1e3a8a", "#38bdf8"));
        Team aus = teamRepository.save(new Team("Australia", "AUS", "🇦🇺", "#854d0e", "#facc15"));
        Team eng = teamRepository.save(new Team("England", "ENG", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "#991b1b", "#f87171"));
        Team sa  = teamRepository.save(new Team("South Africa", "SA", "🇿🇦", "#166534", "#4ade80"));
        Team csk = teamRepository.save(new Team("Chennai Super Kings", "CSK", "🦁", "#ca8a04", "#fef08a"));
        Team mi  = teamRepository.save(new Team("Mumbai Indians", "MI", "⚡", "#0369a1", "#38bdf8"));

        // 2. Create Players for India
        Player rohit = playerRepository.save(new Player("Rohit Sharma (c)", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", ind));
        Player kohli = playerRepository.save(new Player("Virat Kohli", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm medium", ind));
        Player sky = playerRepository.save(new Player("Suryakumar Yadav", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", ind));
        Player pant = playerRepository.save(new Player("Rishabh Pant (wk)", PlayerRole.WICKET_KEEPER, "Left-hand bat", "None", ind));
        Player hardik = playerRepository.save(new Player("Hardik Pandya", PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm fast-medium", ind));
        Player axar = playerRepository.save(new Player("Axar Patel", PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", ind));
        Player jadeja = playerRepository.save(new Player("Ravindra Jadeja", PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", ind));
        Player bumrah = playerRepository.save(new Player("Jasprit Bumrah", PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", ind));
        Player arshdeep = playerRepository.save(new Player("Arshdeep Singh", PlayerRole.BOWLER, "Left-hand bat", "Left-arm medium-fast", ind));
        Player kuldeep = playerRepository.save(new Player("Kuldeep Yadav", PlayerRole.BOWLER, "Left-hand bat", "Left-arm wrist-spin", ind));
        Player siraj = playerRepository.save(new Player("Mohammed Siraj", PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", ind));

        // Initial career stats for leaderboard
        kohli.setTotalRuns(4188); kohli.setTotalBallsFaced(3040); kohli.setHighestScore(122); playerRepository.save(kohli);
        rohit.setTotalRuns(4231); rohit.setTotalBallsFaced(2980); rohit.setHighestScore(121); playerRepository.save(rohit);
        sky.setTotalRuns(2520); sky.setTotalBallsFaced(1490); sky.setHighestScore(117); playerRepository.save(sky);
        hardik.setTotalRuns(1680); hardik.setTotalBallsFaced(1190); hardik.setTotalWickets(86); playerRepository.save(hardik);
        bumrah.setTotalWickets(89); bumrah.setTotalBallsBowled(1420); bumrah.setTotalRunsConceded(1480); playerRepository.save(bumrah);

        // 3. Create Players for Australia
        Player head = playerRepository.save(new Player("Travis Head", PlayerRole.BATSMAN, "Left-hand bat", "Right-arm offbreak", aus));
        Player warner = playerRepository.save(new Player("David Warner", PlayerRole.BATSMAN, "Left-hand bat", "Right-arm legbreak", aus));
        Player marsh = playerRepository.save(new Player("Mitchell Marsh (c)", PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm medium", aus));
        Player maxwell = playerRepository.save(new Player("Glenn Maxwell", PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm offbreak", aus));
        Player stoinis = playerRepository.save(new Player("Marcus Stoinis", PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm medium", aus));
        Player david = playerRepository.save(new Player("Tim David", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", aus));
        Player wade = playerRepository.save(new Player("Matthew Wade (wk)", PlayerRole.WICKET_KEEPER, "Left-hand bat", "Right-arm medium", aus));
        Player cummins = playerRepository.save(new Player("Pat Cummins", PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", aus));
        Player starc = playerRepository.save(new Player("Mitchell Starc", PlayerRole.BOWLER, "Left-hand bat", "Left-arm fast", aus));
        Player zampa = playerRepository.save(new Player("Adam Zampa", PlayerRole.BOWLER, "Right-hand bat", "Right-arm legbreak", aus));
        Player hazlewood = playerRepository.save(new Player("Josh Hazlewood", PlayerRole.BOWLER, "Left-hand bat", "Right-arm fast-medium", aus));

        head.setTotalRuns(1280); head.setTotalBallsFaced(810); playerRepository.save(head);
        maxwell.setTotalRuns(2600); maxwell.setTotalBallsFaced(1700); maxwell.setTotalWickets(43); playerRepository.save(maxwell);
        zampa.setTotalWickets(111); zampa.setTotalBallsBowled(1800); zampa.setTotalRunsConceded(2150); playerRepository.save(zampa);
        starc.setTotalWickets(79); starc.setTotalBallsBowled(1340); starc.setTotalRunsConceded(1690); playerRepository.save(starc);

        // 4. Create Players for England
        playerRepository.save(new Player("Jos Buttler (c/wk)", PlayerRole.WICKET_KEEPER, "Right-hand bat", "None", eng));
        playerRepository.save(new Player("Phil Salt", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", eng));
        playerRepository.save(new Player("Jonny Bairstow", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm medium", eng));
        playerRepository.save(new Player("Harry Brook", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm medium", eng));
        playerRepository.save(new Player("Moeen Ali", PlayerRole.ALL_ROUNDER, "Left-hand bat", "Right-arm offbreak", eng));
        playerRepository.save(new Player("Liam Livingstone", PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm legbreak", eng));
        playerRepository.save(new Player("Sam Curran", PlayerRole.ALL_ROUNDER, "Left-hand bat", "Left-arm medium-fast", eng));
        playerRepository.save(new Player("Chris Jordan", PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast-medium", eng));
        playerRepository.save(new Player("Jofra Archer", PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", eng));
        playerRepository.save(new Player("Adil Rashid", PlayerRole.BOWLER, "Right-hand bat", "Right-arm legbreak", eng));
        playerRepository.save(new Player("Reece Topley", PlayerRole.BOWLER, "Right-hand bat", "Left-arm fast-medium", eng));

        // 5. Create Players for South Africa
        playerRepository.save(new Player("Quinton de Kock (wk)", PlayerRole.WICKET_KEEPER, "Left-hand bat", "None", sa));
        playerRepository.save(new Player("Reeza Hendricks", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", sa));
        playerRepository.save(new Player("Aiden Markram (c)", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", sa));
        playerRepository.save(new Player("Heinrich Klaasen", PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", sa));
        playerRepository.save(new Player("David Miller", PlayerRole.BATSMAN, "Left-hand bat", "Right-arm offbreak", sa));
        playerRepository.save(new Player("Marco Jansen", PlayerRole.ALL_ROUNDER, "Right-hand bat", "Left-arm fast", sa));
        playerRepository.save(new Player("Keshav Maharaj", PlayerRole.BOWLER, "Right-hand bat", "Slow left-arm orthodox", sa));
        playerRepository.save(new Player("Kagiso Rabada", PlayerRole.BOWLER, "Left-hand bat", "Right-arm fast", sa));
        playerRepository.save(new Player("Anrich Nortje", PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", sa));
        playerRepository.save(new Player("Tabraiz Shamsi", PlayerRole.BOWLER, "Right-hand bat", "Left-arm wrist-spin", sa));
        playerRepository.save(new Player("Ottneil Baartman", PlayerRole.BOWLER, "Right-hand bat", "Right-arm medium-fast", sa));

        // 6. Seed Featured Match 1: INDIA vs AUSTRALIA (LIVE Thriller - ICC T20 World Cup Final)
        seedLiveMatch(ind, aus, rohit, kohli, sky, pant, hardik, bumrah, head, warner, marsh, maxwell, cummins, starc, zampa, hazlewood);

        // 7. Seed Completed Match 2: INDIA vs ENGLAND (Semi-Final)
        seedCompletedMatch(ind, eng, kohli, rohit, bumrah);

        // 8. Seed Upcoming Match 3: CSK vs MI (IPL El Clásico)
        seedUpcomingMatch(csk, mi);
    }

    private void seedLiveMatch(Team ind, Team aus, Player rohit, Player kohli, Player sky, Player pant, Player hardik,
                               Player bumrah, Player head, Player warner, Player marsh, Player maxwell,
                               Player cummins, Player starc, Player zampa, Player hazlewood) {
        Match match = new Match();
        match.setTitle("India vs Australia - ICC T20 World Cup Final");
        match.setSeries("ICC Men's T20 World Cup 2024");
        match.setVenue("Kensington Oval, Bridgetown, Barbados");
        match.setMatchType(MatchType.T20);
        match.setTotalOvers(20);
        match.setStatus(MatchStatus.LIVE);
        match.setTeam1(aus);
        match.setTeam2(ind);
        match.setTossWinner(aus);
        match.setTossDecision(TossDecision.BAT);

        // 1st Innings: AUS 186/6 (20.0 ov)
        match.setTeam1Score(186);
        match.setTeam1Wickets(6);
        match.setTeam1Overs("20.0");
        match.setTargetRuns(187);

        // Currently in 2nd Innings: IND Batting
        match.setCurrentInningsNumber(2);
        match.setBattingTeam(ind);
        match.setBowlingTeam(aus);
        match.setCurrentScore(142);
        match.setCurrentWickets(3);
        match.setCurrentBalls(92); // 15.2 overs
        match.setCurrentOversFormatted("15.2");
        match.setCurrentRunRate(9.26);
        match.setRequiredRunRate(9.64);
        match.setLastBallsSummary("1 4 0 W 6 1");

        match.setCurrentStrikerName(kohli.getName());
        match.setCurrentStrikerRuns(68);
        match.setCurrentStrikerBalls(42);

        match.setCurrentNonStrikerName(hardik.getName());
        match.setCurrentNonStrikerRuns(19);
        match.setCurrentNonStrikerBalls(11);

        match.setCurrentBowlerName(cummins.getName());
        match.setCurrentBowlerFigures("3.2-0-29-1");

        match.setStartTime(LocalDateTime.now().minusHours(2));
        match.setLastUpdated(LocalDateTime.now());

        Match savedMatch = matchRepository.save(match);

        // --- Innings 1 (Australia Batting) ---
        Innings inn1 = new Innings(savedMatch.getId(), 1, aus, ind);
        inn1.setTotalRuns(186);
        inn1.setTotalWickets(6);
        inn1.setTotalBalls(120);
        inn1.setOversFormatted("20.0");
        inn1.setWides(4);
        inn1.setNoBalls(1);
        inn1.setByes(2);
        inn1.setLegByes(3);
        inn1.setCompleted(true);
        inningsRepository.save(inn1);

        // Australia Batsmen
        createBatsman(savedMatch.getId(), 1, head, 1, 76, 43, 8, 4, true, WicketType.CAUGHT, "Bumrah", "Kuldeep");
        createBatsman(savedMatch.getId(), 1, warner, 2, 18, 12, 3, 0, true, WicketType.BOWLED, "Arshdeep", null);
        createBatsman(savedMatch.getId(), 1, marsh, 3, 37, 28, 3, 2, true, WicketType.CAUGHT, "Kuldeep", "Axar");
        createBatsman(savedMatch.getId(), 1, maxwell, 4, 15, 12, 1, 1, true, WicketType.BOWLED, "Bumrah", null);
        createBatsman(savedMatch.getId(), 1, cummins, 5, 24, 15, 2, 1, false, WicketType.NONE, null, null);

        // India Bowlers in Innings 1
        createBowler(savedMatch.getId(), 1, bumrah, 1, 24, "4.0", 0, 21, 2, 5.25);
        createBowler(savedMatch.getId(), 1, cummins, 2, 24, "4.0", 0, 38, 1, 9.50); // placeholder or arshdeep

        // --- Innings 2 (India Batting) ---
        Innings inn2 = new Innings(savedMatch.getId(), 2, ind, aus);
        inn2.setTotalRuns(142);
        inn2.setTotalWickets(3);
        inn2.setTotalBalls(92);
        inn2.setOversFormatted("15.2");
        inn2.setWides(3);
        inn2.setNoBalls(1);
        inn2.setCompleted(false);
        inningsRepository.save(inn2);

        // India Batsmen in Innings 2
        createBatsmanWithStrike(savedMatch.getId(), 2, rohit, 1, 28, 16, 4, 1, true, WicketType.CAUGHT, "Starc", "Head", false, false);
        createBatsmanWithStrike(savedMatch.getId(), 2, pant, 2, 15, 11, 2, 0, true, WicketType.LBW, "Zampa", null, false, false);
        createBatsmanWithStrike(savedMatch.getId(), 2, sky, 3, 12, 10, 1, 1, true, WicketType.CAUGHT, "Cummins", "Maxwell", false, false);
        createBatsmanWithStrike(savedMatch.getId(), 2, kohli, 4, 68, 42, 6, 2, false, WicketType.NONE, null, null, true, false); // On strike
        createBatsmanWithStrike(savedMatch.getId(), 2, hardik, 5, 19, 11, 1, 1, false, WicketType.NONE, null, null, false, true); // Non-striker

        // Australia Bowlers in Innings 2
        createBowlerWithCurrent(savedMatch.getId(), 2, starc, 1, 24, "4.0", 0, 36, 1, 9.00, false);
        createBowlerWithCurrent(savedMatch.getId(), 2, hazlewood, 2, 24, "4.0", 0, 31, 0, 7.75, false);
        createBowlerWithCurrent(savedMatch.getId(), 2, zampa, 3, 24, "4.0", 0, 42, 1, 10.50, false);
        createBowlerWithCurrent(savedMatch.getId(), 2, cummins, 4, 20, "3.2", 0, 29, 1, 8.70, true); // Current bowler

        // Seed realistic commentary events
        seedCommentaryEvents(savedMatch.getId());
    }

    private void seedCommentaryEvents(Long matchId) {
        String[] commentaries = {
                "15.2 | Pat Cummins to Hardik Pandya, 1 run. Pushed gently towards mid-off for a quick single to retain strike for Virat Kohli.",
                "15.1 | Pat Cummins to Virat Kohli, SIX! HIGH, HANDSOME AND INTO THE STANDS! Kohli steps down the wicket and launches Cummins into the second tier over long-on! What a stroke under immense pressure!",
                "14.6 | Adam Zampa to Virat Kohli, WICKET! OUT! Suryakumar Yadav tries to sweep against the spin, gets a top edge and Maxwell at backward point takes an easy catch! IND 135/3.",
                "14.5 | Adam Zampa to Suryakumar Yadav, no run. Beaten by the drift and flight outside off.",
                "14.4 | Adam Zampa to Virat Kohli, FOUR! Classic Kohli! Pierces the cover gap with pinpoint perfection. The outfield is lightning fast!",
                "14.3 | Adam Zampa to Virat Kohli, 1 run. Flicked off the pads towards deep square leg.",
                "14.2 | Adam Zampa to Suryakumar Yadav, 1 run. Driven down to long-on."
        };

        for (int i = 0; i < commentaries.length; i++) {
            BallEvent b = new BallEvent();
            b.setMatchId(matchId);
            b.setInningsNumber(2);
            b.setOverNumber(15);
            b.setBallInOver(6 - i);
            b.setStrikerName("Virat Kohli");
            b.setNonStrikerName("Hardik Pandya");
            b.setBowlerName("Pat Cummins");
            b.setCommentary(commentaries[i]);
            b.setRunsScored(i == 1 ? 6 : (i == 2 ? 0 : 1));
            b.setWicket(i == 2);
            b.setScoreAfterBall(142 - i * 2);
            b.setWicketsAfterBall(i >= 2 ? 3 : 2);
            b.setOversAfterBall("15." + (6 - i));
            b.setTimestamp(LocalDateTime.now().minusMinutes(i));
            ballEventRepository.save(b);
        }
    }

    private void seedCompletedMatch(Team ind, Team eng, Player kohli, Player rohit, Player bumrah) {
        Match match = new Match();
        match.setTitle("India vs England - ICC T20 Semi-Final 2");
        match.setSeries("ICC Men's T20 World Cup 2024");
        match.setVenue("Providence Stadium, Guyana");
        match.setMatchType(MatchType.T20);
        match.setTotalOvers(20);
        match.setStatus(MatchStatus.COMPLETED);
        match.setTeam1(ind);
        match.setTeam2(eng);
        match.setTossWinner(eng);
        match.setTossDecision(TossDecision.BOWL);

        match.setTeam1Score(171);
        match.setTeam1Wickets(7);
        match.setTeam1Overs("20.0");

        match.setTeam2Score(103);
        match.setTeam2Wickets(10);
        match.setTeam2Overs("16.4");

        match.setCurrentInningsNumber(2);
        match.setBattingTeam(eng);
        match.setBowlingTeam(ind);
        match.setCurrentScore(103);
        match.setCurrentWickets(10);
        match.setCurrentBalls(100);
        match.setCurrentOversFormatted("16.4");
        match.setWinnerTeam(ind);
        match.setWinDescription("India won by 68 runs!");
        match.setPlayerOfTheMatch("Rohit Sharma (57 off 39)");
        match.setStartTime(LocalDateTime.now().minusDays(3));
        match.setLastUpdated(LocalDateTime.now().minusDays(3));

        Match savedMatch = matchRepository.save(match);

        // Innings 1
        Innings inn1 = new Innings(savedMatch.getId(), 1, ind, eng);
        inn1.setTotalRuns(171);
        inn1.setTotalWickets(7);
        inn1.setTotalBalls(120);
        inn1.setOversFormatted("20.0");
        inn1.setCompleted(true);
        inningsRepository.save(inn1);

        createBatsman(savedMatch.getId(), 1, rohit, 1, 57, 39, 6, 2, true, WicketType.BOWLED, "Adil Rashid", null);
        createBatsman(savedMatch.getId(), 1, kohli, 2, 9, 9, 0, 1, true, WicketType.BOWLED, "Reece Topley", null);

        // Innings 2
        Innings inn2 = new Innings(savedMatch.getId(), 2, eng, ind);
        inn2.setTotalRuns(103);
        inn2.setTotalWickets(10);
        inn2.setTotalBalls(100);
        inn2.setOversFormatted("16.4");
        inn2.setCompleted(true);
        inningsRepository.save(inn2);

        createBowler(savedMatch.getId(), 2, bumrah, 1, 16, "2.4", 0, 12, 2, 4.50);
    }

    private void seedUpcomingMatch(Team csk, Team mi) {
        Match match = new Match();
        match.setTitle("Chennai Super Kings vs Mumbai Indians");
        match.setSeries("Indian Premier League 2025");
        match.setVenue("Wankhede Stadium, Mumbai");
        match.setMatchType(MatchType.T20);
        match.setTotalOvers(20);
        match.setStatus(MatchStatus.UPCOMING);
        match.setTeam1(mi);
        match.setTeam2(csk);
        match.setStartTime(LocalDateTime.now().plusHours(3));
        match.setLastUpdated(LocalDateTime.now());
        matchRepository.save(match);
    }

    private BatsmanScore createBatsman(Long matchId, int innNum, Player p, int pos, int runs, int balls, int fours, int sixes, boolean out, WicketType wt, String bowler, String fielder) {
        return createBatsmanWithStrike(matchId, innNum, p, pos, runs, balls, fours, sixes, out, wt, bowler, fielder, false, false);
    }

    private BatsmanScore createBatsmanWithStrike(Long matchId, int innNum, Player p, int pos, int runs, int balls, int fours, int sixes, boolean out, WicketType wt, String bowler, String fielder, boolean strike, boolean nonStrike) {
        BatsmanScore b = new BatsmanScore(matchId, innNum, p, pos);
        b.setRuns(runs);
        b.setBalls(balls);
        b.setFours(fours);
        b.setSixes(sixes);
        b.setOut(out);
        b.setDismissalType(wt);
        b.setBowlerName(bowler);
        b.setFielderName(fielder);
        b.setHasBatted(true);
        b.setOnStrike(strike);
        b.setNonStriker(nonStrike);
        b.calculateStrikeRate();
        return batsmanScoreRepository.save(b);
    }

    private BowlerScore createBowler(Long matchId, int innNum, Player p, int pos, int balls, String overs, int maidens, int runs, int wickets, double econ) {
        return createBowlerWithCurrent(matchId, innNum, p, pos, balls, overs, maidens, runs, wickets, econ, false);
    }

    private BowlerScore createBowlerWithCurrent(Long matchId, int innNum, Player p, int pos, int balls, String overs, int maidens, int runs, int wickets, double econ, boolean current) {
        BowlerScore b = new BowlerScore(matchId, innNum, p, pos);
        b.setBallsBowled(balls);
        b.setOversBowled(overs);
        b.setMaidens(maidens);
        b.setRunsConceded(runs);
        b.setWickets(wickets);
        b.setEconomyRate(econ);
        b.setCurrentBowler(current);
        return bowlerScoreRepository.save(b);
    }
}
