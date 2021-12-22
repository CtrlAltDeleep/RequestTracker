package main;

import java.util.ArrayList;
import java.util.Objects;
import main.exceptions.IllegalRequestException;
import org.jetbrains.annotations.NotNull;

public class RequestNode {
  private Team requester;
  private Team requestee;
  private String details;
  private RequestNode source;
  private ArrayList<RequestNode> branches = new ArrayList<>();

  public RequestNode(Team requester, Team requestee, String details, RequestNode source,
      ArrayList<RequestNode> branches) throws IllegalRequestException {

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

  public ArrayList<RequestNode> getBranches() {
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
      //TODO: remove branch from main structure
    }else{
      //TODO: remove root from main structure
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

  public void setSource(RequestNode newSource) throws IllegalRequestException {
    if (newSource != null) {
      if (newSource.getRequestee() == requester) {
        this.source = newSource;
      } else {
        throw new IllegalRequestException(
            "New request tried to solve for a request not directed to the team.");
      }
    }
  }
  public void addBranch(RequestNode newBranch){
    branches.add(newBranch);
  }
  public void removeBranch(RequestNode newBranch){
    branches.remove(newBranch);
  }

  @Override
  public String toString() {
    if (isRoot()){
      return "Root Request #" + hashCode() + ":"
          + "\n\tWaiting on: " + branches;
    }else{
      return "Request #" + hashCode() + ":"
          + "\n\tResolving: " + source
          + "\n\tWaiting on: " + branches;
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

  @Override
  public int hashCode() {
    return Objects.hash(requester, source, branches);
  }
}

class RequestBuilder {

  public static RequestBuilder ANewRequest(){
    return new RequestBuilder();
  }

}
