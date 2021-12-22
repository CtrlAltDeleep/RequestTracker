package main.exceptions;

public class IllegalRequestException extends Exception {

    public IllegalRequestException(String errorMessage) {
    super(errorMessage);
  }

}
