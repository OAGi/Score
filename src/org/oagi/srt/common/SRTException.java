package org.oagi.srt.common;

import org.chanchan.common.util.ChanChanException;

/**
 *
 * @author Yunsu Lee
 */
public class SRTException extends ChanChanException {

	private static final long serialVersionUID = 5129981872821185460L;

	/** PERSISTENCE ERROR(1400)*/
	public static final int SRT_DATABASE_DRIVER_NOT_FOUND = 1401;//
	public static final int SRT_DATABASE_DRIVER_INSTANTIATION_FAILURE = 1402;//
	public static final int SRT_DATABASE_DRIVER_ACCESS_FAILURE = 1403;//
	public static final int SRT_DATABASE_SQL_EXCEPTION = 1404; //
	public static final int SRT_DATABASE_CONNECTION_NUMBER_FAILURE = 1405;//
	public static final int SRT_DATABASE_POOLNAME_NULL = 1406;
	public static final int SRT_PERSISTENCE_OPERATION_FAILURE = 1407;
	public static final int SRT_DATABASE_CPA_NOT_FOUND = 1408;
	public static final int SRT_DATABASE_MESSAGE_NOT_FOUND = 1409;
	public static final int SRT_DATABASE_PMODE_NOT_FOUND = 1410;

	/** UNKNOWN ERROR(1900) */
	public static final int SRT_UNKNOWN_ERROR = 1901;// Unknown Error

	public SRTException() {
		super();
		_code = SRT_UNKNOWN_ERROR;
	}

	public SRTException(String message) {
		super(message);
		_code = SRT_UNKNOWN_ERROR;
	}

	public SRTException(String msg, Throwable cause) {
		super(msg, cause);
		_code = SRT_UNKNOWN_ERROR;
	}

	public SRTException(Throwable cause) {
		super(cause);
		_code = SRT_UNKNOWN_ERROR;
	}

	public SRTException(int code) {
		super(code);
	}

	public SRTException(int code, String message) {
		super(code, message);
	}

	public SRTException(int code, String message, Throwable cause) {
		super(message, cause);
		_code = code;
	}

	public SRTException(int code, Throwable cause) {
		super(code, cause);
	}

	/**
	 * @return
	 */
	 public int getCode() {
		return _code;
	}

	/**
	 * @param i
	 */
	 public void setCode(int s) {
		 _code = s;
	 }

}

