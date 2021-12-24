package main;

import java.util.ArrayList;
import main.utilities.IDGenerator;

public class RequestGraph { // State storing utility class - NEEDS FIXING
  public static ArrayList<RequestNode> rootRequests;

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

  public void removeRoot(RequestNode request) {
    rootRequests.remove(request);
  }

  public void addRoot(RequestNode request) {
    rootRequests.add(request);
  }

  public boolean addNewRequest(RequestNode newRequest) {
    if (newRequest.isRoot()){
      rootRequests.add(newRequest);
    }
    //TODO saveDataToPath();
    return true;
  }
}
