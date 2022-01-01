package ksp.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class InterfaceUtilities {

  public static boolean askYN(String question){
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    String ans;
    while (true) {
      System.out.print(question + " [y/n] ");
      try {
        ans = reader.readLine().toLowerCase().strip();
        if (ans.equals("y")){
          return true;
        } else if (ans.equals("n")){
          return false;
        }
      } catch (Exception ignored) {}
    }
  }
}
