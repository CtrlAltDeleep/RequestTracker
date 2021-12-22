package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import main.RequestBuilder;
import main.RequestNode;
import main.Team;
import main.exceptions.IllegalRequestException;
import org.junit.jupiter.api.Test;

public class RequestNodeTest {

  RequestNode newRequest = null;

  @Test
  public void NewRequestInitializationTest(){
    try {
      newRequest = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS).withQuery("How many CPUS you using?").build();
    } catch (IllegalRequestException e) {
      fail(e);
    }

    RequestNode newBranchRequest = null;
    try {
      newBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES).withQuery("Whats the diameter of the inner tube where the CPUs sit?").toSolve(newRequest).build();
    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertEquals(1, newRequest.getBranches().size());
    assertNull(newRequest.getSource());
    assertEquals(newRequest, newBranchRequest.getSource());

    System.out.println(newRequest);

  }

  @Test
  public void NewRootToStringTest(){
    assertEquals("Root Request #29792: How many CPUS you using?\n" + "\tWaiting on: []", newRequest.toString());
  }



}
