package ksp;

import ksp.utilities.Team;

public interface MailHandlerInterface {
  void send(Team sender, String password, Team recipient, String message);
}
