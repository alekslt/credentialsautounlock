package no.toppe.android.credentialautounlock;


public class WrapperException extends AppException {

    private static final long serialVersionUID = 1L;

    public WrapperException(final String detailMessage) {
        super(detailMessage, 0);
    }

    public WrapperException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

}