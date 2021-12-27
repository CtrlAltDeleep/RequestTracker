package ksp;

import java.util.Arrays;

public class RequestTracker {

  /**
   * Application entry point.
   *
   * @param args application command line arguments
   */
  public static void main(String[] args) {
    System.out.println(Arrays.toString(args));
    var app = new TrackerApp();
    app.run(args);
  }

  //mvn clean compile && mvn package && (java -jar target/RequestTracker-1.0-SNAPSHOT-jar-with-dependencies.jar)

}
