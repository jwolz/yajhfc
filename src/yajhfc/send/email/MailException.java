package yajhfc.send.email;

/**
 * An exception sending the mail
 * @author jonas
 *
 */
public class MailException extends Exception {

    public MailException() {
    }

    public MailException(String message) {
        super(message);
    }

    public MailException(Throwable cause) {
        super(cause);
    }

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }

}