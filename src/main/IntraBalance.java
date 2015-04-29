package main;

import java.io.IOException;

import balancer.Balancer;
import balancer.NameFormater;
import balancer.PlayerGroup;
import balancer.RiotRequestSender;
import balancer.PerfScoreCalculator;
import balancer.TalkingMachine;

public class IntraBalance {
  
  private final static int MAX_ARGS = 5;
  
  public static void main(String[] args) {
    boolean perfScore = true;
    boolean communityScore = false;
    boolean initFromPath = false;
    boolean teamsFromPath = false;
    
    String valuesPath = "";
    String playersPath = "";
    
    // Handling arguments
    if (args.length > 0) {
      int i = 0;
      if (args.length > MAX_ARGS) {
        System.out.println("Wrong number of arguments, mate. There are " + args.length + " of these here, and I cannot have more than " + MAX_ARGS + ". So, thank you, by-bye.");
        System.exit(0);
      }
      while (i < args.length) {
        switch(args[i]) {
          case "-b" :
            System.out.println("\nWelcome. Launch mode : using both performance score and community score\n");
            communityScore = true;
            perfScore = true;
            i++;
            break;
            
          case "-c" :
            System.out.println("\nWelcome. Launch mode : only community score\n"); 
            perfScore = false;
            communityScore = true;
            i++;
            break;
            
          case "-f" : 
            if (i == args.length - 1) {
              System.out.println("Input error : -f option must be followed by a file path/name.");
              System.exit(0);
            }
            initFromPath = true;
            valuesPath = args[i+1];
            i += 2;
            break;
            
          case "-t" :
            if (i == args.length - 1) {
              System.out.println("Input error : -t option must be followed by a file path/name.");
              System.exit(0);
            }
            teamsFromPath = true;
            playersPath = args[i+1];
            i += 2;
            break;
            
          default :
            System.out.println("I don't know what you tried to give me, but that's obviously NOT something I'm allowed to take in. Show some respect, please, thank you.");
            System.exit(0);    
        }
      }
    }
    
    //Now we can put the workers to work. Onwaaaards, my slave army of Java objects !
    NameFormater nf = (initFromPath && communityScore) ? new NameFormater(valuesPath) : new NameFormater();  // If the community score is not used, there is ZERO point to read the values from the input file
    TalkingMachine tm = new TalkingMachine(System.out, nf.getPlayerValues().keySet());
    RiotRequestSender sender = new RiotRequestSender();
    Balancer balancer = new Balancer();
    PerfScoreCalculator superCalc = new PerfScoreCalculator(/*nf.getRiotNames(), */sender);
    
    /*
     * Here we have two different execution modes : the first one makes its calculation
     * once from a text file input. The second one keep asking for players to balance 
     * until the end of the universe OR until the user exits the application.
     */
    if (teamsFromPath) {
      try {
        PlayerGroup[] formatedInput = nf.formatFromFile(playersPath, communityScore, perfScore);
        if (perfScore) {
          formatedInput = superCalc.associateRiotScore(formatedInput);
        }
        tm.showResults(balancer.run(formatedInput));
      } 
      /*TODO: Please, please, please. Better exception handling, really.
      * We can get so many different IOExceptions from so many different objects that it is
      * quite impossible now to know what we should do with it.
      */
      catch(IOException e) {
        tm.sorryError(e.getMessage());
      }
      catch(IllegalArgumentException e) {  
        System.out.println(e.getMessage());
      }
      try {
        System.out.println("Please press Enter to leave this place forever ...");
        System.in.read();
      }
      catch (IOException e) {
        System.out.println("An irrelevant, ridiculous, unimportant error just happened. Hard to believe, "
            + "but well, you never know, right. Anyway, exiting now, bye.");
        System.exit(-1);
      }
    }
    else {
      tm.intro();  // Welcome message

      // Exit command is within TalkingMachine for now (... no comment, please), 
      // the user can get there by entering the correct input ("exit", namingly)
      // More probably, he'll merely close his terminal. Or well, let's just say
      // it's what 95% of the population would do. Rest are hipsters. ... Or testers.
      // (Well actually nevermind that, they're quite the same thing)
      while (true) {
        String players = tm.ask();
        try {
          PlayerGroup[] formatedInput = nf.format(players, communityScore, perfScore);
          if (perfScore) {
            formatedInput = superCalc.associateRiotScore(formatedInput);
          }
          tm.showResults(balancer.run(formatedInput));
        } 
        
        // TODO: exact same than twenty lines before. Thanks, cheers.
        catch(IOException e) {
          tm.sorryError(e.getMessage());
        }
        catch(IllegalArgumentException e) {  
          System.out.println(e.getMessage());
        }
      }
    }
  }
}




