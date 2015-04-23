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

public class TestMain {
  
  public static void main(String[] args) {
    
    RiotRequestSender sender = new RiotRequestSender();
    // Ago : 24757055
    // Sako : 20730417
    // Snakeuh : 24552188
    // Garga : 139679
    // Waun : 19270300
    // Delzur : 46816246
    // Drahl : 32601942
    // Trolol : 28930672
    // Vıco : 21847350
    // DD : 21576300
    // Barbie : 29691434
    // Anjali : 33857021
    try {
      sender.debugRegexRecent(21847350);
      //  sender.debugRegexId(new String[]{"anjalï"});
      //  sender.debugRegexTotal(21847350);
      //  sender.debugNameById(64392993);
      
      /*
      //PlayerGroup pg = new PlayerGroup("Barbie", 0);
      //PlayerGroup pg = new PlayerGroup("Vico", 0);
      PlayerGroup pg = new PlayerGroup("Anjali", 0);
      PlayerGroup[] group = new PlayerGroup[]{pg};
      RiotScoreCalculator rsc = new RiotScoreCalculator(sender);
      group = rsc.associateRiotScore(group);
      //System.out.println("Score de Barbie : " + group[0].getRiotValue());
      //System.out.println("Score de Vico : " + group[0].getRiotValue());
      System.out.println("Score de Anjali : " + group[0].getRiotValue());
      for (String s : rsc.getRecentStats().keySet()) {
        System.out.println(s + " : " + rsc.getRecentStats().get(s));
      }
      for (String s : rsc.getTotalStats().keySet()) {
        System.out.println(s + " : " + rsc.getTotalStats().get(s));
      }
      catch (PlayerNameException e) {
        
      }
      */    
    }
    catch (IOException e) {
      System.exit(-1);
    } 
  }
}



