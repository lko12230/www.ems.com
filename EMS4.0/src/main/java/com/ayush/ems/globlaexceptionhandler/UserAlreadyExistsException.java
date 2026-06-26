package com.ayush.ems.globlaexceptionhandler;

//UserAlreadyExistsException.java
public class UserAlreadyExistsException extends Exception {
 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public UserAlreadyExistsException(String message) {
     super(message);
 }
}