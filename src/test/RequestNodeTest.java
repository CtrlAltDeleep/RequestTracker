package test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import main.RequestBuilder;
import main.RequestNode;
import main.utilities.IDGenerator;
import main.utilities.Team;
import main.exceptions.IllegalRequestException;
import org.junit.jupiter.api.Test;

public class RequestNodeTest {

  RequestNode newRequest = null;
  RequestNode newBranchRequest = null;
  boolean success = IDGenerator.init(10);

  @Test
  public void NewRequestInitializationTest(){
    try {
      newRequest = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS)
          .withQuery("How many CPUS are you using?")
          .build();
    } catch (IllegalRequestException e) {
      fail(e);
    }

    try {
      newBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES)
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
            Root Request #11 from Systems to Avionics: How many CPUS are you using?
            Waiting on:\s
            \tBranch Request #12 from Avionics to Structures: What's the diameter of the inner tube where the CPUs sit?""",
        newRequest.toString());
  }

  @Test
  public void InvalidBranchRequesterThrowsErrorTest(){
    NewRequestInitializationTest();

    assertThrows(
        IllegalRequestException.class,
        () -> RequestBuilder.ANewRequest(Team.STRUCTURES,Team.SPONSORSHIP)
                            .withQuery("This is unrelated. Why are you trying to add it here?")
                             .toSolve(newRequest).build()
    );
  }

  @Test
  public void CanRemoveTipRequests(){
    NewRequestInitializationTest();
    assertFalse(newRequest.isTip());
    newBranchRequest.removeRequest();
    assertTrue(newRequest.isTip());
  }

  @Test
  public void CanRemoveNonTipRequests(){
    NewRequestInitializationTest();
    try {
      RequestNode newBranchBranchRequest = RequestBuilder.ANewRequest(Team.STRUCTURES, Team.SYSTEMS)
          .withQuery("How fast are we expecting to go?")
          .toSolve(newBranchRequest)
          .build();
    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertFalse(newRequest.isTip());
    assertFalse(newBranchRequest.isTip());

    newBranchRequest.removeRequest();
    assertTrue(newRequest.isTip());
  }


  @Test
  public void InvalidSourceChangeThrowsErrorTest() {
    NewRequestInitializationTest();
    RequestNode anotherRootRequest = null;
    try {
      anotherRootRequest = RequestBuilder.ANewRequest(Team.SPONSORSHIP, Team.PROPULSION)
          .withQuery("Is it worth pursuing a Boeing sponsorship?")
          .build();
    } catch (IllegalRequestException e) {
      fail(e);
    }

    RequestNode finalAnotherRootRequest = anotherRootRequest;
    assertThrows(
        IllegalRequestException.class,
        () -> newBranchRequest.setSource(finalAnotherRootRequest)
    );
  }

  @Test
  public void ValidSourceChangeCorrectlyMergesBranchesAndUpstreamBranches() {
    NewRequestInitializationTest();
    RequestNode anotherRootRequest = null;
    RequestNode anotherBranchRequest = null;

    try {
      anotherRootRequest = RequestBuilder.ANewRequest(Team.SPONSORSHIP, Team.AVIONICS)
          .withQuery("What company are you buying the CPU from?")
          .build();
      anotherBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS, Team.SYSTEMS)
          .withQuery("How high are we going?")
          .toSolve(anotherRootRequest)
          .build();

      assertEquals(1,anotherRootRequest.getBranches().size());
      newBranchRequest.setSource(anotherRootRequest);
      assertEquals(anotherRootRequest,newBranchRequest.getSource());
    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertTrue(newRequest.isTip());
    System.out.println(anotherRootRequest);
    assertEquals(2,anotherRootRequest.getBranches().size());
  }

  @Test
  public void SourceChangeOnARootRemovesItAsARoot() {
    NewRequestInitializationTest();
    RequestNode anotherRootRequest = null;

    try {
      anotherRootRequest = RequestBuilder.ANewRequest(Team.AVIONICS, Team.SPONSORSHIP)
          .withQuery("Do we have any carbon fibre sponsors?")
          .build();
      assertTrue(anotherRootRequest.isRoot());
      anotherRootRequest.setSource(newRequest);
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
      newBranchRequest.setSource(null);
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
          .withQuery("Is it worth pursuing a Boeing sponsorship?")
          .build();
    } catch (IllegalRequestException e) {
      fail(e);
    }

    RequestNode finalBadBranchRequest = badBranchRequest;
    assertThrows(
        IllegalRequestException.class,
        () -> newBranchRequest.addBranch(finalBadBranchRequest)
    );
  }

  @Test
  public void ValidBranchAdditionFromRootCorrectlyAdds(){
    NewRequestInitializationTest();
    RequestNode anotherBranchRequest;
    try {
      anotherBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS, Team.SYSTEMS)
          .withQuery("How high are we going - do our CPUs have to worry about temperature?")
          .build();

      assertEquals(1,newRequest.getBranches().size());
      assertTrue(anotherBranchRequest.isRoot());

      newRequest.addBranch(anotherBranchRequest);

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
          .withQuery("What company are you buying the CPU from?")
          .build();
      anotherBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS, Team.SYSTEMS)
          .withQuery("How high are we going?")
          .toSolve(anotherRootRequest)
          .build();

      assertEquals(1,newRequest.getBranches().size());
      assertFalse(anotherRootRequest.isTip());

      newRequest.addBranch(anotherBranchRequest);

      assertTrue(anotherRootRequest.isTip());
      assertEquals(newRequest,anotherBranchRequest.getSource());

    } catch (IllegalRequestException e) {
      fail(e);
    }
    assertEquals(2,newRequest.getBranches().size());
  }
}
