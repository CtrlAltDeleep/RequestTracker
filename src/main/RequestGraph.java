package main;

import java.util.ArrayList;
import main.utilities.IDGenerator;

public class RequestGraph {
  public static ArrayList<RequestNode> rootRequests;

  public RequestGraph(ArrayList<RequestNode> rootRequests) {
    RequestGraph.rootRequests = rootRequests;
    IDGenerator.init(0);
  }

  public RequestGraph(String path) {
    rootRequests = readDataFromPath(path);
  }

  public static ArrayList<RequestNode> readDataFromPath(String path) {
    // TODO: also set IDgen state
    return null;
  }

  public static boolean saveDataToPath(String path) {
    // TODO: traverse graph and save
    return false;
  }

  public static void removeRoot(RequestNode request) {
    rootRequests.remove(request);
  }

  public static void addRoot(RequestNode request) {
    rootRequests.add(request);
  }

  public static boolean addNewRequest(RequestNode newRequest) {
    if (newRequest.isRoot()){
      rootRequests.add(newRequest);
    }
    //TODO saveDataToPath();
    return true;
  }


}
