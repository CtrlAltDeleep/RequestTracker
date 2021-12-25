/**
 * @author Avaneesh Deleep <a href="mailto:ad2820@ic.ac.uk">Email for bug reports</a>
 */

package ksp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import ksp.utilities.IDGenerator;
import ksp.utilities.RequestDirection;
import ksp.utilities.Team;



public class RequestGraph {
  private static ArrayList<RequestNode> rootRequests;

  public RequestGraph(ArrayList<RequestNode> rootRequests) {
    RequestGraph.rootRequests = rootRequests;
    IDGenerator.init(0);
  }

  public RequestGraph(String path) {
    rootRequests = readDataFromPath(path);
    IDGenerator.init(0); //TODO: replace with int read from file
  }

  public ArrayList<RequestNode> readDataFromPath(String path) {
    // TODO: also set IDgen state
    return null;
  }

  public boolean saveDataToPath(String path) {
    // TODO: traverse graph and save
    return false;
  }

  protected void removeRoot(RequestNode request) {
    rootRequests.remove(request);
  }

  protected void addRoot(RequestNode request) {
    rootRequests.add(request);
  }

  public boolean addNewRequest(RequestNode newRequest) {
    if (newRequest.isRoot()){
      rootRequests.add(newRequest);
    }
    //TODO saveDataToPath();
    return true;
  }

  public boolean resolveRequest(RequestNode request){
    request.removeRequest(this);
    // TODO saveDataToPath();
    return true;
  }

  public RequestNode findRequest(int id){
    for (RequestNode root : rootRequests){
      RequestNode output = findRequestIDSearch(root,id);
      if (output != null){
        return output;
      }
    }
    return null;
  }

  public ArrayList<RequestNode> findRequests(Team team, RequestDirection direction){
    Set<RequestNode> output = new HashSet<>();
    if (direction == RequestDirection.FROM_US){
      // find requests that this team made / are the requester for
      for (RequestNode root : rootRequests) {
        output.addAll(findRequestsRequesterSearch(root,team,new HashSet<>()));
      }
    } else{
      // find requests that want info from this team / are the requestee in
      for (RequestNode root : rootRequests) {
        output.addAll(findRequestsRequesteeSearch(root,team,new HashSet<>()));
      }
    }
    return new ArrayList<>(output);
  }

  private RequestNode findRequestIDSearch(RequestNode requestToCheck, int id) {
    if (requestToCheck.getID() == id){ // current request matches
      return requestToCheck;
    }else if (requestToCheck.isTip()){ // no more branches to search here
      return null;
    }else{
      for (RequestNode branch:requestToCheck.getBranches()){ // search branches for id
        if (findRequestIDSearch(branch,id) != null){
          return branch;
        }
      }
      return null; //id not found in branches
    }
  }

  private Set<RequestNode> findRequestsRequesterSearch(
      RequestNode requestToCheck,
      Team team,
      Set<RequestNode> currentMatches) {

    if (requestToCheck.getRequester() == team){ // current request matches
      currentMatches.add(requestToCheck);
    }
    if (requestToCheck.isTip()){ // no more branches to search here
      return currentMatches;
    }else{
      for (RequestNode branch:requestToCheck.getBranches()){ // search branches
        currentMatches.addAll(findRequestsRequesterSearch(branch,team,currentMatches));
      }
    }
    return currentMatches; // return currentMatches with upstream branch matches added
  }

  private Set<RequestNode> findRequestsRequesteeSearch(
      RequestNode requestToCheck,
      Team team,
      Set<RequestNode> currentMatches) {

    if (requestToCheck.getRequestee() == team){ // current request matches
      currentMatches.add(requestToCheck);
    }
    if (requestToCheck.isTip()){ // no more branches to search here
      return currentMatches;
    }else{
      for (RequestNode branch:requestToCheck.getBranches()){ // search branches
        currentMatches.addAll(findRequestsRequesteeSearch(requestToCheck,team,currentMatches));
      }
    }
    return currentMatches; // return currentMatches with upstream branch matches added
  }

  @Override
  public String toString() {
     StringBuilder output = new StringBuilder();
     for (RequestNode root :rootRequests){
       output.append(root.toString()).append("\n");
     }
     return output.toString();
  }
}
