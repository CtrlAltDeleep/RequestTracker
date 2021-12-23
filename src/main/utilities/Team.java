package main.utilities;

public enum Team {
  AVIONICS("Avionics","ad2820@ic.ac.uk"),
  PROPULSION("Propulsion","ad2820@ic.ac.uk"),
  STRUCTURES("Structures","ad2820@ic.ac.uk"),
  SYSTEMS("Systems","ad2820@ic.ac.uk"),
  SPONSORSHIP("Sponsorships","ad2820@ic.ac.uk");
  // we can add new teams as we go on here

  private String name;
  private String email;


  Team(String name, String email){
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
