package ksp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import ksp.utilities.IDGenerator;
import ksp.utilities.RequestDirection;
import ksp.utilities.Team;

public class RequestTracker {
  static Team user;
  static String input = "❔   ";
  static String error = "⚠️    ";
  static String success = "✅️   ";
  static String info = "❕   ";
  static String prompt = ">>>   ";
  static RequestGraph requestGraph;


  /**
   * Application entry point.
   *
   * @param args application command line arguments
   */
  public static void main(String[] args) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("\uD83D\uDE80   Welcome to the Karman Space programme Request Tracker");
    System.out.println("      To the stars -idk what the motto is but imagine that here\n");
    System.out.println("""
        -   Press ctrl+c to exit the program at any point
        -   If you face any issues please contact Avaneesh Deleep or raise a github issue. Thanks :)

        ----------------------------------------------
        """);
    String name = null;
    while (name == null) {
      try {
        System.out.print(input + "Enter your team " + Arrays.toString(Team.values()) + ": " );
        name = reader.readLine().toUpperCase();
        user = Team.valueOf(name);
      } catch (IOException e) {
        System.out.println(error + "Oops...There was an error reading input.");
      } catch (Exception e) {
        System.out.println(error + "Invalid team.");
        name = null;
      }
    }
    System.out.println(success + "Session team assigned: " + name);
    //TODO: take user credentials for retrieving the graph and metadata
    System.out.println(info + "Loading requests and metadata.... ");
    requestGraph = new RequestGraph(new ArrayList<>());
    boolean successfulIDGenCreation = IDGenerator.init(0);
    System.out.println(success + "Initialised.");
    System.out.println(info + "Entering console mode...");
    System.out.println(info + "Type \"help\" to show options.");

    String line = null;
    while(true){
      try {
        System.out.print(prompt);
        line = reader.readLine().toUpperCase();
        if (!parse(line)){
          System.out.println(error + "Invalid command. Type help for command options.");
        }
      } catch (Exception e) {
        System.out.println(error + "Hmmmmm...something unexpected happened. "
            + "Please contact me (Avaneesh), with how you got this error, and I'll try to resolve it.");
      }
    }
  }

  private static boolean parse(String line){
    if (line.toLowerCase().equals("help")){
      showHelp();
      return true;
    } else if (line.equals("")){
      return true;
    }
    String command = line.split(" ")[0].toLowerCase();

    if (command.equals("display")){
      showGraph(line.split(" ")[1].toLowerCase());
      return true;
    }
    System.out.println(line);
    return false;
  }

  private static void showGraph(String type) {
    ArrayList<RequestNode> outputs = null;
    if (type.equals("all")){
      System.out.println(success + "All unresolved requests:\n");
      System.out.println(requestGraph);
    }
    try{
      int id = Integer.parseInt(type);
      System.out.println(success + "Request #"+id+":\n");
      RequestNode out = requestGraph.findRequest(id);
      if (out == null){
        System.out.println(error + "No matches found.");
      } else {
      System.out.println(requestGraph.findRequest(id));
      }
    } catch (Exception e) {
      switch (type) {
        case "sent" -> {
          System.out.println(success + "All unresolved requests sent by you:\n");
          outputs = requestGraph.findRequests(RequestDirection.FROM, user);
        }
        case "pending" -> {
          System.out.println(success + "All requests you still need to solve:\n");
          outputs = requestGraph.findRequests(RequestDirection.TO, user);
        }
        case "immediate" -> {
          System.out.println(success + "All requests that can be solved right now:\n");
          outputs = requestGraph.getImmediateProblems();
        }
      }
      if (outputs == null){
        System.out.println(error + "No matches found.");
      } else{
        for (RequestNode request : outputs) {
          System.out.println(request);
        }
      }
    }
  }

  private static void showHelp(){
    System.out.printf("""
        %sRequest Tracker Help Screen:
        
        help
             - Launches help screen.

                 
         request <query> <team> <(optional) ID of request that this solves for>
             - Creates a new request from your team to the team specified:
                 query: The question you want to ask - surround in double quote marks
                 team:  Team you want query. One of %s
                 ID:    If you are making this request to answer a request directed to
                        your team, enter it's ID here
                 
                 e.g. request "What's 1+(4+1)?" Systems
                 e.g. request "What's 1+5?" Sponsorship 3
        
        
        solve <ID> <solution>
             - Solves the request with ID specified. All involved parties will be notified
               via email, so there is nothing more to do from your end:
                 ID:       ID of request you are solving
                 solution: The answer to the request asked of you
                           (NOTE: solution is one line only. So please just answer in
                             one long paragraph. Sorry for any inconvenience)
                             
                 e.g. solve 3 "The diameter is 1m. CAD model here: *some teams link*. Thanks"
                 
                 
        display <type>
             - Shows the current request graph
                 type: One of [sent, pending, all, immediate, *ID*]
                        sent      - displays queries that you are waiting on responses from
                        pending   - displays queries you have yet to solve
                        all       - displays all unsolved requests
                        immediate - displays all unsolved requests, that can be solved right now!
                                    (i.e. they have no dependant requests they are waiting for)
                        ID        - displays the request with ID entered
                        
                 e.g. display immediate
                 e.g. display 67
                 e.g. display pending
        """, info, Arrays.toString(Team.values()));
  }
  //mvn clean compile && mvn package && (java -jar target/RequestTracker-1.0-SNAPSHOT-jar-with-dependencies.jar)

}
