package ksp.utilities;

public class SearchUtilities {

  public static Integer stringMatchPercentage(String stringToSearch, String searchPhrase) {
    String[] words = searchPhrase.replaceAll("\\p{Punct}", "").toLowerCase().split(" ");
    int matches = 0;
    for (String word : words) {
      if (stringToSearch.toLowerCase().contains(word)) {
        matches++;
      }
    }
    return (100 * matches) / words.length;
  }
}
