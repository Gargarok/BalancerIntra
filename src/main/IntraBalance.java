package main;

import java.io.IOException;

import balancer.Balancer;
import balancer.NameFormater;
import balancer.PlayerGroup;
import balancer.RiotRequestSender;
import balancer.RiotScoreCalculator;
import balancer.TalkingMachine;
import exception.PlayerException;
import exception.PlayerNameException;

public class IntraBalance {
  
  public static void main(String[] args) {
    boolean riotScore = true;
    boolean communityScore = true;
    
    // Handling arguments
    if (args.length > 0) {
      if (args.length > 1) {
        System.out.println("Wrong number of arguments, mate. It's one or none, thank you, by-bye.");
        System.exit(0);
      }
      switch (args[0]) {
        case "-b" :  // both
        System.out.println("\nWelcome. Launch mode : using both Riot score and community score\n");
        break;
        
        case "-c" :  // community
        System.out.println("\nWelcome. Launch mode : only Community score\n"); 
        riotScore = false;
        break;
        
        default:
        System.out.println("I don't know what you tried to give me, but that's obviously NOT something I'm allowed to take in. Show some respect, please, thank you.");
        System.exit(0);    
      }
    }
    else {
      System.out.println("\nWelcome. Launch mode : only Riot score\n");
      communityScore = false;
    }
    
    //Now we can put the workers to work. Onwaaaards, my slave army of Java objects !
    //TalkingMachine.talk();
    TalkingMachine tm = new TalkingMachine();
    NameFormater nf = new NameFormater();
    RiotRequestSender sender = new RiotRequestSender();
    Balancer balancer = new Balancer();
    RiotScoreCalculator superCalc = new RiotScoreCalculator(/*nf.getRiotNames(), */sender);
    
    tm.intro();  // Welcome message
    
    // Exit command is within TalkingMachine for now (... no comment, please), 
    // the user can get there by entering the correct input ("exit", namingly)
    // More probably, he'll merely close his terminal. Or well, let's just say
    // it's what 95% of the population would do. Rest are hipsters. ... Or testers.
    // (Well actually nevermind that, they're quite the same thing)
    while (true) {
      String players = tm.ask();
      try {
        PlayerGroup[] formatedInput = nf.format(players, communityScore);
        if (riotScore) {
          formatedInput = superCalc.associateRiotScore(formatedInput);
        }
        tm.showResults(balancer.run(formatedInput));
      } 
      //TODO: Make your own exceptions, I mean come on, IOException are used everywhere, you gonna catch something unwanted at some point
      // ... Who volunteers to do it ? 
      // (I knew it)
      // (Big talking, then nothing)
      catch(IOException e) {
        tm.sorryError(e.getMessage());
      }
      catch(PlayerException e) {  // typically these ones happens in the format() method, with a wrong number of players
        tm.inputError(); 
      }
      catch (PlayerNameException e) {
        System.out.println("PlayerNameException");
      }
      catch (IllegalStateException e) {
        tm.playerError();
      }
    }
  }
}

