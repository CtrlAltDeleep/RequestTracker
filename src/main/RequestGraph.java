package main;

import java.util.ArrayList;
import main.utilities.IDGenerator;

public class RequestGraph { // State storing utility class - NEEDS FIXING
  public static ArrayList<RequestNode> rootRequests;

  public static boolean init(ArrayList<RequestNode> rootRequests) {
    RequestGraph.rootRequests = rootRequests;
    return IDGenerator.init(0);
  }

  public static boolean init(String path) {
    rootRequests = readDataFromPath(path);
    return IDGenerator.init(0); //TODO: replace with int read from file
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
