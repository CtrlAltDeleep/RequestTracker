package ksp;

public interface MailHandlerInterface {
  boolean send( String sender, String password, String recipient, String message);
}
