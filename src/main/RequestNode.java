package main;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import main.exceptions.IllegalRequestException;
import main.utilities.IDGenerator;
import main.utilities.Team;
import org.jetbrains.annotations.NotNull;

public class RequestNode {
  private Team requester;
  private Team requestee;
  private String details;
  private RequestNode source;
  private List<RequestNode> branches = new LinkedList<>();
  private final int id;

  /* RequestNode constructor is package-private. Please use builder instead. */
  RequestNode(Team requester, Team requestee, String details, RequestNode source,
      LinkedList<RequestNode> branches) throws IllegalRequestException {

    this.requester = requester;
    this.requestee = requestee;

    this.details = details;

    setSource(source);

    if (branches != null){
      if (branches.stream().allMatch(x -> (x.requester == this.requestee))){
        this.branches = branches;

      }else{
        throw new IllegalRequestException(
            "New request was generated with branch requests not made by requestee team"
        );
      }
    }

    id = IDGenerator.generateNewID();
  }

  public boolean isRoot() {
    return source == null;
  }

  public boolean isTip() {
    return branches.isEmpty();
  }

  public Team getRequester() {
    return requester;
  }

  public Team getRequestee() {
    return requestee;
  }

  public RequestNode getSource() {
    return source;
  }

  public List<RequestNode> getBranches() {
    return branches;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  /*
  Removes a request node from the dependency graph.
  If the node is not a tip, then all branches are deleted.
  */
  public void removeRequest(){
    if (!isRoot()){
      source.removeBranch(this);
    }else{
      RequestGraph.removeRoot(this);
    }

    for (RequestNode branch:branches){
      branch.removeRequest();
    }

    //TODO: send email to this requester and the source requester saying this request is solved.
    //      maybe save response in log.
  }

  private void setRequestee(Team newRequestee) {
    requestee = newRequestee;
  }

  /*
  Sets the source of this Request, and updates this source to
  have this as a branch. If there is a mismatch with the source requestee
  and this node's requester, then throw an IllegalRequestException. If there
  was a previous source then unlink that.
  */
  public void setSource(RequestNode newSource) throws IllegalRequestException {
    if (newSource != null) {
      if (source != null){
        source.removeBranch(this);
      }
      if (newSource.getRequestee() == requester) {
        this.source = newSource;
        newSource.addBranch(this);
      } else {
        if (source != null){ //revert changes
          source.addBranch(this);
        }
        throw new IllegalRequestException(
            "New request tried to solve for a request not directed to the team.");
      }
    }else{ // we want to make this request a root
      if (source != null){
        source.removeBranch(this);
      }
      this.source = null; // TODO: somehow we need to let the graph know we made a new root here...
    }
  }

  /*
  Force sets source. DO NOT CALL without first checking newSource.requestee == this.requester
  Used by addBranch to prevent cycles
  */
  private void hardSetSource(RequestNode newSource){
    source = newSource;
  }

  /*
  Adds the Request as a branch, and updates the Requests old source by
  removing it if the Request is not a root. If there is a mismatch with the
  new branches requester and this node's requestee, then throw an IllegalRequestException.
  Does not use setSource on newBranch to prevent function call cycle.
  */
  public void addBranch(@NotNull RequestNode newBranch) throws IllegalRequestException {
    if (newBranch.getRequester() == requestee){
      if (newBranch.isRoot()){
        branches.add(newBranch); //TODO: we need to let graph know we lost a root, then we can extract common parts of if statement
        newBranch.hardSetSource(this);
      }else{
        newBranch.getSource().removeBranch(newBranch);
        branches.add(newBranch);
        newBranch.hardSetSource(this);
      }
    } else{
      throw new IllegalRequestException(
          "Branch could not be added as source requestee is not the branch requester."
      );
    }
  }

  public void removeBranch(RequestNode newBranch){
    branches.remove(newBranch);
  }

  @Override
  public String toString() {
   return toStringHelper(0);
  }

  public String toStringHelper(int tabLevel){
    String tab = new String(new char[tabLevel]).replace("\0", "\t");

    StringBuilder detailsString = new StringBuilder();
    if (!isTip()){
      detailsString = new StringBuilder("\n" + tab + "Waiting on: \n");
      for (RequestNode branch:branches){
        detailsString.append(branch.toStringHelper(tabLevel+1));
        detailsString.append("\n");
      }
      detailsString.deleteCharAt(detailsString.length()-1);
    }

    if (isRoot()){
      return tab + "Root Request #" + id + " from " + requester + " to " + requestee +": " + details
          + detailsString;
    }else{
      return tab + "Branch Request #" + id + " from " + requester + " to " + requestee +": " + details
          + detailsString;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestNode that = (RequestNode) o;
    return Objects.equals(id, that.id) && Objects.equals(details, that.details);
  }

  public int getID() {
    return id;
  }
}