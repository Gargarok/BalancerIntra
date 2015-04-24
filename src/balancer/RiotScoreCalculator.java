package balancer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import exception.PlayerNameException;

public class RiotScoreCalculator {  
  // Configuration constants
  // Recent games stats multipliers
  private final static double RECENT_KILL_RATIO = 1.0;
  private final static double RECENT_ASSIST_RATIO = 0.33;
  private final static double RECENT_DEATH_RATIO = -1.0;
  private final static double RECENT_DAMAGE_RATIO = 0.0001;
  private final static double RECENT_MINION_RATIO = 0.02;
  private final static double RECENT_NEXUS_RATIO = 5.0;
  private final static double RECENT_CC_RATIO = 0.01;
  private final static double RECENT_WARDS_RATIO = 0.7;
  private final static double RECENT_HEAL_RATIO = 0.0001;
  private final static double RECENT_WIN_RATIO = 2.0;
  private final static double RECENT_MULTIPLIER = 0.25;
  
  // Total stats multipliers
  private final static double TOTAL_NORMAL_KILL_RATIO = 0.5;
  private final static double TOTAL_NORMAL_ASSIST_RATIO = 0.25;
  private final static double TOTAL_NORMAL_MINION_RATIO = 0.025;
  private final static double TOTAL_NORMAL_NEUTRAL_MINION_RATIO = 0.05;
  private final static double TOTAL_NORMAL_TURRET_RATIO = 2.0;
  // (let's keep in mind that the normal score will be all the previous ratios multiplied with)
  // their associated value, DIVIDED by (the win number * the win ratio)
  // So, the higher this ratio is, the lower the normal score will be (this is done to "simulate" the
  // number of losses)
  private final static double TOTAL_NORMAL_WIN_RATIO = 2.0;   
  
  private final static double TOTAL_RANKED_MIN_GAMES_COUNT = 50.0;
  private final static double TOTAL_RANKED_DEFAULT_RATIO = 0.002;
  
  private final static double TOTAL_RANKED_DAMAGE_TAKEN_RATIO = 0.00001;
  private final static double TOTAL_RANKED_DAMAGE_DONE_RATIO = 0.00001;
  private final static double TOTAL_RANKED_HEAL_RATIO = 0.00002;
  private final static double TOTAL_RANKED_MINION_RATIO = 0.00025;
  private final static double TOTAL_RANKED_NEUTRAL_MINION_RATIO = 0.0005;
  private final static double TOTAL_RANKED_KILL_RATIO = 0.3;
  private final static double TOTAL_RANKED_DEATH_RATIO = -0.3;
  private final static double TOTAL_RANKED_ASSIST_RATIO = 0.9;
  private final static double TOTAL_RANKED_PENTA_RATIO = 0.5;
  private final static double TOTAL_RANKED_TURRET_RATIO = 0.2;
  private final static double TOTAL_RANKED_WIN_DIFFERENCE_RATIO = 1000.0;
  private final static double TOTAL_RANKED_MULTIPLIER = 1.0;
  
  private final static double TOTAL_EXP_GAMES = 50.0;
  private final static double TOTAL_EXP_KILLS = 5.0;
  private final static double TOTAL_EXP_ASSISTS = 3.0;
  private final static double TOTAL_EXP_MINIONS = 0.166;
  private final static double TOTAL_EXP_NEUTRAL_MINIONS = 1.0;
  private final static double TOTAL_EXP_TURRETS = 0.5;
  private final static double TOTAL_EXP_DAMAGE_TAKEN = 0.0;
  private final static double TOTAL_EXP_DAMAGE_DONE = 0.0;
  private final static double TOTAL_EXP_HEAL = 0.0;
  private final static double TOTAL_EXP_PENTA = 0.0;
  private final static double TOTAL_EXP_MULTIPLIER = 0.00002;
  
  private final static double PERF_SCORE_MULTIPLIER = 1.5;
  
  // private HashMap<String,String> riotNames;
  private HashMap<String,Long> riotIds;
  private RiotRequestSender requestSender;
  
  private HashMap<String,Double> recentStats;
  private HashMap<String,Double> totalStats;
  
  public RiotScoreCalculator(RiotRequestSender sender) {
    //this.riotNames = names;
    this.requestSender = sender;
    this.recentStats = new HashMap<>();
    this.totalStats = new HashMap<>();
    this.riotIds = new HashMap<>();
  }
  
  public HashMap<String,Double> getRecentStats() {
    return this.recentStats;
  }
  
  public HashMap<String,Double> getTotalStats() {
    return this.totalStats;
  } 
  
  /**
  * This method goes through the players field (array of PlayerGroup which is given in constructor)
  * and gives to each group its corresponding riot score, after 
  * querying the riot server. Returns the resulting PlayerGroup array.
  * @throws IOException 
  */
  public PlayerGroup[] associateRiotScore(PlayerGroup[] players) throws IOException, PlayerNameException {
    HashSet<String> tmpNames = new HashSet<>();
    for(PlayerGroup pg : players) {
      for (String n : pg.getNames()) {
        tmpNames.add(n);
      }
    }
    String[] idParam = new String[tmpNames.size()];
    tmpNames.toArray(idParam);
    
    this.riotIds = requestSender.getSummonerId(idParam);
    //this.riotIds.put("Barbie", (long)(29691434));
    //this.riotIds.put("Vico", (long)(21847350));
    //this.riotIds.put("Anjali", (long)(33857021));
    
    // Name conservation : we would like to keep the original names entered, instead of the 
    // Riot-format names
    for(PlayerGroup pg : players) {
      for (String n : pg.getNames()) {
        if (!n.equals(n.toLowerCase()) && this.riotIds.containsKey(n.toLowerCase())) {
          this.riotIds.put(n,this.riotIds.get(n.toLowerCase()));
          this.riotIds.remove(n.toLowerCase());
        }
      }
    }
    
    // Here we have an id association for every name in our groups.
    // From there we can actually send the statistic requests to riot,
    // so we can calculate the performance score for every group.
    for (PlayerGroup group : players) {
      double perfScore = 0;
      
      long id = 0; 
      for (String name : group.getPlayers().keySet()) {
        if (riotIds.containsKey(name)) {
          id = riotIds.get(name);
        } else {
          throw new PlayerNameException();
        }
        recentStats = requestSender.getRecentMatches(id);
        totalStats = requestSender.getTotalStats(id);
        
        double recentScore = recentStats.get(RiotRequestSender.RECENT_KILLS) * RECENT_KILL_RATIO
          + recentStats.get(RiotRequestSender.RECENT_ASSISTS) * RECENT_ASSIST_RATIO
          + recentStats.get(RiotRequestSender.RECENT_DEATHS) * RECENT_DEATH_RATIO
          + recentStats.get(RiotRequestSender.RECENT_MINIONS) * RECENT_MINION_RATIO
          + recentStats.get(RiotRequestSender.RECENT_NEXUSKILL) * RECENT_NEXUS_RATIO
          + recentStats.get(RiotRequestSender.RECENT_DAMAGE) * RECENT_DAMAGE_RATIO
          + recentStats.get(RiotRequestSender.RECENT_CC) * RECENT_CC_RATIO
          + recentStats.get(RiotRequestSender.RECENT_WARDPLACED) * RECENT_WARDS_RATIO
          + recentStats.get(RiotRequestSender.RECENT_WIN) * RECENT_WIN_RATIO
          + recentStats.get(RiotRequestSender.RECENT_HEAL) * RECENT_HEAL_RATIO;
        recentScore /= recentStats.get(RiotRequestSender.RECENT_GAMENB);  // Result is being divided by the number of games
        recentScore *= RECENT_MULTIPLIER;
        
        /*
        double normalOverallScore = totalStats.get(RiotRequestSender.TOTAL_NORMAL_KILLS) * TOTAL_NORMAL_KILL_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_NORMAL_ASSISTS) * TOTAL_NORMAL_ASSIST_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_NORMAL_MINIONS) * TOTAL_NORMAL_MINION_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_NORMAL_TURRETS) * TOTAL_NORMAL_TURRET_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_NORMAL_NEUTRAL_MINIONS) * TOTAL_NORMAL_NEUTRAL_MINION_RATIO;
        normalOverallScore /= (totalStats.get(RiotRequestSender.TOTAL_NORMAL_WINS) * TOTAL_NORMAL_WIN_RATIO);
        */
        
        double experienceScore = (totalStats.get(RiotRequestSender.TOTAL_RANKED_ASSISTS) + totalStats.get(RiotRequestSender.TOTAL_NORMAL_ASSISTS)) * TOTAL_EXP_ASSISTS
          + (totalStats.get(RiotRequestSender.TOTAL_RANKED_LOSSES) + totalStats.get(RiotRequestSender.TOTAL_RANKED_WON) + totalStats.get(RiotRequestSender.TOTAL_NORMAL_WINS)) * TOTAL_EXP_GAMES
          + (totalStats.get(RiotRequestSender.TOTAL_RANKED_KILLS) + totalStats.get(RiotRequestSender.TOTAL_NORMAL_KILLS)) * TOTAL_EXP_KILLS
          + (totalStats.get(RiotRequestSender.TOTAL_RANKED_MINIONS) + totalStats.get(RiotRequestSender.TOTAL_NORMAL_MINIONS)) * TOTAL_EXP_MINIONS
          + (totalStats.get(RiotRequestSender.TOTAL_RANKED_NEUTRAL_MINIONS) + totalStats.get(RiotRequestSender.TOTAL_NORMAL_NEUTRAL_MINIONS)) * TOTAL_EXP_NEUTRAL_MINIONS
          + (totalStats.get(RiotRequestSender.TOTAL_RANKED_TURRETS) + totalStats.get(RiotRequestSender.TOTAL_NORMAL_TURRETS)) * TOTAL_EXP_TURRETS
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_DAMAGE_DONE) * TOTAL_EXP_DAMAGE_DONE
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_DAMAGE_TAKEN) * TOTAL_EXP_DAMAGE_TAKEN
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_HEAL) * TOTAL_EXP_HEAL
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_PENTA) * TOTAL_EXP_PENTA;
        experienceScore *= TOTAL_EXP_MULTIPLIER;
        
        double rankedGameNumber = totalStats.get(RiotRequestSender.TOTAL_RANKED_WON) + totalStats.get(RiotRequestSender.TOTAL_RANKED_LOSSES);
        double rankedWinDifference = totalStats.get(RiotRequestSender.TOTAL_RANKED_WON) - totalStats.get(RiotRequestSender.TOTAL_RANKED_LOSSES);
        double rankedDifferenceRatio = (rankedGameNumber >= TOTAL_RANKED_MIN_GAMES_COUNT) ? rankedWinDifference / rankedGameNumber : TOTAL_RANKED_DEFAULT_RATIO;
        
        double rankedOverallScore = rankedDifferenceRatio * TOTAL_RANKED_WIN_DIFFERENCE_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_DAMAGE_TAKEN) * TOTAL_RANKED_DAMAGE_TAKEN_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_DAMAGE_DONE) * TOTAL_RANKED_DAMAGE_DONE_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_HEAL) * TOTAL_RANKED_HEAL_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_MINIONS) * TOTAL_RANKED_MINION_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_NEUTRAL_MINIONS) * TOTAL_RANKED_NEUTRAL_MINION_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_DEATHS) * TOTAL_RANKED_DEATH_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_ASSISTS) * TOTAL_RANKED_ASSIST_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_KILLS) * TOTAL_RANKED_KILL_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_PENTA) * TOTAL_RANKED_PENTA_RATIO
          + totalStats.get(RiotRequestSender.TOTAL_RANKED_TURRETS) * TOTAL_RANKED_TURRET_RATIO;
        rankedOverallScore /= rankedGameNumber;
        rankedOverallScore *= TOTAL_RANKED_MULTIPLIER;
        
        rankedOverallScore = (rankedOverallScore > 0) ? rankedOverallScore : 0;
        
        totalStats.put("Total ranked score", rankedOverallScore);
        //totalStats.put("Total normal score", normalOverallScore);
        recentStats.put("Total recent score", recentScore);
        totalStats.put("Total experience score", experienceScore);
        
        perfScore = (recentScore /*+ normalOverallScore*/ + rankedOverallScore + experienceScore) * PERF_SCORE_MULTIPLIER;
      }
      group.setPerfValue(perfScore);    
    }
    
    return players;
  }
}




