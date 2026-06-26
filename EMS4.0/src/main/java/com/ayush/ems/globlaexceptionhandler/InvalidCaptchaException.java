package com.ayush.ems.globlaexceptionhandler;

//InvalidCaptchaException.java
public class InvalidCaptchaException extends Exception {
 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public InvalidCaptchaException(String message) {
     super(message);
 }
}