package balancer;

import java.util.HashMap;
import exception.PlayerException;

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
  
  private HashMap<String,Double> player_values;
  private HashMap<String,String> riotNames;
  private double avg;
  
  public NameFormater() {
    this.player_values = new HashMap<>();
    this.riotNames = new HashMap<>();
    this.avg = 0;
  }
  
  public HashMap<String,Double> getPlayerValues() {
    return this.player_values;
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
    
    player_values.put(riotNames.get(DRAHL), 24.87);
    player_values.put(riotNames.get(DELZUR), 14.87);
    player_values.put(riotNames.get(SAKO), 25.31);
    player_values.put(riotNames.get(DD), 23.25);
    player_values.put(riotNames.get(GARGA), 24.25);
    player_values.put(riotNames.get(SNAKEUH), 25.5);
    player_values.put(riotNames.get(VICO), 23.68);
    player_values.put(riotNames.get(AGO), 19.81);
    player_values.put(riotNames.get(THIIB), 17.92);
    player_values.put(riotNames.get(BYA), 16.81);
    player_values.put(riotNames.get(DOYL), 16.75);
    player_values.put(riotNames.get(PEG), 12.81);
    player_values.put(riotNames.get(YAYIA), 17.97);
    player_values.put(riotNames.get(WAUN), 15.85);
    player_values.put(riotNames.get(CRISTA), 13.62);
    player_values.put(riotNames.get(RIKOS), 14.68);
    player_values.put(riotNames.get(BAKAI), 13.28);
    player_values.put(riotNames.get(IMMO), 10.75);
    player_values.put(riotNames.get(FLECHE), 11.34);
    player_values.put(riotNames.get(SWIFTH), 11.37);
    player_values.put(riotNames.get(STAZZ), 11.93);
    player_values.put(riotNames.get(MELO), 7.91);
    player_values.put(riotNames.get(HINO), 8.5); 
    player_values.put(riotNames.get(HAMNISTIE), 14.2);
    player_values.put(riotNames.get(BARBIE), 14.95);
    player_values.put(riotNames.get(LUNIWA), 12.8);
    
    // mean calculation
    for (double i : player_values.values()) {
      avg += i;
    }
    avg /= player_values.size();
    
  }
  
  /**
  * Format the String input (a line with player names, basically) into
  * an array of PlayerGroup which can be used by the Balancer class.
  * Also takes a boolean input, it tells us if we are to take into account our
  * personal-table scores, or not (if not, then most probably we will only care for the Riot score).
  * @throws PlayerNbException: wrong number of players given (not a pair number) 
  * @throws IllegalStateException: a player given wasn't found in our list and our program exploded because of it. Congrats. ... Asshole.
  */
  public PlayerGroup[] format(String lineInput, boolean communityScore) throws PlayerException{
    this.init();
    
    String[] players = lineInput.split(" ");
    
    int player_number = players.length;
    if (player_number % 2 != 0) {
      throw new PlayerException();
    }
    
    // If communityScore is false, no value will be put in any of the PlayerGroup created
    int mask = (communityScore) ? 1 : 0;
    
    /*
    * The groups array will either contain, as player names :
    * - the community name (if known)
    * - the riot name (otherwise)
    */ 
    PlayerGroup[] groups = new PlayerGroup[players.length];
    
    for (int i = 0 ; i < players.length ; i++) {
      // First case : the player is one of the people known
      // by the software
      if (riotNames.containsKey(players[i])) {
        groups[i] = new PlayerGroup(riotNames.get(players[i]), player_values.get(riotNames.get(players[i])) * mask);
      }
      else {    
        PlayerGroup split_group = new PlayerGroup();
        String[] splitted = players[i].split(",");
        for (String s : splitted) {
          // Second case : known player associated with another one
          if (riotNames.containsKey(s)) {
            split_group.addPlayer(s, player_values.get(riotNames.get(s)) * mask,0);
          }
          else {
            // Third case : unknown player associated to a known one with an "="
            String[] equals_split = s.split("=");
            if(equals_split.length >= 2) {
              if (!riotNames.containsKey(equals_split[1])) throw new IllegalStateException();
              String formatedRiotName = equals_split[0].toLowerCase();
              double value = player_values.get(riotNames.get(equals_split[1]));
              player_values.put(formatedRiotName, value);
              split_group.addPlayer(formatedRiotName, value * mask, 0);
            }
            else {
              // Fourth and last case : complete unknown buddy, we consider
              // it's a riot nickname and give him the table's average as
              // value.        
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
}




