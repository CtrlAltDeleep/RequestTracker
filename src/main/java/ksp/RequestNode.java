/**
 * @author Avaneesh Deleep <a href="mailto:ad2820@ic.ac.uk">Email for bug reports</a>
 */

package ksp;

import static ksp.utilities.Search.stringMatchPercentage;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import ksp.exceptions.IllegalRequestException;
import ksp.utilities.IDGenerator;
import ksp.utilities.Team;
import org.jetbrains.annotations.NotNull;

public class RequestNode implements Serializable {
  private final Team requester;
  private Team requestee;
  private String details;
  private RequestNode source;
  private List<RequestNode> branches = new LinkedList<>();
  private final int id;

  /* RequestNode constructor is package-private. Please use builder instead. */

  /**
   * @param requester Team that is requesting (who the request is from)
   * @param requestee Team that the request id directed to
   * @param details String containing actual question or query
   * @param source Request that this Request is trying to resolve
   * @param branches List of requests that are trying to solve this request
   * @param requestGraph The graph that tracks this request - auto-links on construction
   * @throws IllegalRequestException if the request is invalid
   */
  RequestNode(
      Team requester,
      Team requestee,
      String details,
      RequestNode source,
      LinkedList<RequestNode> branches,
      RequestGraph requestGraph)
      throws IllegalRequestException {

    this.requester = requester;
    this.requestee = requestee;

    this.details = details;

    setSource(source, requestGraph);

    if (branches != null) {
      if (branches.stream().allMatch(x -> (x.requester == this.requestee))) {
        this.branches = branches;

      } else {
        throw new IllegalRequestException(
            "New request was generated with branch requests not made by requestee team");
      }
    }

    id = IDGenerator.generateNewID();
  }

  protected boolean isRoot() {
    return source == null;
  }

  protected boolean isTip() {
    return branches.isEmpty();
  }

  protected Team getRequester() {
    return requester;
  }

  protected Team getRequestee() {
    return requestee;
  }

  protected RequestNode getSource() {
    return source;
  }

  protected List<RequestNode> getBranches() {
    return branches;
  }

  protected String getDetails() {
    return details;
  }

  protected void setDetails(String details) {
    this.details = details;
  } //TODO:add edit function

  /**
   *  Removes a request node from the dependency graph. If the node is not a tip, then all
   *  branches are deleted.
   *
   * @param requestGraph The request graph that tracks this request
   */
  protected void removeRequest(RequestGraph requestGraph) {
    if (!isRoot()) {
      source.removeBranch(this);
    } else {
      requestGraph.removeRoot(this);
    }

    for (RequestNode branch : branches) {
      branch.removeRequest(requestGraph);
    }

    // TODO: send email to this requester and the source requester saying this request is solved.
    //      maybe save response in log. needs to be here, not in graph as when branches
    //      more around, we can drop a branch without going through the graph
  }

  private void setRequestee(Team newRequestee) {
    requestee = newRequestee;
  }

  /**
   * Sets the source of this Request, and updates this source to have this as a branch. If there is
   * a mismatch with the source requestee and this node's requester, then throw an
   * IllegalRequestException. If there was a previous source then unlink that.
   *
   * @param newSource New source to set. Can be null to set RequestNode to root.
   * @param requestGraph The request graph that tracks this request
   * @throws IllegalRequestException if the newSource isn't directed at this request
   */
  protected void setSource(RequestNode newSource, RequestGraph requestGraph)
      throws IllegalRequestException {

    if ((newSource != null) && (newSource.getRequestee() != requester)) { // lazy evaluation
      throw new IllegalRequestException(
          "New request tried to solve for a request not directed to the team.");
    }

    if (newSource != null) {
      if (source == null) { // node is currently a root
        requestGraph.removeRoot(this);
      } else {
        source.removeBranch(this);
      }
      this.source = newSource;
      newSource.addBranch(this, requestGraph);
    }
    else { // we want to make this request a root
      if (source != null) {
        source.removeBranch(this);
      }
      this.source = null;
      requestGraph.addRoot(this); // let the graph know we have a new root to track
    }
  }

  /**
   * Force sets source. DO NOT CALL without first checking newSource.requestee == this.requester
   * Used by addBranch to prevent cycles
   *
   * @param newSource new source
   */
  private void hardSetSource(RequestNode newSource, RequestGraph requestGraph) {
    source = newSource;
  }

  /**
   * Adds the Request as a branch, and updates the Requests old source by removing it if the Request
   * is not a root. If there is a mismatch with the new branches requester and this node's
   * requestee, then throw an IllegalRequestException. Does not use setSource on newBranch to
   * prevent function call cycle.
   *
   * @param newBranch New branch to add to this Request Node
   * @param requestGraph The request graph that tracks this request
   * @throws IllegalRequestException is the branch to be added doesn't solve this request
   */
  protected void addBranch(@NotNull RequestNode newBranch, RequestGraph requestGraph)
      throws IllegalRequestException {
    if (newBranch.getRequester() == requestee) {
      if (newBranch.isRoot()) {
        branches.add(newBranch);
        requestGraph.removeRoot(newBranch); // let graph know we newBranch isn't a root anymore
        newBranch.hardSetSource(this, requestGraph);
      } else {
        newBranch.getSource().removeBranch(newBranch);
        branches.add(newBranch);
        newBranch.hardSetSource(this, requestGraph);
      }
    } else {
      throw new IllegalRequestException(
          "Branch could not be added as source requestee is not the branch requester.");
    }
  }

  protected void removeBranch(RequestNode newBranch) {
    branches.remove(newBranch);
  }

  @Override
  public String toString() {
    return toStringHelper(0);
  }

  private String toStringHelper(int tabLevel) {
    String tab = new String(new char[tabLevel]).replace("\0", "\t");

    StringBuilder detailsString = new StringBuilder();
    if (!isTip()) {
      detailsString = new StringBuilder("\n" + tab + "Waiting on: \n");
      for (RequestNode branch : branches) {
        detailsString.append(branch.toStringHelper(tabLevel + 1));
        detailsString.append("\n");
      }
      try{
        detailsString.deleteCharAt(detailsString.length() - 1);
      } catch (Exception ignored){}
    }

    if (isRoot()) {
      return tab + "Root Request #" + id + " from " + requester + " to " + requestee + ": "
          + details + detailsString;
    } else {
      return tab + "Branch Request #" + id + " from " + requester + " to " + requestee + ": "
          + details + detailsString;
    }
  }

  /**
   * Returns an int from 0-100 based on how much the string entered
   * matches the details of this request.
   *
   * 0   : No words in the searchPhrase match
   * 100 : All words in the search phrase are present in this request description
   *
   * @param searchPhrase The string to check match on
   */
  public Integer matchPercentage(String searchPhrase){
    return stringMatchPercentage(details,searchPhrase);
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

  protected int getID() {
    return id;
  }
}
