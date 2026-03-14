package ru.otus.exceptions;

public class PathNotFoundException extends RuntimeException {
    public PathNotFoundException() {
        super("Path annotation not found");
    }
}
