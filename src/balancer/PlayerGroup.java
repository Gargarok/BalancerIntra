package balancer;

import java.util.HashMap;

/** 
* That's a group of players and we can add players inside. Woohooo !
*/
public class PlayerGroup {
  private HashMap<String,Double> players;
  private double total_value;
  private double comm_value;
  private double riot_value;
  
  public PlayerGroup(String player, double commValue) {
    players = new HashMap<>();
    players.put(player, commValue);
    total_value = commValue;
	comm_value = commValue;
	riot_value = 0;
  }
  
  public PlayerGroup() {
    players = new HashMap<>();
    total_value = 0;
	comm_value = 0;
	riot_value = 0;
  }
   
  public PlayerGroup(HashMap<String,Double> player) {
    players = player;
    total_value = 0;
	comm_value = 0;
    for (String s : player.keySet()) {
      total_value += player.get(s); 
	  comm_value += total_value;
    }
	riot_value = 0;
  }
  
  public void addPlayer(String player, double commValue, double riotValue) {
    players.put(player, commValue);
	this.comm_value += commValue;
	this.riot_value += riotValue;
    this.total_value += (commValue + riotValue);
  }
  
  public String[] getNames() {
    String[] names = new String[this.players.size()];
    int i = 0;
    for (String n : this.players.keySet()) {
      names[i] = n;
      i++;
    }
    return names;
  }
  
  public HashMap<String,Double> getPlayers() {
    return players;
  }
  
  public double getTotalValue() {
    return total_value;
  }
  
  public double getCommValue() {
	return comm_value;
  }
  
  public double getRiotValue() {
	return riot_value;
  }
  
  public void setRiotValue(double value) {
	riot_value = value;
	total_value += riot_value;
  }
}
