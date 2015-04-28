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
  public final static String DRAHL = "Drahlkan";
  public final static String DELZUR = "Delzur";
  public final static String SAKO = "Sako";
  public final static String DD = "DD";
  public final static String GARGA = "Garga";
  public final static String SNAKEUH = "Snakeuh";
  public final static String VICO = "Vico";
  public final static String AGO = "Ago";
  public final static String THIIB = "Thiib";
  public final static String BYA = "Byajow";
  public final static String DOYL = "Doyl";
  public final static String PEG = "Peg";
  public final static String YAYIA = "Yayia";
  public final static String WAUN = "Waun";
  public final static String CRISTA = "Cristallix";
  public final static String RIKOS = "RC";
  public final static String BAKAI = "Bakaiser";
  public final static String IMMO = "Immo";
  public final static String FLECHE = "Fleche";
  public final static String SWIFTH = "Swifth";
  public final static String STAZZ = "Stazz";
  public final static String MELO = "Melo";
  public final static String HINO = "Hino";
  public final static String HAMNISTIE = "Hamnistie";
  public final static String BARBIE = "Barbie";
  public final static String LUNIWA = "Luniwa";
  
  private final static String PARITY_ERROR = "Non valid player number given in parameters. I need a pair number.";
  private final static String BUDDY_ERROR = "Unknown buddy given as a value reference : ";
  private final static String UNKNOWN_COMM = "Unknown player in input : ";
  
  private final static String REGEX_VALUES = " *(.*?) *= *([0-9]+(\\.[0-9]+)?)";
  private final static String REGEX_PLAYERS = " *([^\\s\n]([^\\s\n]* *[^\\s\n]+)*) *";
  
  private final static Charset CHARSET = StandardCharsets.UTF_8;
  
  private HashMap<String,Double> playerValues;
  private HashMap<String,String> riotNames;
  
  private double avg;
  
  public NameFormater() {
    this.playerValues = new HashMap<>();
    this.riotNames = new HashMap<>();
    this.avg = 0;
    this.init();
  }
  
  public NameFormater(String path) {
    this.playerValues = new HashMap<>();
    this.riotNames = new HashMap<>();
    this.avg = 0;
    this.fileNamesInit(path);
  }
  
  public HashMap<String,Double> getPlayerValues() {
    return this.playerValues;
  }
  
  public HashMap<String,String> getRiotNames() {
    return this.riotNames;
  }
  
  private void init() {
    /**
    * Riot names : need to be fully lower case, with no space
    */
    riotNames.put(DRAHL, "drahlkan");
    riotNames.put(DELZUR, "delzur");
    riotNames.put(SAKO, "speedy");
    riotNames.put(DD, "lanfeust");
    riotNames.put(GARGA, "potemkin");
    riotNames.put(SNAKEUH, "snakeuh");
    riotNames.put(VICO, "vıco");
    riotNames.put(AGO, "agolstar");
    riotNames.put(THIIB, "thiib");
    riotNames.put(BYA, "byajow");
    riotNames.put(DOYL, "doyl");
    riotNames.put(PEG, "liddoch");
    riotNames.put(YAYIA, "pornstarreksai");
    riotNames.put(WAUN, "waun");
    riotNames.put(CRISTA, "cristallix55");
    riotNames.put(RIKOS, "rc12");
    riotNames.put(BAKAI, "bakaiser0");
    riotNames.put(IMMO, "ewilia");
    riotNames.put(FLECHE, "byakurenhijiri");
    riotNames.put(SWIFTH, "mriswifth");
    riotNames.put(STAZZ, "stazz");
    riotNames.put(MELO, "owimelody");
    riotNames.put(HINO, "hinota"); 
    riotNames.put(HAMNISTIE, "hamnistie");
    riotNames.put(BARBIE, "lapoupéebarbie");
    riotNames.put(LUNIWA, "luniwa");
    
    playerValues.put(riotNames.get(DRAHL), 24.87);
    playerValues.put(riotNames.get(DELZUR), 14.87);
    playerValues.put(riotNames.get(SAKO), 25.31);
    playerValues.put(riotNames.get(DD), 23.25);
    playerValues.put(riotNames.get(GARGA), 24.25);
    playerValues.put(riotNames.get(SNAKEUH), 25.5);
    playerValues.put(riotNames.get(VICO), 23.68);
    playerValues.put(riotNames.get(AGO), 19.81);
    playerValues.put(riotNames.get(THIIB), 17.92);
    playerValues.put(riotNames.get(BYA), 16.81);
    playerValues.put(riotNames.get(DOYL), 16.75);
    playerValues.put(riotNames.get(PEG), 12.81);
    playerValues.put(riotNames.get(YAYIA), 17.97);
    playerValues.put(riotNames.get(WAUN), 15.85);
    playerValues.put(riotNames.get(CRISTA), 13.62);
    playerValues.put(riotNames.get(RIKOS), 14.68);
    playerValues.put(riotNames.get(BAKAI), 13.28);
    playerValues.put(riotNames.get(IMMO), 10.75);
    playerValues.put(riotNames.get(FLECHE), 11.34);
    playerValues.put(riotNames.get(SWIFTH), 11.37);
    playerValues.put(riotNames.get(STAZZ), 11.93);
    playerValues.put(riotNames.get(MELO), 7.91);
    playerValues.put(riotNames.get(HINO), 8.5); 
    playerValues.put(riotNames.get(HAMNISTIE), 14.2);
    playerValues.put(riotNames.get(BARBIE), 14.95);
    playerValues.put(riotNames.get(LUNIWA), 12.8);
    
    // mean calculation
    for (double i : playerValues.values()) {
      avg += i;
    }
    avg /= playerValues.size();
    
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
      // First case : the player is one of the people known
      // by the software
      if (riotNames.containsKey(players[i])) {
        groups[i] = new PlayerGroup(riotNames.get(players[i]), playerValues.get(riotNames.get(players[i])) * mask);
      }
      else {    
        PlayerGroup split_group = new PlayerGroup();
        String[] splitted = players[i].split(",");
        for (String s : splitted) {
          // Second case : known player associated with another one
          if (riotNames.containsKey(s)) {
            split_group.addPlayer(s, playerValues.get(riotNames.get(s)) * mask,0);
          }
          else {
            // Third case : unknown player associated to a known one with an "="
            String[] equals_split = s.split("=");
            if(equals_split.length >= 2) {
              if (!riotNames.containsKey(equals_split[1])) { 
                throw new IllegalArgumentException(BUDDY_ERROR + equals_split[1]);
              }
              String formatedRiotName = equals_split[0].toLowerCase();
              double value = playerValues.get(riotNames.get(equals_split[1]));
              playerValues.put(formatedRiotName, value);
              split_group.addPlayer(formatedRiotName, value * mask, 0);
            }
            else {
              // Fourth and last case : complete unknown buddy, we consider
              // it's a riot nickname and give him the table's average as
              // value.
              if (!perfScore) {
                throw new IllegalArgumentException(UNKNOWN_COMM + s);
              }
              String formatedRiotName = s.toLowerCase();
              split_group.addPlayer(formatedRiotName, this.avg * mask, 0);
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
    int mask = (communityScore) ? 1 : 0;

    Path javaPath = FileSystems.getDefault().getPath(path);

    try( Stream<String> lines = Files.lines(javaPath, CHARSET)) { 
      for(String line : (Iterable<String>) lines::iterator ) { 
        Pattern p = Pattern.compile(REGEX_PLAYERS);
        Matcher m = p.matcher(line);
        if (m.find()) {
          String player = m.group(0);
          if (riotNames.containsKey(player)) {
            finalList.add(new PlayerGroup(riotNames.get(player), playerValues.get(player) * mask));
          }
          else {    
            PlayerGroup split_group = new PlayerGroup();
            String[] splitted = player.split(",");
            for (String s : splitted) {
              // Second case : known player associated with another one
              if (riotNames.containsKey(s)) {
                split_group.addPlayer(s, playerValues.get(riotNames.get(s)) * mask,0);
              }
              else {
                // Third case : unknown player associated to a known one with an "="
                String[] equals_split = s.split("=");
                if(equals_split.length >= 2) {
                  if (!riotNames.containsKey(equals_split[1])) { 
                    throw new IllegalArgumentException(BUDDY_ERROR + equals_split[1]);
                  }
                  String formatedRiotName = equals_split[0].toLowerCase();
                  double value = playerValues.get(riotNames.get(equals_split[1]));
                  playerValues.put(formatedRiotName, value);
                  split_group.addPlayer(formatedRiotName, value * mask, 0);
                }
                else {
                  // Fourth and last case : complete unknown buddy, we consider
                  // it's a riot nickname and give him the table's average as
                  // value.
                  if (!perfScore) {
                    throw new IllegalArgumentException(UNKNOWN_COMM + s);
                  }
                  String formatedRiotName = s.toLowerCase();
                  split_group.addPlayer(formatedRiotName, this.avg * mask, 0);
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
  
  private void fileNamesInit(String path) {
    Path javaPath = FileSystems.getDefault().getPath(path);
    
    try( Stream<String> lines = Files.lines(javaPath, CHARSET)) { 
      for(String line : (Iterable<String>) lines::iterator ) { 
        System.out.println(line);
        Pattern p = Pattern.compile(REGEX_VALUES);
        Matcher m = p.matcher(line);
        if (m.find()) {
          playerValues.put(m.group(1), Double.parseDouble(m.group(2)));
          riotNames.put(m.group(1), m.group(1).replace(" ", ""));
          this.avg += Double.parseDouble(m.group(2));
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





