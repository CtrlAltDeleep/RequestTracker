/**
 * @author Avaneesh Deleep <a href="mailto:ad2820@ic.ac.uk">Email for bug reports</a>
 */

package ksp;

import java.util.ArrayList;
import java.util.LinkedList;
import ksp.exceptions.IllegalRequestException;
import ksp.utilities.Team;

public class RequestBuilder {

  private final Team requester;
  private final Team requestee;
  private String details;
  private RequestNode source;
  private final LinkedList<RequestNode> branches = new LinkedList<>();
  private RequestGraph requestGraph;

  private RequestBuilder(Team from, Team to) {
    requester = from;
    requestee = to;
  }

  public static RequestBuilder ANewRequest(Team from, Team to) {
    return new RequestBuilder(from, to);
  }

  public RequestBuilder withQuery(String details) {
    this.details = details;
    return this;
  }

  public RequestBuilder inGraph(RequestGraph requestGraph) {
    this.requestGraph = requestGraph;
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
    if (details == null){
      throw new IllegalRequestException("Cannot build a Request without a query");
    }
    if (requestGraph == null){
      throw new IllegalRequestException("No graph specified to attach this Request node onto");
    }
    return new RequestNode(requester, requestee, details, source, branches, requestGraph);
  }
}
