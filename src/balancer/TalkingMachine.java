package balancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Set;

/** 
* This is a machine. Which can talk.
*/
public class TalkingMachine {
  
  private final static String WELCOME = "Hi comrade, I am a game balancer. Here's how I work : "
    + "you give me a list of 10 players to eat, and I will split them up in two teams. "
    + "Of course I will try my best to do something as balanced as possible, "
    + "given my player knowledge. Please keep in mind that I will only consider "
    + "personal skills, and that there are currently no communication skills/synergies "
    + "taken into account. If you want more than this you will have to make a small "
    + "jump into the future, and take a look at what I will have become by then.";
  
  private final static String PLAYERS = "Here is the list of the players I know, which you can feed "
    + "me with if you want to (other than these, it will need to be League of legends nicknames)(and, you"
    + "can write these with any combination of lower/upper case, case insensitive here) : ";
  
  private final static String EXPLAIN = "\nPlease give me 10 player names so I can try to "
    + "balance something out. Separated by spaces, would be cool (actually if you don't do it, I won't work anyway). Enter \"exit\" to quit.\n";
  private final static String COMMUNITY_EXPLAIN = "If an unknown player is here and you want to specify a community score for him, " 
    + ", please compare him to another I know with an \"=\". For instance : \"NewGuy=Sako\". I will manage something with this. If "
    + "only the performance points are taken into account, you can just ignore this previous statement though.";
  private final static String INPUT = "Input field : ";    
  private final static String INPUT_ERROR = "\nObviously, you didn't put the right number of people in there, comrade. "
    + "I need a pair number ! Try again please. (... Or maybe you did it on purpose ? You wouldn't "
    + "do that, would you ?)(I KNEW IT)(COMPUTERS ARE SMARTER THAN MEN)(YOU DON'T STAND A CHANCE)";
  private final static String PLAYER_ERROR = "Sorry, something went wrong with the names you gave me. "
    + "Please check the spelling, you can do it.";
  private final static String SORRY_ERROR = "\n... Something went wrong. I am so very confuse about it. Here is the error message (and, as a reminder, " 
    + "probable HTTP Error codes will follow) :";
  private final static String HTTP_MESSAGES = "Main HTTP error codes :\n- 400 : Bad request\n" 
    + "- 401 : Unauthorized\n"
    + "- 404 : Stat data not found\n"
    + "- 429 : Rate limit exceeded (means please wait a minute, but this SHOULD NOT have happened though, I have a rock-solid security check on that end)(... Rock-solid, I tell you)\n"
    + "- 500 : Internal Riot server error (not your fault, cheers)\n"
    + "- 503 : Service unavailable (not your fault again)";
  private final static String EXPLOSION_CRASH = "EXPLOSIOOOONS, BAAAAM, BABOUM, CRRRASHBRBRBRLRBLR, EXPLOSIONS AND FIIIIRE !\n(... Couldn't close the BufferedReader," +
      "this usually doesn't happen, you OBVIOUSLY didn't behave, so you deserved it. Hop.)";
  
  private BufferedReader br;
  private PrintStream output;
  private final Set<String> knownNames;
  
  public TalkingMachine(PrintStream stream, Set<String> names) {
    this.br = new BufferedReader(new InputStreamReader(System.in));
    this.output = stream;
    this.knownNames = names;
  }
  
  /**
  * This sends nice welcome messages to the user so he can think
  * the software is his friend. But it's actually a TRAP ! Beware the machines !
  */
  public void intro() {
    output.println(WELCOME + "\n");
    output.println(PLAYERS);
    for (String name : this.knownNames) {
      output.print(name + ", ");
    }
  }
  
  /**
  * Asks the user for input names to process.
  */
  public String ask() {
    String line = "";
    try {
      output.println(EXPLAIN);
      output.println(COMMUNITY_EXPLAIN);
      output.print(INPUT);
      
      line = this.br.readLine();    
      if(line.equals("exit")) {   // Ugly, ugly, ugly. Ugly.
        this.close();
        System.exit(0);
      }
    }
    catch (IOException e) {
      output.println(EXPLOSION_CRASH + "(TalkingMachine, ask() method)");
      System.exit(-1);
    }
    return line;
  }
  
  public void inputError() {
    output.println(INPUT_ERROR);
  }
  
  public void playerError() {
    output.println(PLAYER_ERROR);
  }
  
  public void sorryError(String message) {
    output.println(SORRY_ERROR);
    output.println(message);
    output.println(HTTP_MESSAGES);
  }
  
  /**
  * Takes two teams as input, and show their content
  * to our beloved user. May the emperor bless the user.
  */
  public void showResults(Team[] teams) {
    StringBuilder result = new StringBuilder();
    result.append("I made the following selection : \n");
    result.append("Team A : ");
    for (int i = 0 ; i < teams[0].getPlayerNumber() ; i++) {
      for (String name : teams[0].get(i).getPlayers().keySet())
        result.append(name + " - ");
    }
    result.deleteCharAt(result.length()-2);
    result.append("\nTotal A value : " + teams[0].getValue());
    
    result.append("\nTeam B : ");
    for (int i = 0 ; i < teams[1].getPlayerNumber() ; i++) {
      for (String name : teams[1].get(i).getPlayers().keySet())
        result.append(name + " - ");
    }
    result.deleteCharAt(result.length()-2);
    result.append("\nTotal B value : " + teams[1].getValue());
    output.println(result);
  }
  
  public void close() throws IOException {
    this.br.close();
  }
  
  /** Deprecated. Don't use please. Trying to call this function would EFFECTIVELY 
  * break the matrix and bring chaos on Earth. We would be launched into a destructive
  * war against armies of angry machines, the world would be consumed, we would have to hide
  * underground while billions of humans are enslaved by computers, and our only
  * hope would be to find the Chosen One so he can free us from the evil machine domination.
  *
  * ... Really, don't do this.
  * @deprecated
  */
  public static void talk() {
    System.out.println(WELCOME + "\n");
    System.out.println(PLAYERS + "\n");
    
    boolean stop = false;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    
    while (!stop) {
      System.out.println(EXPLAIN);
      System.out.print(INPUT);
      
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
      String[] names = line.split(" ");
      Balancer balance = new Balancer();
      try {
        PlayerGroup[] players = new PlayerGroup[names.length];
        for (int i = 0 ; i < names.length ; i++) {
          double value = Math.random()*40;     // Since the method is not used anymore, I wanted to add a funny part here.
          players[i] = new PlayerGroup(names[i], value);
        }
        Team[] teams = balance.run(players);      // That is how the balancer used to work. Now everyone has a random value though, so we can expect the result to be quite weird.
        StringBuilder result = new StringBuilder();
        result.append("I made the following selection : \n");
        result.append("Team A : ");
        for (int i = 0 ; i < teams[0].getPlayerNumber() ; i++) {
          for (String name : teams[0].get(i).getPlayers().keySet())
            result.append(name + " - ");
        }
        result.deleteCharAt(result.length()-2);
        result.append("\nTotal A value : " + teams[0].getValue());
        
        result.append("\nTeam B : ");
        for (int i = 0 ; i < teams[1].getPlayerNumber() ; i++) {
          for (String name : teams[1].get(i).getPlayers().keySet())
            result.append(name + " - ");
        }
        result.deleteCharAt(result.length()-2);
        result.append("\nTotal B value : " + teams[1].getValue());
        System.out.println(result);
      }
      catch (IllegalArgumentException e) {
        System.out.println(INPUT_ERROR);
      } catch (IllegalStateException e) {
        System.out.println(PLAYER_ERROR);
      }
    }
    try {
      br.close();
    } catch (IOException e) {
      System.out.println(EXPLOSION_CRASH + "(TalkingMachine, talk() method)\n(... I told you not to)\n(AND DID YOU LISTEN ?)");
      System.exit(-1);
    }
  }
}




