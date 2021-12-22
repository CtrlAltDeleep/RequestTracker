package main;

import java.util.ArrayList;
import java.util.LinkedList;
import main.exceptions.IllegalRequestException;

public class RequestBuilder {

  private Team requester;
  private Team requestee;
  private String details;
  private RequestNode source;
  private LinkedList<RequestNode> branches = new LinkedList<>();

  private RequestBuilder(Team from, Team to) {
  }

  public static RequestBuilder ANewRequest(Team from, Team to) {
    return new RequestBuilder(from, to);
  }

  public RequestBuilder withQuery(String details) {
    this.details = details;
    return this;
  }

  public RequestBuilder toSolve(RequestNode source) {
    this.source = source;
    return this;
  }

  public RequestBuilder withBranch(RequestNode branch) {
    this.branches.add(branch);
    return this;
  }

  public RequestBuilder withBranches(ArrayList<RequestNode> branches) {
    this.branches.addAll(branches);
    return this;
  }

  public RequestNode build() throws IllegalRequestException {
    return new RequestNode(requester, requestee, details, source, branches);
  }

}
