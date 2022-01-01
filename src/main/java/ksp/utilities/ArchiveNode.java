package ksp.utilities;

import java.io.Serializable;
import ksp.RequestNode;

public record ArchiveNode(RequestNode archivedNode, String solution) implements Serializable {
  @Override
  public String toString() {
    return """
  %s
  Solution: %s""".formatted(archivedNode.toString(),solution);
  }
}
