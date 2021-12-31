package ksp.utilities;

import java.io.Serializable;

public record Metadata(int idGenState) implements Serializable {

  public Metadata(){
    this(0);
  }
}
