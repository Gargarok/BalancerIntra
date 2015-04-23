package balancer;

public class Team {
  private PlayerGroup[] players;
  private int current_index;
  private double totalvalue;
  
  public Team(int max_size) {
    players = new PlayerGroup[max_size];
    current_index = 0;
    totalvalue = 0;
  }
  
  public int getPlayerNumber() {
    return this.current_index;
  }
  
  public double getValue() {
    return this.totalvalue;
  }
  
  public int getMaxSize() {
    return this.players.length;
  }
  
  public PlayerGroup get(int i) {
	return this.players[i];
  }
  
  /**
  * You give me a playerGroup, and I'll take it while erasing the 
  * previous group at the i index. They were bad anyway.
  */
  public void replace(int i, PlayerGroup pg) {
	this.totalvalue = this.totalvalue - this.players[i].getTotalValue() + pg.getTotalValue();
    this.players[i] = pg;
  }
  
  /**
  * The weakest group I give you back here is ERASED from my team. Good to know, right ?
  */
  public PlayerGroup giveAwayWeakest() {
	PlayerGroup weakest = new PlayerGroup("", Double.MAX_VALUE);
	int j = 0;
    for (int i = 0 ; i < this.current_index ; i++) {
      if (this.players[i].getPlayers().keySet().size() == 1 && this.players[i].getTotalValue() < weakest.getTotalValue()) {
        weakest = this.players[i];
        j = i;
      }
    }
    
    for (int i = j ; i < this.current_index-1 ; i++) {
      this.players[i] = this.players[i+1];
    }
    this.current_index--;
    this.totalvalue -= weakest.getTotalValue();
    return weakest;
  }
  
  public void addPlayer(PlayerGroup pg) {
    this.players[this.current_index] = pg;
    this.totalvalue += pg.getTotalValue();
    this.current_index++;
  }
}
