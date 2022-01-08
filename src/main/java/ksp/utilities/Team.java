package ksp.utilities;

public enum Team { // TODO read enum db
  AVIONICS("Avionics", "ad2820@ic.ac.uk"),
  PROPULSION("Propulsion", "ad2820@ic.ac.uk"),
  STRUCTURES("Structures", "ad2820@ic.ac.uk"),
  SYSTEMS("Systems", "ad2820@ic.ac.uk"),
  SPONSORSHIP("Sponsorship", "ad2820@ic.ac.uk");
  // we can add new teams as we go on here

  private final String name;
  private final String email;

  Team(String name, String email) {
    this.name = name;
    this.email = email;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getEmail() {
    return email;
  }
}
