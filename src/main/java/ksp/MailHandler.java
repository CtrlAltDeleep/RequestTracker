package ksp;

import static ksp.RequestTracker.email;

import ksp.utilities.Team;

public class MailHandler implements MailHandlerInterface{

  @Override
  public void send(Team sender, String password, Team recipient, String message) {
    System.out.println(email + "Email not implemented yet.");
    /*
    String password = getAns(input + "Enter password for user team email: ");

    System.out.println(email + "Should send: \"" + message + "\" from "
        + sender.getEmail() + " to " + recipient.getEmail());
   */
  }
}
