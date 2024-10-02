package com.errors;

public class UserServiceException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Constructor mặc định
    public UserServiceException() {
        super();
    }

    // Constructor với thông điệp lỗi
    public UserServiceException(String message) {
        super(message);
    }

    // Constructor với thông điệp lỗi và nguyên nhân gây ra lỗi
    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor với nguyên nhân gây ra lỗi
    public UserServiceException(Throwable cause) {
        super(cause);
    }
}
