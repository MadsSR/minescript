package interpreter.exceptions;

public class ThreadInterruptedException extends RuntimeException {
    public ThreadInterruptedException(String message) { super(message); }
}
