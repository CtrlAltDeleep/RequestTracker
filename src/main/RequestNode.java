package main;

import java.util.ArrayList;
import java.util.Objects;

public class RequestNode {
  private Team requester;
  private Team requestee;
  private String details;
  private RequestNode source;
  private ArrayList<RequestNode> branches = new ArrayList<>();

  public RequestNode(Team requester, Team requestee, String details, RequestNode source,
      ArrayList<RequestNode> branches) {

    this.requester = requester;
    this.requestee = requestee;

    this.details = details;
    this.source = source; // todo: link source to this new one
    if (branches != null){
      this.branches = branches;
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

  public boolean removeRequest(){
    boolean notRoot = !isRoot();
    for (RequestNode branch : branches) {
      branch.setSource(source);
      if (notRoot){
        source.addBranch(branch);
      }
    }
    if (notRoot){
      source.removeBranch(this);
    }

    return true;
  }

  public void setSource(RequestNode newSource){
    source = newSource;
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
