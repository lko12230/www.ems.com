package com.ayush.ems.globlaexceptionhandler;

public class CustomException {
    
    public static class UserNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidTeamException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InvalidTeamException(String message) {
            super(message);
        }
    }
}
