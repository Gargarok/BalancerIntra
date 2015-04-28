package balancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RiotRequestSender {
  /** 
  * Recent matches stats : keys for the resulting map
  */
  private final static String[] RECENT_RELEVANT_MODES = {"NORMAL", "RANKED_SOLO_5x5", "RANKED_PREMADE_3x3", 
    "RANKED_PREMADE_5x5", "RANKED_TEAM_3x3", "RANKED_TEAM_5x5", "NORMAL_3x3", "ARAM_UNRANKED_5x5", "URF"};
  public final static String RECENT_KILLS = "championsKilled";
  public final static String RECENT_ASSISTS = "assists";
  public final static String RECENT_DEATHS = "numDeaths";
  public final static String RECENT_MINIONS = "minionsKilled";
  public final static String RECENT_NEXUSKILL = "nexusKilled";
  public final static String RECENT_DAMAGE = "totalDamageDealtToChampions";
  public final static String RECENT_CC = "totalTimeCrowdControlDealt";
  public final static String RECENT_WARDPLACED = "wardPlaced";
  public final static String RECENT_WIN = "win";
  public final static String RECENT_HEAL = "totalHeal";
  public final static String RECENT_GAMENB = "gameNb";
  
  /** 
  * Global stats : once again, keys for the resulting map, bla bla bla, see getTotalStats()
  */
  //private final static String[] TOTAL_RELEVANT_MODES = {"AramUnranked5x5", "RankedTeam3x3", "RankedPremade3x3", "Unranked3x3", 
    //         "RankedPremade5x5", "RankedSolo5x5", "RankedTeam5x5", "Unranked", "CAP5x5", "CounterPick", "URF"};
  private final static String[] TOTAL_RELEVANT_MODES = {"AramUnranked5x5", "Unranked3x3", "Unranked", "CAP5x5", "CounterPick", "URF"};
  public final static String TOTAL_NORMAL_WINS = "wins";   
  public final static String TOTAL_NORMAL_KILLS = "totalChampionKills";
  public final static String TOTAL_NORMAL_ASSISTS = "totalAssists";
  public final static String TOTAL_NORMAL_NEUTRAL_MINIONS = "totalNeutralMinionsKilled";
  public final static String TOTAL_NORMAL_TURRETS = "totalTurretsKilled";
  public final static String TOTAL_NORMAL_MINIONS = "totalMinionKills";
  
  public final static String TOTAL_RANKED_ASSISTS = "rankedTotalAssists";
  public final static String TOTAL_RANKED_KILLS = "rankedTotalChampionKills";
  public final static String TOTAL_RANKED_DAMAGE_DONE = "totalDamageDealt";
  public final static String TOTAL_RANKED_HEAL = "totalHeal";  
  public final static String TOTAL_RANKED_MINIONS = "rankedTotalMinionKills";
  public final static String TOTAL_RANKED_PENTA = "totalPentaKills";
  public final static String TOTAL_RANKED_WON = "totalSessionsWon";
  public final static String TOTAL_RANKED_LOSSES = "totalSessionsLost";
  public final static String TOTAL_RANKED_DAMAGE_TAKEN = "totalDamageTaken";
  public final static String TOTAL_RANKED_DEATHS = "totalDeathsPerSession";
  public final static String TOTAL_RANKED_NEUTRAL_MINIONS = "rankedTotalNeutralMinionsKilled";
  public final static String TOTAL_RANKED_TURRETS = "rankedTotalTurretsKilled";
  
  
  /*
  * Riot URL addresses
  */
  private final static String DEV_KEY = "?api_key=ENCRYPTED_HIDDEN_SECRET_PROTECTED_BURROWED_INVISIBLE_KEY";
  private final static String NAME_URL = "https://euw.api.pvp.net/api/lol/euw/v1.4/summoner/by-name/";
  private final static String RECENT_URL = "https://euw.api.pvp.net/api/lol/euw/v1.3/game/by-summoner/";
  private final static String STATS_URL = "https://euw.api.pvp.net/api/lol/euw/v1.3/stats/by-summoner/";
  private final static String TIERS_URL = "https://euw.api.pvp.net/api/lol/euw/v2.5/league/by-summoner/";
  
  // Different ranked adds we ought to put to the ranked-games URL.
  private final static String[] URL_ADDS_RANKED = {"&season=SEASON2015", "&season=SEASON3", "&season=SEASON2014"};
  
  /*
  * Regular expressions
  */
  // First group : summoner name, second group : summoner id
  private final static String REGEX_ID = "\"([^\"]*?)\":\\{\"id\":(.*?),";
  
  // subType of the game (actually represents which kind of it is, we use it to know if there are wards or not)
    private final static String REGEX_RECENT_SUBTYPE = "\"subType\":\"(.*?)\",";      

  // Group 1 : contains all the data about one given match. Find each match by looking for
  // a pattern as follows : "gameId" <bla bla> [<bla>] <bip> {<beulululu>}} (without spaces)
  private final static String REGEX_RECENT_ALLSTATS = "(\"gameId\".*?\\[.*?\\].*?\\{.*?\\}\\})";

  private final static String REGEX_GLOBAL_PERQUEUE = "(\\{\"playerStatSummaryType\".*?\\{.*?\\}\\})";
  private final static String REGEX_GLOBAL_GAMETYPE = "\"playerStatSummaryType\":\"(.*?)\",";
  // This one get the stats for the id 0 champion, which contains the total stats for every champ.
  private final static String REGEX_GLOBAL_RANKEDSTATS = "\"id\":0,(.*?)\\}";
  
  
  /*
  * Here we find the rate information : little and big rate.
  * Little rate is a max number of requests per given time (ex : 10 requests /s),
  * and big rate is the same over a wider window (ex : 500 requests /min).
  * Written in milliseconds here.
  */
  private final static int LITTLE_RATE_NB = 5;    // 10
  private final static int LITTLE_RATE_TIME = 15000;  // 10000
  private final static int BIG_RATE_NB = 20;    //500  
  private final static int BIG_RATE_TIME = 70000;  // 600000
  private final static int SECURITY_SPACE = 100;
  
  private final static int HTTP_NOTFOUND = 404;
  
  private long[] requestsTime;
  private int cursor;
  private final HashSet<String> allRecentModes;
  private final HashSet<String> totalStatsModes;
  
  public RiotRequestSender() { 
    this.allRecentModes = new HashSet<>(Arrays.asList(RECENT_RELEVANT_MODES));
    this.totalStatsModes = new HashSet<>(Arrays.asList(TOTAL_RELEVANT_MODES));
    this.requestsTime = new long[BIG_RATE_NB];
    this.cursor = 0;
  }  
  
  /**
  * Returns a HashMap pointing to the summoner ids associated with the
  * names in the "names" input.
  * @throws IOException 
  */
  public HashMap<String,Long> getSummonerId(String[] names) throws IOException {
    String nameConcat = "";
    for (String n : names) {
      nameConcat += n.replace(" ", "") + ",";
    }
    nameConcat = nameConcat.substring(0,nameConcat.length()-1);
    //System.out.println("Searching for : " + nameConcat);
    String result = requestGet(NAME_URL + nameConcat + DEV_KEY);
    //System.out.println("request result : " + result);
    HashMap<String,Long> idMap = new HashMap<>();
    Pattern p = Pattern.compile(REGEX_ID);
    Matcher m = p.matcher(result);
    while(m.find()) {
      String name = m.group(1);
      //System.out.print("Name : " + m.group(1));
      //System.out.println(", id : " + m.group(2));
      Long id = Long.parseLong(m.group(2));
      idMap.put(name,id);
    }
    
    return idMap;
  }
  
  /**
  * Returns a HashMap associating each field we have an interest in with 
  * the total value of the given field (summed for every game present, when
  * it's a statistic. Only the win number and game number are flat values here).
  * @throws IOException 
  */
  public HashMap<String, Double> getRecentMatches(long summonerId) throws IOException {
    String result = requestGet(RECENT_URL + summonerId + "/recent" + DEV_KEY);
    
    HashMap<String,Double> statsMap = new HashMap<>();
    double kills = 0;
    double assists = 0;
    double deaths = 0;
    double minions = 0;
    double nexus = 0;
    double damage = 0;
    double cc = 0;
    double wards = 0;
    double wins = 0;
    double games = 0;
    double heal = 0;
    
    
    /*
    * It's quite easy actually, we have a text input and we want to extract the interesting 
    * data out of it. So we use regular expressions, shaped as needed, and we read the data.
    * Simple !
    */
    Pattern p = Pattern.compile(REGEX_RECENT_ALLSTATS);
    Matcher fullMatch = p.matcher(result);
    while(fullMatch.find()) {
      // Everytime we get here, fullMatch contains the stats of one specific match
      String isolatedMatch = fullMatch.group(1);
      String subType = "";
      Matcher typeMatch = Pattern.compile(REGEX_RECENT_SUBTYPE).matcher(isolatedMatch);
      if (typeMatch.find()) {
        subType = typeMatch.group(1);
      }
      
      // Selection here : we don't want to get weird modes stats, like 1x1 or something 
      // (Just imagine, 1x1, unbelievable)
      // (... Unbelievable)
      if (this.allRecentModes.contains(subType)) {
        deaths += getField(isolatedMatch, RECENT_DEATHS);
        minions += getField(isolatedMatch, RECENT_MINIONS);
        kills += getField(isolatedMatch, RECENT_KILLS);
        heal += getField(isolatedMatch, RECENT_HEAL);
        assists += getField(isolatedMatch, RECENT_ASSISTS);
        damage += getField(isolatedMatch, RECENT_DAMAGE);
        cc += getField(isolatedMatch, RECENT_CC);
        wins += getField(isolatedMatch, RECENT_WIN);
        wards += getField(isolatedMatch, RECENT_WARDPLACED); 
        games++;
      }
    }
    statsMap.put(RECENT_KILLS,kills);
    statsMap.put(RECENT_ASSISTS,assists);
    statsMap.put(RECENT_DEATHS,deaths);
    statsMap.put(RECENT_MINIONS,minions);
    statsMap.put(RECENT_NEXUSKILL,nexus);
    statsMap.put(RECENT_DAMAGE,damage);
    statsMap.put(RECENT_CC,cc);
    statsMap.put(RECENT_WARDPLACED,wards);
    statsMap.put(RECENT_HEAL,heal);
    statsMap.put(RECENT_WIN,wins);
    statsMap.put(RECENT_GAMENB,games);
    return statsMap;
  } 
  
  /**
  * Same as with the getRecentMatches method, only this time
  * it collects data from all matches.
  * @throws IOException 
  */
  public HashMap<String, Double> getTotalStats(long summonerId) throws IOException {
    HashMap<String,Double> statsMap = new HashMap<>();
    
    double normalWins = 0;
    double normalKills = 0;
    double normalAssists = 0;
    double normalMinions = 0;
    double normalNeutralMinions = 0;
    double normalTurretsKilled = 0;
    
    String result = requestGet(STATS_URL + summonerId + "/summary" + DEV_KEY);
    
    // We isolate every stat block from the data, for every type of queue
    // (Normal unranked, ARAM, etc)
    Pattern perQueue = Pattern.compile(REGEX_GLOBAL_PERQUEUE);
    Matcher selectedQueues = perQueue.matcher(result);
    
    while(selectedQueues.find()) {
      String rawData = selectedQueues.group(1);
      // Each time we pass here, we're looking at a different queue
      Pattern queue = Pattern.compile(REGEX_GLOBAL_GAMETYPE);
      Matcher type = queue.matcher(rawData);
      boolean found = type.find();
      
      if (found && this.totalStatsModes.contains(type.group(1))) {
        normalKills += getField(rawData, TOTAL_NORMAL_KILLS);      
        normalAssists += getField(rawData, TOTAL_NORMAL_ASSISTS);      
        normalMinions += getField(rawData, TOTAL_NORMAL_MINIONS);      
        normalWins += getField(rawData, TOTAL_NORMAL_WINS);   
        normalNeutralMinions += getField(rawData, TOTAL_NORMAL_NEUTRAL_MINIONS);   
        normalTurretsKilled += getField(rawData, TOTAL_NORMAL_TURRETS);   
      }
    }
    
    double rankedWins = 0;
    double rankedLosses = 0;
    double rankedDamageTaken = 0;
    double rankedNeutralMinions = 0;
    double rankedTurretsKilled = 0;
    double rankedAssists = 0;
    double rankedKills = 0;
    double rankedMinions = 0;
    double rankedPenta = 0;
    double rankedDamageDone = 0;
    double rankedDeaths = 0;
    double rankedHeal = 0;
    
    for(String urlAdd : URL_ADDS_RANKED) {
      result = requestGet(STATS_URL + summonerId + "/ranked" + DEV_KEY + urlAdd);
      // We get the total stats for this ranked season
      Pattern totalStats = Pattern.compile(REGEX_GLOBAL_RANKEDSTATS);
      
      Matcher statsMatcher = totalStats.matcher(result);
      boolean found = statsMatcher.find();
      if (found) {
        String rawData = statsMatcher.group(1);
        
        rankedWins += getField(rawData, TOTAL_RANKED_WON);
        rankedLosses += getField(rawData, TOTAL_RANKED_LOSSES);
        rankedDamageTaken += getField(rawData, TOTAL_RANKED_DAMAGE_TAKEN);
        rankedNeutralMinions += getField(rawData, TOTAL_NORMAL_NEUTRAL_MINIONS);
        rankedTurretsKilled += getField(rawData, TOTAL_NORMAL_TURRETS);
        rankedAssists += getField(rawData, TOTAL_NORMAL_ASSISTS);
        rankedKills += getField(rawData, TOTAL_NORMAL_KILLS);
        rankedMinions += getField(rawData, TOTAL_NORMAL_MINIONS);
        rankedPenta += getField(rawData, TOTAL_RANKED_PENTA);
        rankedDamageDone += getField(rawData, TOTAL_RANKED_DAMAGE_DONE);
        rankedDeaths += getField(rawData, TOTAL_RANKED_DEATHS);
        rankedHeal += getField(rawData, TOTAL_RANKED_HEAL);   
      }
    }
    
    statsMap.put(TOTAL_NORMAL_WINS,normalWins);
    statsMap.put(TOTAL_NORMAL_KILLS,normalKills);
    statsMap.put(TOTAL_NORMAL_ASSISTS,normalAssists);
    statsMap.put(TOTAL_NORMAL_MINIONS,normalMinions);
    statsMap.put(TOTAL_NORMAL_NEUTRAL_MINIONS,normalNeutralMinions);
    statsMap.put(TOTAL_NORMAL_TURRETS,normalTurretsKilled);
    
    statsMap.put(TOTAL_RANKED_WON, rankedWins);
    statsMap.put(TOTAL_RANKED_LOSSES, rankedLosses);
    statsMap.put(TOTAL_RANKED_DAMAGE_TAKEN, rankedDamageTaken);
    statsMap.put(TOTAL_RANKED_NEUTRAL_MINIONS, rankedNeutralMinions);
    statsMap.put(TOTAL_RANKED_TURRETS, rankedTurretsKilled);
    statsMap.put(TOTAL_RANKED_ASSISTS, rankedAssists);
    statsMap.put(TOTAL_RANKED_KILLS, rankedKills);
    statsMap.put(TOTAL_RANKED_MINIONS, rankedMinions);
    statsMap.put(TOTAL_RANKED_PENTA, rankedPenta);
    statsMap.put(TOTAL_RANKED_DAMAGE_DONE, rankedDamageDone);
    statsMap.put(TOTAL_RANKED_DEATHS, rankedDeaths);
    statsMap.put(TOTAL_RANKED_HEAL, rankedHeal);
    
    return statsMap;
  }
  
  /**
  * Sends an HTTP GET request at the chosen URL.
  * Returns a String representation of the result.
  * @throws IOException : the crashes coming from the request sending will throw this. Malformed URL, and such. 
  * Specialize reception to get more details, please.
  */
  private String requestGet(String targetURL) throws IOException {
    // Time check (making sure our rate allows us to send the 
    // request, and update the time array)
    int offset = 0;
    int bigRequestCount = 0;
    int smallRequestCount = 0;
    long oldestLilRequest = Long.MAX_VALUE;
    long oldestBigRequest = Long.MAX_VALUE;
    for (int i = 0 ; i < this.cursor ; i++) {
      // If this specific request was done more than BIG_RATE_TIME milliseconds from now
      if (System.currentTimeMillis() - this.requestsTime[i] >= BIG_RATE_TIME) {
        offset++;
      }
      // If the request was done between LITTLE_RATE_TIME and BIG_RATE_TIME
      else if (System.currentTimeMillis() - this.requestsTime[i] >= LITTLE_RATE_TIME) {
        bigRequestCount++;
        this.requestsTime[i-offset] = this.requestsTime[i];
        if (this.requestsTime[i] < oldestBigRequest) oldestBigRequest = this.requestsTime[i];
      }
      // Finally, if the request was done under LITTLE_RATE_TIME
      else {
        bigRequestCount++;
        smallRequestCount++;  
        this.requestsTime[i-offset] = this.requestsTime[i];
        if (this.requestsTime[i] < oldestLilRequest) oldestLilRequest = this.requestsTime[i];
        if (this.requestsTime[i] < oldestBigRequest) oldestBigRequest = this.requestsTime[i];
      }
    }
    this.cursor -= offset;
    
    //System.out.println("Number of big requests : " + bigRequestCount);
    //System.out.println("Number of small requests : " + smallRequestCount);
    
    // in case one of our limitation rates is reached, we have to wait until being allowed
    // to do requests again. Since our array is sorted, we can actually wait
    // for the first entry to go away.
    try {
      if (bigRequestCount >= BIG_RATE_NB - 1) {
        long waitTime = BIG_RATE_TIME + SECURITY_SPACE - (System.currentTimeMillis() - oldestBigRequest);
        System.out.println("Sorry, too many requests were made recently, we have to wait " + waitTime/1000 + "s.");
        Thread.sleep(waitTime);
      }
      else if (smallRequestCount >= LITTLE_RATE_NB - 1) {
        long waitTime = LITTLE_RATE_TIME + SECURITY_SPACE - (System.currentTimeMillis() - oldestLilRequest);
        System.out.println("Sorry, too many requests were made recently, we have to wait " + waitTime/1000 + "s.");
        Thread.sleep(waitTime);
      }
    } 
    catch (InterruptedException e) {
      System.out.println("Unwanted, unexpected waking up order received. The next request will inevitably probably make the world crumble. So I'm not exiting the program just yet.");
    }
    
    URL obj;
    obj = new URL(targetURL);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    
    con.setRequestMethod("GET");
    
    int responseCode = con.getResponseCode();
    // Sometimes stats will not be found. Whenever this happens, we're returning right away, no need to do the rest.
    if (responseCode == HTTP_NOTFOUND) {
      return Integer.toString(responseCode);
    }
    
    BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();
    
    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    
    // update the time array
    this.requestsTime[this.cursor] = System.currentTimeMillis();
    this.cursor++;
    
    return response.toString();
  }
  
  private Double getField(String data, String regex) {
    Pattern regexp = Pattern.compile("\"" + regex + "\":(.*?)[,\\}\\]\n]");
    Matcher localResult = regexp.matcher(data);
    boolean found = localResult.find();
    if (found) {
      try {
        return Double.parseDouble(localResult.group(1));
      }
      catch (NumberFormatException e) { // Not a number : we expect it to be true or false, ans return 1 for true and 0 for everything else
        // We consider that the line asking for this field knows what it expects
        if (localResult.group(1).equals("true")) return 1.0;
        else return 0.0;
      }
    }
    return 0.0;
  }
  
  /**
  * These methods have a debug purpose and are only useful for me, I'm supposed to erase
  * them as soon as everything is working. However, since I WILL forget
  * about it, please be so kind as to remind me, thank you mate.
  * (oh by the way, I saw the "Matrix Reloaded" movie yesterday again, it's
  * quite funny how they say that once its lifespan is over, a program can either
  * face deletion or hide. Maybe these methods will try to hide in the matrix
  * as well, heh ? Well, we shall see about that, for I really intend to erase them).
  * (... Good luck with your hiding)
  * @throws IOException : when the universe crashes, we get an exception here from the request sender
  */
  //TODO: delete all this
  public void debugRegexId(String[] names) throws IOException {
    HashMap<String,Long> idMap = this.getSummonerId(names);
    for (String name : idMap.keySet()) {
      System.out.println("Name : " + name + ", id : " + idMap.get(name) + ".");
    }
  }
  
  //TODO: delete this too
  public void debugRegexRecent(long id) throws IOException {
    HashMap<String,Double> statMap = this.getRecentMatches(id);
    System.out.println("Request recent stats for user number " + id + " : ");
    for (String field : statMap.keySet()) {
      System.out.println("Field " + field + " = " + statMap.get(field) + ".");
    }
  }
  
  //TODO: Yes, yes, this as well, come on let's not repeat that everywhere
  public void debugRegexTotal(long id) throws IOException {
    HashMap<String,Double> statMap = this.getTotalStats(id);
    System.out.println("Request total stats for user number " + id + " : ");
    for (String field : statMap.keySet()) {
      System.out.println("Field " + field + " = " + statMap.get(field) + ".");
    }
  }
  
  //TODO: ...
  public void debugNameById(long id) throws IOException {
    System.out.println(this.requestGet("https://euw.api.pvp.net/api/lol/euw/v1.4/summoner/" + id + DEV_KEY));
  }
  
  //TODO: put some vanishing powder here
  public String debugIWantToSeeThisResult(long[] ids) throws IOException {
    String id = "";
    for (long l : ids) {
      id += (l + ",");
    }
    id = id.substring(0, id.length()-1);
    String result = this.requestGet(TIERS_URL + id + "/entry" + DEV_KEY);
    //System.out.println(this.requestGet(TIERS_URL + id + DEV_KEY));    // Changed my mind. VERY bad idea.
    return result;
  }
}






