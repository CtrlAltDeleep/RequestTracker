package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import main.RequestBuilder;
import main.RequestGraph;
import main.RequestNode;
import main.exceptions.IllegalRequestException;
import main.utilities.IDGenerator;
import main.utilities.Team;
import org.junit.jupiter.api.Test;


public class RequestNodeTest {

  RequestNode newRequest = null;
  RequestNode newBranchRequest = null;
  RequestGraph requestGraph = new RequestGraph(new ArrayList<>());

  @Test
  public void NewRequestInitializationTest(){
    try {
      newRequest = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("How many CPUS are you using?")
          .build();
    } catch (IllegalRequestException e) {
      fail(e);
    }

    try {
      newBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(newRequest).build();
    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertEquals(1, newRequest.getBranches().size());
    assertNull(newRequest.getSource());
    assertEquals(newRequest, newBranchRequest.getSource());
  }

  @Test
  public void ToStringTest(){
    NewRequestInitializationTest();
    assertEquals(
        """
            Root Request #1 from Systems to Avionics: How many CPUS are you using?
            Waiting on:\s
            \tBranch Request #2 from Avionics to Structures: What's the diameter of the inner tube where the CPUs sit?""",
        newRequest.toString());
  }

  @Test
  public void InvalidBranchRequesterThrowsErrorTest(){
    NewRequestInitializationTest();

    assertThrows(
        IllegalRequestException.class,
        () -> RequestBuilder.ANewRequest(Team.STRUCTURES,Team.SPONSORSHIP)
                            .inGraph(requestGraph)
                            .withQuery("This is unrelated. Why are you trying to add it here?")
                            .toSolve(newRequest).build()
    );
  }

  @Test
  public void CanRemoveTipRequests(){
    NewRequestInitializationTest();
    assertFalse(newRequest.isTip());
    newBranchRequest.removeRequest(requestGraph);
    assertTrue(newRequest.isTip());
  }

  @Test
  public void CanRemoveNonTipRequests(){
    NewRequestInitializationTest();
    try {
      RequestNode newBranchBranchRequest = RequestBuilder.ANewRequest(Team.STRUCTURES, Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("How fast are we expecting to go?")
          .toSolve(newBranchRequest)
          .build();
    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertFalse(newRequest.isTip());
    assertFalse(newBranchRequest.isTip());

    newBranchRequest.removeRequest(requestGraph);
    assertTrue(newRequest.isTip());
  }


  @Test
  public void InvalidSourceChangeThrowsErrorTest() {
    NewRequestInitializationTest();
    RequestNode anotherRootRequest = null;
    try {
      anotherRootRequest = RequestBuilder.ANewRequest(Team.SPONSORSHIP, Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("Is it worth pursuing a Boeing sponsorship?")
          .build();
    } catch (IllegalRequestException e) {
      fail(e);
    }

    RequestNode finalAnotherRootRequest = anotherRootRequest;
    assertThrows(
        IllegalRequestException.class,
        () -> newBranchRequest.setSource(finalAnotherRootRequest,requestGraph)
    );
  }

  @Test
  public void ValidSourceChangeCorrectlyMergesBranchesAndUpstreamBranches() {
    NewRequestInitializationTest();
    RequestNode anotherRootRequest = null;
    RequestNode anotherBranchRequest;

    try {
      anotherRootRequest = RequestBuilder.ANewRequest(Team.SPONSORSHIP, Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("What company are you buying the CPU from?")
          .build();
      anotherBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS, Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("How high are we going?")
          .toSolve(anotherRootRequest)
          .build();

      assertEquals(1,anotherRootRequest.getBranches().size());
      newBranchRequest.setSource(anotherRootRequest,requestGraph);
      assertEquals(anotherRootRequest,newBranchRequest.getSource());
      assertEquals(anotherRootRequest,anotherBranchRequest.getSource());

    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertTrue(newRequest.isTip());
    System.out.println(anotherRootRequest);
    System.out.println(newRequest);
    assertEquals(2,anotherRootRequest.getBranches().size());
  }

  @Test
  public void SourceChangeOnARootRemovesItAsARoot() {
    NewRequestInitializationTest();
    RequestNode anotherRootRequest = null;

    try {
      anotherRootRequest = RequestBuilder.ANewRequest(Team.AVIONICS, Team.SPONSORSHIP)
          .inGraph(requestGraph)
          .withQuery("Do we have any carbon fibre sponsors?")
          .build();
      assertTrue(anotherRootRequest.isRoot());
      anotherRootRequest.setSource(newRequest, requestGraph);
    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertFalse(anotherRootRequest.isRoot());
  }


  @Test
  public void SourceChangeOnABranchCanSetItAsARoot() {
    NewRequestInitializationTest();
    assertFalse(newRequest.isTip());
    assertFalse(newBranchRequest.isRoot());
    try {
      newBranchRequest.setSource(null, requestGraph);
    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertTrue(newRequest.isTip());
    assertTrue(newBranchRequest.isRoot());
  }

  @Test
  public void InvalidBranchAdditionThrowsErrorTest(){
    NewRequestInitializationTest();
    RequestNode badBranchRequest = null;
    try {
      badBranchRequest = RequestBuilder.ANewRequest(Team.SPONSORSHIP, Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("Is it worth pursuing a Boeing sponsorship?")
          .build();
    } catch (IllegalRequestException e) {
      fail(e);
    }

    RequestNode finalBadBranchRequest = badBranchRequest;
    assertThrows(
        IllegalRequestException.class,
        () -> newBranchRequest.addBranch(finalBadBranchRequest, requestGraph)
    );
  }

  @Test
  public void ValidBranchAdditionFromRootCorrectlyAdds(){
    NewRequestInitializationTest();
    RequestNode anotherBranchRequest;
    try {
      anotherBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS, Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("How high are we going - do our CPUs have to worry about temperature?")
          .build();

      assertEquals(1,newRequest.getBranches().size());
      assertTrue(anotherBranchRequest.isRoot());

      newRequest.addBranch(anotherBranchRequest, requestGraph);

      assertFalse(anotherBranchRequest.isRoot());

    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertEquals(2,newRequest.getBranches().size());

  }

  @Test
  public void ValidBranchAdditionFromBranchCorrectlyAdds(){
    NewRequestInitializationTest();
    RequestNode anotherRootRequest;
    RequestNode anotherBranchRequest;
    try {
      anotherRootRequest = RequestBuilder.ANewRequest(Team.SPONSORSHIP, Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("What company are you buying the CPU from?")
          .build();
      anotherBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS, Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("How high are we going?")
          .toSolve(anotherRootRequest)
          .build();

      assertEquals(1,newRequest.getBranches().size());
      assertFalse(anotherRootRequest.isTip());

      newRequest.addBranch(anotherBranchRequest, requestGraph);

      assertTrue(anotherRootRequest.isTip());
      assertEquals(newRequest,anotherBranchRequest.getSource());

    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertEquals(2,newRequest.getBranches().size());
  }
}
