package com.ayush.ems.globlaexceptionhandler;

//UserAlreadyExistsException.java
public class UserAlreadyExistsException extends Exception {
 public UserAlreadyExistsException(String message) {
     super(message);
 }
}