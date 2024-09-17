package com.quside;

/**
 * Exception emitted by an event that causes a shift in FipsStatus
 * A new instance of this will automatically move FipsStatus to Error!
 */
public class EntropyProviderOperationException extends RuntimeException
{
    public EntropyProviderOperationException()
    {

    }

    public EntropyProviderOperationException(String message)
    {
        super(message);
    }

    public EntropyProviderOperationException(String message, Throwable cause)
    {
        super(message, cause);

    }

    public EntropyProviderOperationException(Throwable cause)
    {
        super(cause);

    }

    public EntropyProviderOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);

    }
}
