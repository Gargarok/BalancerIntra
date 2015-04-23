package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;

import balancer.PlayerGroup;
import balancer.RiotRequestSender;
import balancer.RiotScoreCalculator;
import balancer.TalkingMachine;

import exception.PlayerNameException;

/**
* Basically this is a debugging main for the IntraBalancer,
* which lets us take a look at the Riot score calculation alone.
* It's still pretty funny to use though, but I think the code will be kinda ugly,
* I don't intend to spend much time on this here, thank you for your understanding,
* happy birthday.
*/
public class SoloRiotScore {
  
  public static void main(String[] args) {
    RiotRequestSender sender = new RiotRequestSender();
    PlayerGroup[] result;
    RiotScoreCalculator calculator = new RiotScoreCalculator(sender);
    
    while(true) { 
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("\nPlease go ahead and give me some name (a league of legends nick, actually), so I can feed on it : ");
      String line = ""; 
      try {
        line = br.readLine();
      } catch (IOException e) {
        System.out.println("\nError reading your line, I am sorry but I must terminate now. This was fatal to me.\n");
        System.exit(-1);
      }    
      if(line.equals("exit")) {
        break;
      }
      
      PlayerGroup gp = new PlayerGroup();
      gp.addPlayer(line,0,0);
      HashMap<String,String> name = new HashMap<>();
      name.put(line, line);
      
      
      try {
        result = calculator.associateRiotScore(new PlayerGroup[]{gp});
        
        HashMap<String,Double> recentStats = calculator.getRecentStats();
        HashMap<String,Double> totalStats = calculator.getTotalStats();
        System.out.println("Total recent score : " + recentStats.get("Total recent score"));
        //System.out.println("Total normal score : " + totalStats.get("Total normal score"));
        System.out.println("Total ranked score : " + totalStats.get("Total ranked score"));
        System.out.println("Total experience score : " + totalStats.get("Total experience score"));
        System.out.println("Global riot score : " + gp.getRiotValue());
        
        System.out.println("\nRECENT STATS : ");
        for (String field : recentStats.keySet()) {
          if (field != "Total recent score" && recentStats.get(field) != 0)
          System.out.println(field + " : " + recentStats.get(field));
        }  
        System.out.println("--------------");
        
        System.out.println("TOTAL STATS : ");
        for (String field : totalStats.keySet()) {
          if (field != "Total experience score" && field != "Total ranked score" && totalStats.get(field) != 0)
          System.out.println(field + " : " + totalStats.get(field));
        }
        System.out.println("--------------");
      }
      catch (IOException e) {
        System.out.println("\nIOException while calculating : you broke everything, THANK YOU VERY MUCH. (Are you even connected to the internet ? Are the HTTP requests allowed ?)\n");
        System.out.println(e.getMessage());
        System.exit(-1);
      }
      catch (PlayerNameException e) {
        System.out.println("\nPlayerNameException while calculating. As a result : \n - Either you mispelled the nickname or wrote nonsense on my input (... Bastard)\n - Either the player renamed (...Ago)\n - Either the player does not exist on this server (euw)");
        //System.exit(-2);
      }
    }    
  }
}

