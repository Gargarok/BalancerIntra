package balancer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


/**
* This item is responsible for taking care of a user input :
* it's supposed to be one line, filled with player names 
* separated by spaces (or sometimes comas or equals, please
* take a look at the user doc).
* From this input, it is going to return an array of PlayerGroup
* filled with the correct player names and their "community score".
* If the player is unknown to the software, its name will be considered
* as a riot nickname, and he will get the community's average as a score.
* (*EXCEPT* if the user asked for the mere, raw, riot-score balancing. In this 
* case the community score will be 0, simple right)
*(Right)
*/
public class NameFormater {  
  public final static String DRAHL = "drahlkan";
  public final static String DELZUR = "delzur";
  public final static String SAKO = "speedy";
  public final static String DD = "lanfeust";
  public final static String GARGA = "potemkin";
  public final static String SNAKEUH = "snakeuh";
  public final static String VICO = "vico";
  public final static String AGO = "ago";
  public final static String THIIB = "thiib";
  public final static String BYA = "byajow";
  public final static String DOYL = "doyl";
  public final static String PEG = "peg";
  public final static String YAYIA = "yayia";
  public final static String WAUN = "waun";
  public final static String CRISTA = "cristallix55";
  public final static String RIKOS = "rc12";
  public final static String BAKAI = "bakaiser0";
  public final static String IMMO = "ewilia";
  public final static String FLECHE = "byakurenhijiri";
  public final static String SWIFTH = "swifth";
  public final static String STAZZ = "stazz";
  public final static String MELO = "melo";
  public final static String HINO = "hino";
  public final static String HAMNISTIE = "hamnistie";
  public final static String BARBIE = "barbie";
  public final static String LUNIWA = "luniwa";
  
  private final static String PARITY_ERROR = "Non valid player number given in parameters. I need a pair number.";
  private final static String BUDDY_ERROR = "Unknown buddy given as a value reference : ";
  private final static String UNKNOWN_COMM = "Unknown player in input : ";
  
  private final static String REGEX_VALUES = " *([a-zA-Z0-9][^\\s\n=]*( +[^\\s\n=]+)*) *= *([0-9]+(\\.[0-9]+)?)";
  private final static int VALUES_NAME_GROUP = 1;
  private final static int VALUES_SCORE_GROUP = 3;
  
  private final static String REGEX_PLAYERS = " *([a-zA-Z0-9]([^\\s\n]* *[^\\s\n]+)*) *";
  private final static int PLAYERS_NAME_GROUP = 1;
  
  private final static Charset CHARSET = StandardCharsets.UTF_8;
  
  private HashMap<String,Double> playerValues;
  
  private double avg;
  
  public NameFormater() {
    this.playerValues = new HashMap<>();
    this.avg = 0;
    this.init();
  }
  
  public NameFormater(String path) {
    this.playerValues = new HashMap<>();
    this.avg = 0;
    this.fileNamesInit(path);
  }
  
  public HashMap<String,Double> getPlayerValues() {
    return this.playerValues;
  }
  
  /**
  * Format the String input (a line with player names, basically) into
  * an array of PlayerGroup which can be used by the Balancer class.
  * Also takes a boolean input, it tells us if we are to take into account our
  * personal-table scores, or not (if not, then most probably we will only care for the Riot score).
  * @throws PlayerNbException: wrong number of players given (not a pair number) 
  * @throws IllegalStateException: a player given wasn't found in our list and our program exploded because of it. Congrats. ... Asshole.
  */
  public PlayerGroup[] format(String lineInput, boolean communityScore, boolean perfScore) {

    String[] players = lineInput.split(" ");
    String[] comaSeparated = lineInput.split(",");

    final int playerNumber = players.length + comaSeparated.length - 1;
    if (playerNumber % 2 != 0) {
      throw new IllegalArgumentException(PARITY_ERROR);
    }
    
    // If communityScore is false, no value will be put in any of the PlayerGroup created
    int mask = (communityScore) ? 1 : 0;
    
    /*
    * The groups array will either contain, as player names :
    * - the community name (if known)
    * - the riot nick (otherwise)
    */ 
    PlayerGroup[] groups = new PlayerGroup[playerNumber];
    
    for (int i = 0 ; i < playerNumber ; i++) {
      // We need to take the lower-case, no-spacing name as it is the playerValues's keys format 
      String formatedName = players[i].toLowerCase().replace(" ", "");
      // First case : the player is one of the people known
      // by the software
      if (playerValues.containsKey(formatedName)) {
        groups[i] = new PlayerGroup(players[i], playerValues.get(formatedName) * mask);
      }
      else {    
        PlayerGroup split_group = new PlayerGroup();
        String[] splitted = players[i].split(",");
        for (String s : splitted) {
          formatedName = s.toLowerCase().replace(" ", "");
          // Second case : known player associated with another one
          if (playerValues.containsKey(formatedName)) {
            split_group.addPlayer(s, playerValues.get(formatedName) * mask,0);
          }
          else {
            // Third case : unknown player associated to a known one with an "="
            String[] equals_split = s.split("=");
            if(equals_split.length >= 2) {
              formatedName = equals_split[1].toLowerCase().replace(" ", "");
              if (!playerValues.containsKey(formatedName)) { 
                throw new IllegalArgumentException(BUDDY_ERROR + equals_split[1]);
              }
              double value = playerValues.get(formatedName);
              playerValues.put(equals_split[0].toLowerCase().replace(" ", ""), value);
              split_group.addPlayer(equals_split[0], value * mask, 0);
            }
            else {
              // Fourth and last case : complete unknown buddy, we consider
              // it's a riot nickname and give him the table's average as
              // value.
              if (!perfScore) {
                throw new IllegalArgumentException(UNKNOWN_COMM + s);
              }
              //String formatedRiotName = s.toLowerCase();
              split_group.addPlayer(s, this.avg * mask, 0);
            }
          }
        }
        groups[i] = split_group;
      }
    }
    return groups;
  }
  
  public PlayerGroup[] formatFromFile(String path, boolean communityScore, boolean perfScore) {
    List<PlayerGroup> finalList = new ArrayList<>();
    // If communityScore is false, no value will be put in any of the PlayerGroup created
    int scoreMask = (communityScore) ? 1 : 0;

    Path javaPath = FileSystems.getDefault().getPath(path);

    try( Stream<String> lines = Files.lines(javaPath, CHARSET)) { 
      for(String line : (Iterable<String>) lines::iterator ) { 
        Pattern p = Pattern.compile(REGEX_PLAYERS);
        Matcher m = p.matcher(line);
        if (m.find()) {
          String player = m.group(PLAYERS_NAME_GROUP);
          String formatedName = player.toLowerCase().replace(" ", "");
          if (playerValues.containsKey(formatedName)) {
            finalList.add(new PlayerGroup(player, playerValues.get(formatedName) * scoreMask));
          }
          else {
            // Second case : known player associated with another one
            PlayerGroup split_group = new PlayerGroup();
            String[] splitted = player.split(",");
            for (String s : splitted) {
              formatedName = s.toLowerCase().replace(" ", "");
              if (playerValues.containsKey(formatedName)) {
                split_group.addPlayer(s, playerValues.get(formatedName) * scoreMask,0);
              }
              else {
                // Third case : unknown player associated to a known one with an "="
                String[] equals_split = s.split("=");
                if(equals_split.length >= 2) {
                  formatedName = equals_split[1].toLowerCase().replace(" ", "");
                  if (!playerValues.containsKey(formatedName)) { 
                    throw new IllegalArgumentException(BUDDY_ERROR + equals_split[1] + "\n(Buddy error, formatFromFile(), NameFormater)");
                  }
                  double value = playerValues.get(formatedName);
                  playerValues.put(equals_split[0].toLowerCase().replace(" ", ""), value);
                  split_group.addPlayer(equals_split[0], value * scoreMask, 0);
                }
                else {
                  // Fourth and last case : complete unknown buddy, we consider
                  // it's a LoL nickname and give him the table's average as
                  // value. (Illegal argument exception if we didn't ask for Perf score,
                  // since then the LoL nick would be useless and the guy is unknown)
                  if (!perfScore) {
                    throw new IllegalArgumentException(UNKNOWN_COMM + s + "\n(Unknown_Comm error, formatFromFile(), NameFormater)");
                  }
                  split_group.addPlayer(s, this.avg * scoreMask, 0);
                }
              }
            }
            finalList.add(split_group);
          } 
        }
      }
    }
    catch (IOException e) {
      System.out.println("Fatal error while processing input : I cannot read your file correctly, mate.");
      System.exit(-1);
    }
    
    PlayerGroup[] result = new PlayerGroup[finalList.size()];
    for (int i = 0 ; i < finalList.size() ; i++) {
      result[i] = finalList.get(i);
    }
    return result;
    //return (PlayerGroup[]) finalList.toArray();
  }
  
  /*
   * Init method called only when no file input has been given.
   */
  private void init() {
    playerValues.put(DRAHL, 24.87);
    playerValues.put(DELZUR, 14.87);
    playerValues.put(SAKO, 25.31);
    playerValues.put(DD, 23.25);
    playerValues.put(GARGA, 24.25);
    playerValues.put(SNAKEUH, 25.5);
    playerValues.put(VICO, 23.68);
    playerValues.put(AGO, 19.81);
    playerValues.put(THIIB, 17.92);
    playerValues.put(BYA, 16.81);
    playerValues.put(DOYL, 16.75);
    playerValues.put(PEG, 12.81);
    playerValues.put(YAYIA, 17.97);
    playerValues.put(WAUN, 15.85);
    playerValues.put(CRISTA, 13.62);
    playerValues.put(RIKOS, 14.68);
    playerValues.put(BAKAI, 13.28);
    playerValues.put(IMMO, 10.75);
    playerValues.put(FLECHE, 11.34);
    playerValues.put(SWIFTH, 11.37);
    playerValues.put(STAZZ, 11.93);
    playerValues.put(MELO, 7.91);
    playerValues.put(HINO, 8.5); 
    playerValues.put(HAMNISTIE, 14.2);
    playerValues.put(BARBIE, 14.95);
    playerValues.put(LUNIWA, 12.8);
    
    // mean calculation
    for (double i : playerValues.values()) {
      avg += i;
    }
    avg /= playerValues.size();
    
  }
  
  /*
   * Important detail : the names are all put into the playerValues Map in
   * LOWER CASE and WITHOUT SPACES, as for Riot the league of legends' nicks are case-insensitive
   * (and space-insensitive)
   * This allows the user some freedom when writing names in his two files.
   */
  private void fileNamesInit(String path) {
    Path javaPath = FileSystems.getDefault().getPath(path);
    
    try( Stream<String> lines = Files.lines(javaPath, CHARSET)) {
      for(String line : (Iterable<String>) lines::iterator ) { 
        Pattern p = Pattern.compile(REGEX_VALUES);
        Matcher m = p.matcher(line);
        
        if (m.find()) {
          //System.out.println("Added name : " + m.group(VALUES_NAME_GROUP));
          String playerName = m.group(VALUES_NAME_GROUP).toLowerCase();
          playerName = playerName.replace(" ", "");
          playerValues.put(playerName, Double.parseDouble(m.group(VALUES_SCORE_GROUP)));
          this.avg += Double.parseDouble(m.group(VALUES_SCORE_GROUP));
        }
      } 
      this.avg /= playerValues.size();
    }
    catch (IOException e) {
      System.out.println("Fatal error while processing input : I cannot read your file correctly, mate.");
      System.exit(-1);
    }
  }
}





