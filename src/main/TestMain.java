package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;

import balancer.PlayerGroup;
import balancer.RiotRequestSender;
import balancer.RiotScoreCalculator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
      /*
      String result = sender.debugIWantToSeeThisResult(new long[] {21847350});
      File file = new File("RequestResult.txt");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(result);
			bw.close();
      */
      //  sender.debugRegexRecent(21847350);
      //  sender.debugRegexId(new String[]{"anjalï"});
      //  sender.debugRegexTotal(21847350);
      //  sender.debugNameById(64392993);
      
      
      //PlayerGroup pg = new PlayerGroup("Barbie", 0);
      //PlayerGroup pg = new PlayerGroup("Vico", 0);
      PlayerGroup pg = new PlayerGroup("Anjali", 0);
      PlayerGroup[] group = new PlayerGroup[]{pg};
      RiotScoreCalculator rsc = new RiotScoreCalculator(sender);
      group = rsc.associateRiotScore(group);
      //System.out.println("Score de Barbie : " + group[0].getPerfValue());
      //System.out.println("Score de Vico : " + group[0].getPerfValue());
      System.out.println("Score de Anjali : " + group[0].getPerfValue());
      for (String s : rsc.getRecentStats().keySet()) {
        System.out.println(s + " : " + rsc.getRecentStats().get(s));
      }
      for (String s : rsc.getTotalStats().keySet()) {
        System.out.println(s + " : " + rsc.getTotalStats().get(s));
      }      
    }
    catch (IOException e) {
      System.exit(-1);
    } 
    catch (PlayerNameException e) {
        
    }
  }
}




