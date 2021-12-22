package main;

import java.util.ArrayList;

public class RequestGraph {
  public static ArrayList<RequestNode> rootRequests;

  public RequestGraph(ArrayList<RequestNode> rootRequests) {
    RequestGraph.rootRequests = rootRequests;
  }

  public RequestGraph(String path) {
    rootRequests = readGraphFromPath(path);
  }

  public static ArrayList<RequestNode> readGraphFromPath(String path){
    //TODO
    return null;
  }

  public static boolean saveGraphToPath(String path){
    //TODO: traverse graph and save
    return false;
  }

  public static void removeRoot(RequestNode request){
    rootRequests.remove(request);
  }

}
