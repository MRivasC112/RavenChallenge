package com.demo.employees.exception;

public class SearchTermTooShortException extends RuntimeException {
    public SearchTermTooShortException(int minLength) {
        super("Search term must be at least " + minLength + " characters");
    }
}
