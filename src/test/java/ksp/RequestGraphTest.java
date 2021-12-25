package ksp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import ksp.exceptions.IllegalRequestException;
import ksp.utilities.Team;
import org.junit.Test;

public class RequestGraphTest {
  RequestGraph requestGraph = new RequestGraph(new ArrayList<>());

  @Test
  public void addSingleRootRequestTest(){
    RequestNode newRootRequest = null ;
    RequestNode newBranchRequest = null;
    try {
      newRootRequest = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("How many CPUS are you using?")
          .build();
    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    try {
      newBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(newRootRequest).build();
    } catch (IllegalRequestException e) {
      fail(e.getMessage());
    }

    assertEquals("""
                    Root Request #1 from Systems to Avionics: How many CPUS are you using?
                    Waiting on:\s
                    	Branch Request #2 from Avionics to Structures: What's the diameter of the inner tube where the CPUs sit?
                    """,requestGraph.toString());
  }

  @Test
  public void addMultipleRootRequestTest(){
    RequestNode newRootRequest = null ;
    RequestNode newBranchRequest = null;
    RequestNode anotherRootRequest = null;
    RequestNode anotherAnotherRootRequest = null ;
    RequestNode anotherBranchRequest = null;

    try {
      newRootRequest = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("How many CPUS are you using?")
          .build();

      newBranchRequest = RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(newRootRequest).build();

      anotherRootRequest = RequestBuilder.ANewRequest(Team.PROPULSION,Team.SPONSORSHIP)
          .inGraph(requestGraph)
          .withQuery("What engine do we have money for?")
          .build();

      anotherBranchRequest = RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(anotherRootRequest).build();

      anotherAnotherRootRequest = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("Are we using carbon fibre?")
          .build();
    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    System.out.println(requestGraph);
    assertEquals("""
                    Root Request #1 from Systems to Avionics: How many CPUS are you using?
                    Waiting on:\s
                    	Branch Request #2 from Avionics to Structures: What's the diameter of the inner tube where the CPUs sit?
                    Root Request #3 from Propulsion to Sponsorships: What engine do we have money for?
                    Waiting on:\s
                    	Branch Request #4 from Sponsorships to Systems: Do you know any engine companies?
                    Root Request #5 from Systems to Structures: Are we using carbon fibre?
                    """,requestGraph.toString());
  }


  @Test
  public void searchUsingIDTest(){

    try {
          RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("How many CPUS are you using?")
          .build();

      RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(requestGraph.findRequest(1)).build();

      RequestBuilder.ANewRequest(Team.PROPULSION,Team.SPONSORSHIP)
          .inGraph(requestGraph)
          .withQuery("What engine do we have money for?")
          .build();

      RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(requestGraph.findRequest(3)).build();

      RequestBuilder.ANewRequest(Team.SYSTEMS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("Are we using carbon fibre?")
          .build();
    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    System.out.println(requestGraph);
    assertEquals("""
                    Root Request #1 from Systems to Avionics: How many CPUS are you using?
                    Waiting on:\s
                    	Branch Request #2 from Avionics to Structures: What's the diameter of the inner tube where the CPUs sit?
                    Root Request #3 from Propulsion to Sponsorships: What engine do we have money for?
                    Waiting on:\s
                    	Branch Request #4 from Sponsorships to Systems: Do you know any engine companies?
                    Root Request #5 from Systems to Structures: Are we using carbon fibre?
                    """,requestGraph.toString());
  }
}
