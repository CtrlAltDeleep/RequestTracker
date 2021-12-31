package ksp;

import static ksp.RequestBuilder.ANewRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ksp.exceptions.IllegalRequestException;
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
  static boolean run = true;


  /**
   * Application entry point.
   *
   * @param args TODO: to be used to specify to launch in gui mode
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
    requestGraph = new RequestGraph();
    System.out.println(success + "Initialised.");
    System.out.println(info + "Entering console mode...");
    System.out.println(info + "Type \"help\" to show options.");

    String line = null;
    while(run){
      try {
        System.out.print(prompt);
        line = reader.readLine();
        if (!parse(line)){
          System.out.println(error + "Invalid command. Type help for command options.");
        }
      } catch (Exception e) {
        System.out.println(error + "Debug log: " + e.getMessage());
        System.out.println(error + "Invalid command. Type help for command options.");
      }
    }
    exit();
  }

  private static boolean parse(String line){
    List<String> args = null;

    try{
      args = splitArgs(line);
    } catch (Exception e){
      System.out.println(error + "Could not parse buffer");
      System.out.println(error + "Debug Log: " + e.getMessage());
      return false;
    }

    if (args.isEmpty()){ //line skip
      return true;
    }

    String command = args.get(0);
    args.remove(0);

    if (command.equalsIgnoreCase("help")){
      showHelp();
      return true;
    } else if (command.equalsIgnoreCase("exit")){
      run = false;
      return true;
    }

    if (command.equalsIgnoreCase("display")){
      showGraph(args);
      return true;
    }

    if (command.equalsIgnoreCase("request")) {
      return request(args);
    }

    if (command.equalsIgnoreCase("solve")){
      return solve(args);
    }

    return false;
  }

  private static List<String> splitArgs(String args){
    List<String> argParts = new ArrayList<>();

    while (args.length() > 0){
      args = args.strip();
      if (args.startsWith("\"")){
        args = args.substring(1);
        String text = args.substring(0,args.indexOf("\""));
        argParts.add(text);
        args = args.substring(args.indexOf("\"")+1);
      } else{
        String arg = args.split(" ")[0];
        argParts.add(arg);
        args = args.substring(arg.length());
      }
    }

    return argParts;
  }

  private static boolean solve(List<String> args){
    RequestNode request = null;
    int id = 0;
    if (args.size() < 2){
      System.out.println(error + "Please provide a solution to the request.");
      return false;
    }
    try{
      id = Integer.parseInt(args.get(0));
    }catch (Exception e){
      System.out.println(error + "ID could not be parsed.");
      return false;
    }
    try{
      request = requestGraph.findRequest(id);
    }catch (Exception e){
      System.out.println(error + e.getMessage());
      return false;
    }
    if (request == null){
      System.out.println(error + "Request with ID #" + id + " not found.");
      return false;
    }

    requestGraph.resolveRequest(request,args.get(1));
    System.out.println(success + request + "\n" + success + "Marked as solved with solution: " + args.get(1));

    return true;
  }

  private static boolean request(List<String> args) {
    String query = args.get(0);

    Team requestee;
    try {
      requestee = Team.valueOf(args.get(1).toUpperCase());
    } catch (Exception e) {
      System.out.println(error + "Invalid Team.");
      return false;
    }
    RequestBuilder newRequest = ANewRequest(user, requestee)
        .inGraph(requestGraph)
        .withQuery(query);

    try {
      if (args.size() >= 3) {
        newRequest.toSolve(requestGraph.findRequest(Integer.parseInt(args.get(2))));
      }
    } catch (Exception e) {
      System.out.println(
          "Failed to find parse new request creation. Make sure id and team are valid");
      return false;
    }
    RequestNode node;
    try {
      node = newRequest.build();
      requestGraph.addNewRequest(node);
    } catch (IllegalRequestException e) {
      System.out.println(e.getMessage());
      return false;
    }

    System.out.println("\n" + success + "Added Request:");
    System.out.println(node + "\n");
    return true;
  }

  private static void exit() {
    System.out.println(info + "Saving Requests and metadata...");
    requestGraph.saveMetadata();
    requestGraph.saveData();
    System.out.println(success + "Saved and exited.");
  }

  private static void showGraph(List<String> args) {
    String type = args.get(0);
    ArrayList<RequestNode> outputs = null;

    if (type.equalsIgnoreCase("search")){ //search case
      if (args.size() <= 1){ //no search phrase
        System.out.println(error + "Provide a search term.");
      } else{

        outputs = requestGraph.findRequests(args.get(1));

        if (outputs == null || outputs.isEmpty()){
          System.out.println(error + "No matches found.\n");
        } else{
          System.out.println(success + "All matching requests, in order of relevance:\n");
          for (RequestNode request : outputs) {
            System.out.println(request + "\n");
          }
        }
      }
    }

    else if (type.equalsIgnoreCase("all")){ //whole graph case
      System.out.println(success + "All unresolved requests:\n");
      if (requestGraph.isEmpty()){
        System.out.println(success + "Wow! There seems to be no requests to solve! This is either really good, or I've messed up and we've lost our data... fingers crossed");
      }else{
        System.out.println(requestGraph);
      }
    }

    else{ //other cases
      try{
        int id = Integer.parseInt(type);
        System.out.println(success + "Request #"+id+":");
        RequestNode out = requestGraph.findRequest(id);
        if (out == null){
          System.out.println(error + "No matches found. For Request #" + id + "\n");
        } else {
        System.out.println(requestGraph.findRequest(id));
        }
      } catch (Exception e) {
        switch (type.toLowerCase()) {
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

        if (outputs == null || outputs.isEmpty()){
          System.out.println(error + "No matches found.\n");
        } else{
          for (RequestNode request : outputs) {
            System.out.println(request + "\n");
          }
        }
      }
    }

  }

  private static void showHelp(){
    System.out.printf("""
        %sRequest Tracker Help Screen:
        
        help
             - Launches help screen.
           
             
        exit
             - Closes program.

                 
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
                 
                 
        display <type> <(if type is search) search phrase>
             - Shows the current request graph
                 type: One of [sent, pending, all, immediate, *ID*, search]
                        sent      - displays queries that you are waiting on responses from
                        pending   - displays queries you have yet to solve
                        all       - displays all unsolved requests
                        immediate - displays all unsolved requests, that can be solved right now!
                                    (i.e. they have no dependant requests they are waiting for)
                        ID        - displays the request with ID entered
                        search    - searches for string provided in double quotes
                        
                 e.g. display immediate
                 e.g. display 67
                 e.g. display search "CPU"
        
        UNDER DEVELOPMENT: display-h which does the same as display but with historical request as well
        
        """, info, Arrays.toString(Team.values()));
  }
}


//mvn clean compile && mvn package && (java -jar target/RequestTracker-1.0-SNAPSHOT-jar-with-dependencies.jar)
/*
request "What are the dimensions of the cpu enclosure?" avionics
request "What material is the cpu enclosure made out of?" structures
request "What fuel are we using?" propulsion
request "Can I get a windows laptop?" sponsorship
request "What cpu are we using?" avionics
request "Is the teams doc up to date: https://imperiallondon.sharepoint.com/:p:/s/KarmanSpaceProgrammeWholeTeam-AE/ES3mbddTJZZOvtSLaef-e-UBzRSPoTGDZ_MoXlaaeJBXZg?e=sfqUF8?" sponsorship
 */