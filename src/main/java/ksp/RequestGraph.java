/**
 * @author Avaneesh Deleep <a href="mailto:ad2820@ic.ac.uk">Email for bug reports</a>
 */

package ksp;

import static ksp.utilities.SearchUtilities.stringMatchPercentage;
import static java.util.stream.Collectors.toSet;
import static ksp.RequestTracker.error;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ksp.utilities.ArchiveNode;
import ksp.utilities.IDGenerator;
import ksp.utilities.Metadata;
import ksp.utilities.RequestDirection;
import ksp.utilities.Team;
import org.jetbrains.annotations.NotNull;


public class RequestGraph {
  private static ArrayList<RequestNode> rootRequests;
  private static ArrayList<ArchiveNode> archive;

  private static final String graphBucketPath = "save-data/request-graph-bucket.data";
  private static final String archiveBucketPath = "save-data/request-archive-bucket.data";
  private static final String metadataBucketPath = "save-data/metadata-bucket.data";



  public RequestGraph(ArrayList<RequestNode> rootRequests) {
    RequestGraph.rootRequests = rootRequests;
    RequestGraph.archive = new ArrayList<>();
    IDGenerator.init(0);
  }

  public RequestGraph() {
    rootRequests = readData();
    archive = readArchiveData();
    Metadata metadata = readMetadata();
    IDGenerator.init(metadata.idGenState());
  }

  public ArrayList<RequestNode> readData() {
    if (!ensureBucketsExist()){
      return new ArrayList<>();
    }


    try {
      FileInputStream graphFileStream = new FileInputStream(graphBucketPath);
      ObjectInputStream graphObjStream = new ObjectInputStream(graphFileStream);

      @SuppressWarnings("unchecked")
      ArrayList<RequestNode> graph = (ArrayList<RequestNode>) graphObjStream.readObject();

      graphObjStream.close();
      graphFileStream.close();
      return graph;
    } catch (Exception e) {
      System.out.println(error + "An error occurred reading graph save file.");
    }

    return new ArrayList<>();
  }

  public ArrayList<ArchiveNode> readArchiveData() {
    if (!ensureBucketsExist()){
      return new ArrayList<>();
    }

    try {
      FileInputStream archiveFileStream = new FileInputStream(archiveBucketPath);
      ObjectInputStream archiveObjStream = new ObjectInputStream(archiveFileStream);

      @SuppressWarnings("unchecked")
      ArrayList<ArchiveNode> archive = (ArrayList<ArchiveNode>) archiveObjStream.readObject();

      archiveObjStream.close();
      archiveFileStream.close();
      return archive;
    } catch (Exception e) {
      System.out.println(error + "An error occurred reading archive data.");
    }

    return new ArrayList<>();
  }

  public Metadata readMetadata() {
    if (!ensureBucketsExist()){
      return new Metadata();
    }

    try {
      FileInputStream metadataFileStream = new FileInputStream(metadataBucketPath);
      ObjectInputStream metadataObjStream = new ObjectInputStream(metadataFileStream);

      Metadata metadata = (Metadata) metadataObjStream.readObject();

      metadataObjStream.close();
      metadataFileStream.close();
      return metadata;
    } catch (Exception e) {
      System.out.println(error + "An error occurred reading archive data.");
    }

    return new Metadata();
  }

  public boolean saveData() {
    if (!ensureBucketsExist()){
      return false;
    }

    try {
      FileOutputStream graphFileStream = new FileOutputStream(graphBucketPath,false);
      ObjectOutputStream graphObjStream = new ObjectOutputStream(graphFileStream);

      graphObjStream.writeObject(rootRequests);

      graphObjStream.close();
      graphFileStream.close();

    } catch (IOException e) {
      System.out.println(error + "An error occurred when saving graph.");
      e.printStackTrace();
    }

    return true;
  }

  public boolean saveArchiveData() {
    if (!ensureBucketsExist()){
      return false;
    }

    try {
      FileOutputStream archiveFileStream = new FileOutputStream(archiveBucketPath,false);
      ObjectOutputStream archiveObjStream = new ObjectOutputStream(archiveFileStream);

      archiveObjStream.writeObject(archive);

      archiveObjStream.close();
      archiveFileStream.close();
    } catch (IOException e) {
      System.out.println(error + "An error occurred when updating archive.");
      e.printStackTrace();
    }

    return saveData();
  }

  public boolean saveMetadata() {
    if (!ensureBucketsExist()){
      return false;
    }

    try {
      FileOutputStream metadataFileStream = new FileOutputStream(metadataBucketPath,false);
      ObjectOutputStream metadataObjStream = new ObjectOutputStream(metadataFileStream);

      Metadata metadata = new Metadata(IDGenerator.saveState());
      metadataObjStream.writeObject(metadata);

      metadataObjStream.close();
      metadataFileStream.close();
    } catch (IOException e) {
      System.out.println(error + "An error occurred when saving Metadata.");
      e.printStackTrace();
    }

    return saveData();
  }

  private boolean ensureBucketsExist(){
    try {
      File graphBucket = new File(graphBucketPath);
      File historyBucket = new File(archiveBucketPath);
      File metadataBucket = new File(metadataBucketPath);

      if (graphBucket.createNewFile()){
        System.out.println(error + "Application could not locate current graph state.");
      }
      if (historyBucket.createNewFile()){
        System.out.println(error + "Application could not locate request history.");
      }
      if (metadataBucket.createNewFile()){
        System.out.println(error + "Application could not locate metadata.");
      }
    } catch (IOException e) {
      System.out.println(error + "An error occurred when finding save files.");
      return false;
    }
    return true;
  }

  protected void removeRoot(RequestNode request) {
    rootRequests.remove(request);
  }

  protected void addRoot(RequestNode request) {
    rootRequests.add(request);
  }

  public boolean addNewRequest(RequestNode newRequest) {
    saveMetadata();
    return saveData();
  }

  public boolean resolveRequest(RequestNode request, String solution){
    request.removeRequest(this);
    archive.add(new ArchiveNode(request,solution));
    return saveArchiveData();
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

  public ArchiveNode findArchivedRequest(int id){
    for (ArchiveNode request : archive){
      if (request.archivedNode().getID() == id){
        return request;
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

  public ArrayList<ArchiveNode> findArchivedRequests(RequestDirection direction, Team team){
    Set<ArchiveNode> output;
    if (direction == RequestDirection.FROM){
      // find requests that this team made / are the requester for
      output = archive.stream().filter(x -> x.archivedNode().getRequester()==team).collect(toSet());
    } else{
      // find requests that want info from this team / are the requestee in
      output = archive.stream().filter(x -> x.archivedNode().getRequestee()==team).collect(toSet());
    }
    return new ArrayList<>(output);
  }

  public ArrayList<RequestNode> findRequests(String keywords){
    Set<RequestNode> output = new HashSet<>();
    for (RequestNode root : rootRequests) {
      output.addAll(findKeyWordsSearch(root,keywords.toLowerCase(),new HashSet<>()));
    }
    Comparator<RequestNode> compareByMatch = Comparator.comparing(
        (RequestNode o) -> o.matchPercentage(keywords));
    ArrayList<RequestNode> listOut = new ArrayList<>(output);
    listOut.sort(compareByMatch);
    Collections.reverse(listOut);
    return listOut;
  }

  public ArrayList<ArchiveNode> findInArchivedRequests(String keywords){
    Set<ArchiveNode> output;
    output = archive.stream().filter(x -> x.archivedNode().matchPercentage(keywords)>0).collect(toSet());

    return sortArchiveNodes(keywords, output);
  }

  public ArrayList<ArchiveNode> findInArchivedSolutions(String keywords){
    Set<ArchiveNode> output;
    output = archive.stream().filter(x -> stringMatchPercentage(x.solution(),keywords)>0).collect(toSet());

    return sortArchiveNodes(keywords, output);
  }

  @NotNull
  private ArrayList<ArchiveNode> sortArchiveNodes(String keywords, Set<ArchiveNode> output) {
    Comparator<ArchiveNode> compareByMatch = Comparator.comparing(
        (ArchiveNode o) -> o.archivedNode().matchPercentage(keywords));

    ArrayList<ArchiveNode> listOut = new ArrayList<>(output);
    listOut.sort(compareByMatch);
    Collections.reverse(listOut);
    return listOut;
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

    if (requestToCheck.matchPercentage(keywords)>0){ // current request contains key phrase
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

  public boolean graphIsEmpty(){
    return rootRequests.isEmpty();
  }
  public boolean archiveIsEmpty() {
    return archive.isEmpty();
  }

  public List<ArchiveNode> getArchive(){
    return archive;
  }

  public void clearArchive(){
    archive = new ArrayList<>();
    saveArchiveData();
  }

  public void clearGraph(){
    rootRequests = new ArrayList<>();
    saveData();
  }

  public void clearMetadata(){
    IDGenerator.init(0);
    saveMetadata();
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

  public String archiveToString() {
    StringBuilder output = new StringBuilder();
    for (ArchiveNode arch :archive){
      output.append(arch.toString()).append("\n\n");
    }
    try{
      output.deleteCharAt(output.length() - 1);
    } catch (Exception ignored){}
    return output.toString();
  }
}
