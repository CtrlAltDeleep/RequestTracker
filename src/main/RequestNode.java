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
  public boolean removeRequest(){
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
    return true;
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
        throw new IllegalRequestException(
            "New request tried to solve for a request not directed to the team.");
      }
    }
  }
  public void addBranch(@NotNull RequestNode newBranch) throws IllegalRequestException {
    if (newBranch.getRequester() == requestee){
      branches.add(newBranch);
    } else{
      throw new IllegalRequestException(
          "Branch could not be added as source requestee is not the branch requester. "
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

    StringBuilder detailsString = new StringBuilder(tab + "\tNothing");
    if (!isTip()){
      detailsString = new StringBuilder();
      for (RequestNode branch:branches){
        detailsString.append(branch.toStringHelper(tabLevel+1));
        detailsString.append("\n");
      }
    }

    if (isRoot()){
      return tab + "Root Request #" + id + " from " + requester + " to " + requestee +": " + details
          + "\n" + tab + "Waiting on: \n" + detailsString;
    }else{
      return tab + "Branch Request #" + id + " from " + requester + " to " + requestee +": " + details
          + "\n" + tab + "Waiting on: \n" + detailsString;
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
    return Objects.equals(source, that.source) && Objects
        .equals(branches, that.branches) && Objects
        .equals(requester, that.requester);
  }

  public int getID() {
    return id;
  }
}