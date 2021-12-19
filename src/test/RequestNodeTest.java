package test;

import main.RequestNode;
import main.Team;
import org.junit.jupiter.api.Test;

public class RequestNodeTest {

  @Test
  public void NewRequestInitializationTest(){ //dummy placeholder test
    RequestNode newRequest = new RequestNode(Team.SYSTEMS, Team.AVIONICS,"", null, null);
    System.out.println(newRequest);
  }

}
