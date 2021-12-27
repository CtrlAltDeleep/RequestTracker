package ksp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ksp.exceptions.IllegalRequestException;
import ksp.utilities.IDGenerator;
import ksp.utilities.RequestDirection;
import ksp.utilities.Team;
import org.junit.Test;

public class RequestGraphTest {
  RequestGraph requestGraph = new RequestGraph(new ArrayList<>());
  boolean successfulIDGenCreation = IDGenerator.init(0);

  @Test
  public void addingASingleRootRequestCorrectlyLinksToGraph(){
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
  public void addingMultipleRootRequestsCorrectlyLinksToGraph(){
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
  public void searchUsingIDReturnsCorrectRequest(){

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
  public void searchUsingRequesterReturnsCorrectRequests(){

    try {
      RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("How many CPUS are you using?")
          .build();

      RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(requestGraph.findRequests(RequestDirection.FROM, Team.SYSTEMS).get(0))
          .build();

      RequestBuilder.ANewRequest(Team.PROPULSION,Team.SPONSORSHIP)
          .inGraph(requestGraph)
          .withQuery("What engine do we have money for?")
          .build();

      RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(requestGraph.findRequests(RequestDirection.FROM, Team.PROPULSION).get(0))
          .build();

      RequestBuilder.ANewRequest(Team.SYSTEMS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("Are we using carbon fibre?")
          .build();
    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

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
  public void searchUsingRequesteeReturnsCorrectRequests(){

    try {
      RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("How many CPUS are you using?")
          .build();

      RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(requestGraph.findRequests(RequestDirection.TO, Team.AVIONICS).get(0))
          .build();

      RequestBuilder.ANewRequest(Team.PROPULSION,Team.SPONSORSHIP)
          .inGraph(requestGraph)
          .withQuery("What engine do we have money for?")
          .build();

      RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(requestGraph.findRequests(RequestDirection.TO, Team.SPONSORSHIP).get(0))
          .build();

      RequestBuilder.ANewRequest(Team.SYSTEMS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("Are we using carbon fibre?")
          .build();
    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

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
  public void addingABranchFromExistingRequestUpdatesGraph(){
    RequestNode mainNode = null;
    RequestNode subNode = null;

    try {
      mainNode = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("What fuel ratio are we using?")
          .build();

      RequestBuilder.ANewRequest(Team.PROPULSION,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(mainNode)
          .build();

      subNode = RequestBuilder.ANewRequest(Team.PROPULSION,Team.SPONSORSHIP)
          .inGraph(requestGraph)
          .withQuery("What engine do we have money for?")
          .build(); // unlinked - setup as a root

      RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(subNode)
          .build();

      RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(subNode)
          .build();

      mainNode.addBranch(subNode,requestGraph);

    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    assertEquals("""
                   Root Request #1 from Systems to Propulsion: What fuel ratio are we using?
                   Waiting on:\s
                   	Branch Request #2 from Propulsion to Structures: What's the diameter of the inner tube where the CPUs sit?
                   	Branch Request #3 from Propulsion to Sponsorships: What engine do we have money for?
                   	Waiting on:\s
                   		Branch Request #4 from Sponsorships to Systems: Do you know any engine companies?
                   		Branch Request #5 from Sponsorships to Propulsion: Do you know any engine companies?
                    """,requestGraph.toString());
  }

  @Test
  public void settingTheSourceToAExistingRequestUpdatesGraph(){
    RequestNode mainNode = null;
    RequestNode subNode = null;

    try {
      mainNode = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("What fuel ratio are we using?")
          .build();

      RequestBuilder.ANewRequest(Team.PROPULSION,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(mainNode)
          .build();

      subNode = RequestBuilder.ANewRequest(Team.PROPULSION,Team.SPONSORSHIP)
          .inGraph(requestGraph)
          .withQuery("What engine do we have money for?")
          .build(); // unlinked - setup as a root

      RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(subNode)
          .build();

      RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(subNode)
          .build();

      subNode.setSource(mainNode,requestGraph);

    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    assertEquals("""
                   Root Request #1 from Systems to Propulsion: What fuel ratio are we using?
                   Waiting on:\s
                   	Branch Request #2 from Propulsion to Structures: What's the diameter of the inner tube where the CPUs sit?
                   	Branch Request #3 from Propulsion to Sponsorships: What engine do we have money for?
                   	Waiting on:\s
                   		Branch Request #4 from Sponsorships to Systems: Do you know any engine companies?
                   		Branch Request #5 from Sponsorships to Propulsion: Do you know any engine companies?
                    """,requestGraph.toString());
  }

  @Test
  public void settingTheSourceToNullUpdatesGraph(){
    RequestNode newRoot = null;
    RequestNode root = null;

    try {
      root = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("What fuel ratio are we using?")
          .build();

      newRoot = RequestBuilder.ANewRequest(Team.PROPULSION,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("Something unrelated...oops, i hope we can move it")
          .toSolve(root)
          .build();

      newRoot.setSource(null,requestGraph);

    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    assertEquals("""
                   Root Request #1 from Systems to Propulsion: What fuel ratio are we using?
                   
                   Root Request #2 from Propulsion to Structures: Something unrelated...oops, i hope we can move it
                    """,requestGraph.toString());
  }

  @Test
  public void resolvingTipRequestUpdatesGraph(){
    RequestNode tip = null;
    RequestNode root = null;

    try {
      root = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("What fuel ratio are we using?")
          .build();

      tip = RequestBuilder.ANewRequest(Team.PROPULSION,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What max speed can we handle?")
          .toSolve(root)
          .build();

    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    requestGraph.resolveRequest(tip, "1 m/s ... if we throw it real hard");

    assertEquals("""
                   Root Request #1 from Systems to Propulsion: What fuel ratio are we using?
                    """,requestGraph.toString());
  }

  @Test
  public void resolvingRootRequestUpdatesGraph(){
    RequestNode tip = null;
    RequestNode root = null;

    try {
      root = RequestBuilder.ANewRequest(Team.SYSTEMS,Team.PROPULSION)
          .inGraph(requestGraph)
          .withQuery("What fuel ratio are we using?")
          .build();

      tip = RequestBuilder.ANewRequest(Team.PROPULSION,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What max speed can we handle?")
          .toSolve(root)
          .build();

    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    requestGraph.resolveRequest(root, "Realised we dont need this. Sorry");

    assertEquals("",requestGraph.toString());
  }

  @Test
  public void graphCanCorrectlyReturnAllRequestsThatCanBeResolvedImmediately(){
    try {
      RequestBuilder.ANewRequest(Team.SYSTEMS,Team.AVIONICS)
          .inGraph(requestGraph)
          .withQuery("How many CPUS are you using?")
          .build();

      RequestBuilder.ANewRequest(Team.AVIONICS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("What's the diameter of the inner tube where the CPUs sit?")
          .toSolve(requestGraph.findRequests(RequestDirection.FROM, Team.SYSTEMS).get(0))
          .build();

      RequestBuilder.ANewRequest(Team.PROPULSION,Team.SPONSORSHIP)
          .inGraph(requestGraph)
          .withQuery("What engine do we have money for?")
          .build();

      RequestBuilder.ANewRequest(Team.SPONSORSHIP,Team.SYSTEMS)
          .inGraph(requestGraph)
          .withQuery("Do you know any engine companies?")
          .toSolve(requestGraph.findRequests(RequestDirection.FROM, Team.PROPULSION).get(0))
          .build();

      RequestBuilder.ANewRequest(Team.SYSTEMS,Team.STRUCTURES)
          .inGraph(requestGraph)
          .withQuery("Are we using carbon fibre?")
          .build();
    } catch (
        IllegalRequestException e) {
      fail(e.getMessage());
    }

    assertThat("List equality without order", requestGraph.getImmediateProblems(),
        containsInAnyOrder(Arrays.asList(
        requestGraph.findRequest(2),
        requestGraph.findRequest(4),
        requestGraph.findRequest(5)).toArray()));
  }

/*
  @Test
  public void bucketSetUp(){
    String credentialsPath = System.getProperty("user.dir") + "/ksp-request-tracker-8291299f282f.json";

    Credentials credentials = null;
    try {
      credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath)).createScoped(
          Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    Storage storage =
        StorageOptions.newBuilder()
            .setCredentials(credentials)
            .setProjectId("ksp-request-tracker\n")
            .build()
            .getService();
    System.out.println(BucketInfo.of("ksp-request-node-bucket"));
    Bucket bucket = storage.create(BucketInfo.of("ksp-request-node-bucket"));

    String value = "Hello, World!";
    byte[] bytes = value.getBytes(UTF_8);
    Blob blob = bucket.create("my-first-blob", bytes);
  }
 */
}
