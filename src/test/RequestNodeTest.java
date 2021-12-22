package test;

import main.RequestNode;
import main.Team;
import main.exceptions.IllegalRequestException;
import org.junit.jupiter.api.Test;

public class RequestNodeTest {

  @Test
  public void NewRequestInitializationTest(){ //dummy placeholder test
    RequestNode newRequest = null;
    try {
      newRequest = new RequestNode(Team.SYSTEMS, Team.AVIONICS,"", null, null);
    } catch (IllegalRequestException e) {
      e.printStackTrace();
    }
    System.out.println(newRequest);
  }

}
