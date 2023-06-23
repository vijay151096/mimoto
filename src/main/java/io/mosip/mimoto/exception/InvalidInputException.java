package io.mosip.mimoto.exception;

/**
 * Class which covers the range of exception which occurs when invalid input
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
public class InvalidInputException extends BaseUncheckedException {

    /**
     * Unique id for serialization
     */
    private static final long serialVersionUID = -5350213197226295789L;

    /**
     * Constructor with errorCode, and errorMessage
     * 
     * @param errorCode    The error code for this exception
     * @param errorMessage The error message for this exception
     */
    public InvalidInputException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public InvalidInputException(String errorMessage) {
        super(PlatformErrorMessages.MIMOTO_PGS_INVALID_INPUT_PARAMETER.getCode(), errorMessage);
    }

}
