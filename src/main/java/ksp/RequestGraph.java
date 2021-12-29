/**
 * @author Avaneesh Deleep <a href="mailto:ad2820@ic.ac.uk">Email for bug reports</a>
 */

package ksp;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.FileInputStream;
import java.io.IOException;
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

  public ArrayList<RequestNode> readDataFromPath(String credentialsPath) {

    if (credentialsPath == null) {
      credentialsPath = System.getProperty("user.dir") + "/ksp-request-tracker-8291299f282f.json";
    }

    Credentials credentials = null;
    try {
      credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
    } catch (IOException e) {
      e.printStackTrace();
    }
    Storage storage =
        StorageOptions.newBuilder()
            .setCredentials(credentials)
            .setProjectId("ksp-request-tracker\n")
            .build()
            .getService();

    Bucket bucket = storage.create(BucketInfo.of("ksp-request-node-bucket"));

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
    //TODO saveDataToPath();
    return true;
  }

  public boolean resolveRequest(RequestNode request, String solution){
    request.removeRequest(this);

    // TODO saveDataToPath();
    // TODO save solution and solved request to archive log
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

  public ArrayList<RequestNode> findRequests(RequestDirection direction, Team team){
    Set<RequestNode> output = new HashSet<>();
    if (direction == RequestDirection.FROM){
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

  public ArrayList<RequestNode> findRequests(String keywords){
    Set<RequestNode> output = new HashSet<>();
    for (RequestNode root : rootRequests) {
      output.addAll(findKeyWordsSearch(root,keywords.toLowerCase(),new HashSet<>()));
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
        currentMatches.addAll(findRequestsRequesteeSearch(branch,team,currentMatches));
      }
    }
    return currentMatches; // return currentMatches with upstream branch matches added
  }

  private Set<RequestNode> findKeyWordsSearch(
      RequestNode requestToCheck,
      String keywords,
      Set<RequestNode> currentMatches) {

    if (requestToCheck.getDetails().toLowerCase().contains(keywords)){ // current request contains key phrase
      currentMatches.add(requestToCheck);
    }
    if (requestToCheck.isTip()){ // no more branches to search here
      return currentMatches;
    }else{
      for (RequestNode branch:requestToCheck.getBranches()){ // search branches
        currentMatches.addAll(findKeyWordsSearch(branch,keywords,currentMatches));
      }
    }
    return currentMatches; // return currentMatches with upstream branch matches added
  }

  public ArrayList<RequestNode> getImmediateProblems() {
    Set<RequestNode> output = new HashSet<>();
    for (RequestNode root : rootRequests) {
      output.addAll(findAllTipsSearch(root,new HashSet<>()));
    }
    return new ArrayList<>(output);
  }

  private Set<RequestNode> findAllTipsSearch(
      RequestNode requestToCheck,
      Set<RequestNode> currentMatches) {

    if (requestToCheck.isTip()){ // no more branches to search here
      currentMatches.add(requestToCheck);
      return currentMatches;
    }else{
      for (RequestNode branch:requestToCheck.getBranches()){ // search branches
        currentMatches.addAll(findAllTipsSearch(branch,currentMatches));
      }
    }
    return currentMatches;
  }

  public boolean isEmpty(){
    return rootRequests.isEmpty();
  }

  @Override
  public String toString() {
     StringBuilder output = new StringBuilder();
     for (RequestNode root :rootRequests){
      output.append(root.toString()).append("\n\n");
     }
    try{
     output.deleteCharAt(output.length() - 1);
     } catch (Exception ignored){}
     return output.toString();
  }
}
