package balancer;

import java.util.HashSet;

public class Balancer {  
  /**
  * Maximum number of iterations in the final 
  * balancing process.
  */
  private final static int MAX_ITER = 10;
  
  /**
  * Maximum allowed difference between the
  * two teams' total power (defines the moment at which we stop iterating).
  */
  private final static int MAX_DIFF = 3;
  
  /**
  * Initial value for our minimum variable comrade.
  */
  private final static int UNREACHABLE_MIN = Integer.MAX_VALUE;
  
  private final boolean A = true;
  private final boolean B = false;
  
  private Team team_a;
  private Team team_b;
  
  public Balancer() {
    
  }
  
  /**
  * Main working method : makes the calculation, returns the
  * two teams, balanced and placed in an array. 
  */
  public Team[] run(PlayerGroup[] groups) {    
    team_a = new Team(groups.length/2);
    team_b = new Team(groups.length/2);
    
    // Start processing
    HashSet<Integer> selected_groups = new HashSet<>();
    Couple<Integer, Double> min_diff = new Couple<>(1, groups[1].getTotalValue());
    
    boolean turn = A;
    
    /*
    * This first loop takes care of the first stage in the balancing :
    * it is going to put every player (one by one) in the two teams, 
    * while trying to keep the power difference at a minimum value.
    */
    team_a.addPlayer(groups[0]);
    selected_groups.add(0);
    turn = B;
    
    while (selected_groups.size() < groups.length) {
      if (turn == A) {
        while (team_a.getPlayerNumber() <= team_b.getPlayerNumber() && selected_groups.size() < groups.length) {
          min_diff = new Couple<>(-1,Double.MAX_VALUE);
          for (int i = 1 ; i < groups.length ; i++) {
            if(!selected_groups.contains(i) && groups[i].getPlayers().keySet().size() + team_a.getPlayerNumber() <= team_a.getMaxSize()) {
              double abs_diff = Math.abs(team_b.getValue() - (team_a.getValue() + groups[i].getTotalValue()));
              if (abs_diff <= min_diff.getB()) {
                min_diff = new Couple<>(i, abs_diff);
              }
            }
          }
          team_a.addPlayer(groups[min_diff.getA()]);
          // System.out.println("Adding " + groups[min_diff.getA()].getNames()[0] + " to team A.");
          selected_groups.add(min_diff.getA());
        }
        turn = B;
      }
      else {
        while (team_b.getPlayerNumber() <= team_a.getPlayerNumber() && selected_groups.size() < groups.length) {
          min_diff = new Couple<>(-1,Double.MAX_VALUE);
          for (int i = 1 ; i < groups.length ; i++) {
            if(!selected_groups.contains(i) && groups[i].getPlayers().keySet().size() + team_b.getPlayerNumber() <= team_b.getMaxSize()) {
              double abs_diff = Math.abs(team_a.getValue() - (team_b.getValue() + groups[i].getTotalValue()));
              if (abs_diff <= min_diff.getB()) {
                min_diff = new Couple<>(i, abs_diff);
              }
            }
          }
          team_b.addPlayer(groups[min_diff.getA()]);
          // System.out.println("Adding " + groups[min_diff.getA()].getNames()[0] + " to team B.");
          selected_groups.add(min_diff.getA());
        }
        turn = A;
      }
    }
    
    /*
    * Last check : if there's more people in one of
    * the teams, put the lowest value members into the 
    * other team to compensate.
    */
    int team_difference = team_a.getPlayerNumber() - team_b.getPlayerNumber();
    while (team_difference < 0) {
      team_a.addPlayer(team_b.giveAwayWeakest());
      team_difference = team_a.getPlayerNumber() - team_b.getPlayerNumber();
    }
    while (team_difference > 0) {
      team_b.addPlayer(team_a.giveAwayWeakest());
      team_difference = team_a.getPlayerNumber() - team_b.getPlayerNumber();
    }
    
    /*
    * At this point, the first step is done. Teams are roughly
    * balanced, we want to improve this a bit. We are going
    * to take a look at every possible trade we can do between
    * the teams, and take the one that gives the best result 
    * (actually, the one that reduces the power difference the 
    * most). This step will be repeated until we have done it
    * a defined number of times (MAX_ITER), or until it gets to
    * a stabilized state (the difference between each team's power
    * is then inferior to the MAX_DIFF value). 
    */
    int nb_iter = 0;
    double team_diff = team_a.getValue() - team_b.getValue();
    
    while(Math.abs(team_diff) > MAX_DIFF && nb_iter < MAX_ITER) {
      double min = UNREACHABLE_MIN;
      Couple<Integer,Integer> min_index = new Couple<>(0,0);
      
      // Here we have an (nm) complexity, based on the number of players in both teams
      // (n for team 1, m for team 2). Careful not to use with big teams, would take ages.
      for (int i = 0 ; i < team_a.getPlayerNumber() ; i++) {
        if (!(team_a.get(i).getPlayers().keySet().size() > 1)) {
          for (int j = 0 ; j < team_b.getPlayerNumber() ; j++) {
            if (!(team_b.get(j).getPlayers().keySet().size() > 1)) {
              double player_diff = team_a.get(i).getTotalValue() - team_b.get(j).getTotalValue();  
              if (Math.abs(player_diff - (team_a.getValue() - team_b.getValue())/2) < min) {
                min = Math.abs(player_diff - (team_a.getValue() - team_b.getValue())/2);
                min_index = new Couple<>(i,j);
              }
            }
          }
        }
      }
      
      if(min >= Math.abs(team_diff) || min == UNREACHABLE_MIN) {
        /* In here, there are NO trade which would improve
        * the balancing. We get out, there are other battles
        * worth fighting for elsewhere.
        */
        break;
      }
      else {
        nb_iter++;
        PlayerGroup save = team_b.get(min_index.getB());
        team_b.replace(min_index.getB(), team_a.get(min_index.getA()));
        team_a.replace(min_index.getA(), save);
        team_diff = team_a.getValue() - team_b.getValue();
      }
    }
    
    Team[] result = new Team[2];
    result[0] = team_a;
    result[1] = team_b;
    return result;  
  }
}

