package ksp.utilities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import ksp.RequestNode;

public record ArchiveNode(RequestNode archivedNode, String solution, Date solutionDate) implements Serializable {

  public ArchiveNode(RequestNode archivedNode, String solution){
    this(archivedNode,solution, Calendar.getInstance().getTime());
  }

  @Override
  public String toString() {
    return """
  %s
  %s
  Solution: %s""".formatted(solutionDate, archivedNode.toString(),solution);
  }
}
