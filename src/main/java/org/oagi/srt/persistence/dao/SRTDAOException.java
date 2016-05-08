package org.oagi.srt.persistence.dao;

import org.oagi.srt.common.SRTException;

/**
 *
 * @author Yunsu Lee
 * @version 1.0
 */
public class SRTDAOException extends SRTException {

	private static final long serialVersionUID = 2916700676430894950L;
	
	private final static int OFFSET = 1000;

	public SRTDAOException() {
		super();
		_code = DAO_UNKNOWN_ERROR;
	}

	public SRTDAOException(String msg) {
		super(msg);
		_code = DAO_UNKNOWN_ERROR;
	}

	public SRTDAOException(int code) {
		super(code, getErrorString(code));
	}

    public SRTDAOException(int code, Throwable cause) {
        super(cause);
        _code = code;
    }

	public final static int DAO_UNKNOWN_ERROR = OFFSET;
	public final static int DAOFACTORY_NOT_FOUND = OFFSET + 1;
	public final static int DAO_CLASS_NOT_FOUND = OFFSET + 2;
	public final static int DAO_CLASS_INSTANTIATION_FAILED = OFFSET + 3;
	public final static int DAO_CLASS_ILLEGAL_ACCESS = OFFSET + 4;
	public final static int INVALID_OBJECT_FOR_DAO_OPERATION = OFFSET + 5;
	public final static int DAO_INSERT_ERROR = OFFSET + 6;
	public final static int DAO_FIND_ERROR = OFFSET + 7;
	public final static int DAO_UPDATE_ERROR = OFFSET + 8;
	public final static int DAO_DELETE_ERROR = OFFSET + 9;
	public final static int SQL_EXECUTION_FAILED = OFFSET + 10;
	public final static int CPA_FILE_SAVE_FAILURE = OFFSET + 11;
	public final static int MESSAGE_FILE_SAVE_FAILURE = OFFSET + 12;
	public final static int UNKNOW_OBJECT_TYPE_FOR_UPDATE = OFFSET + 13;
	public final static int NULL_FILE_CONTENT = OFFSET + 14;
	public final static int WSDL_FILE_SAVE_FAILURE = OFFSET + 15;
	public final static int NO_SUCH_SQL = OFFSET + 16;
	public final static int SUPERCLASS_METHOD_CALLED = OFFSET + 17;
	public final static int INVALID_PARAMETER_FOR_EXECUTE_QUERY = OFFSET + 18;
	public final static int INVALID_PARAMETER_FOR_EXECUTE_UPDATE = OFFSET + 19;
	public final static int PMODE_FILE_SAVE_FAILURE = OFFSET + 20;
	public static final String[] _errString = {
		"Unknown DAO Exception",					// + 0
		"DAOFactory not found",						// + 1
		"DAO class not found",						// + 2
		"DAO class instantiation failed",			// + 3
		"Illegal access to DAO class",				// + 4
		"Invalid object for DAO operation",			// + 5
		"DAO INSERT operation failed",				// + 6
		"DAO FIND operation failed",				// + 7
		"DAO UPDATE operation failed",				// + 8
		"DAO DELETE operation failed",				// + 9
		"SQL execution failed",						// + 10
		"Failed to save CPA file",					// + 11
		"Failed to save Message file",				// + 12
		"Unknown object type for update",			// + 13
		"File content is null",						// + 14
		"Failed to save WSDL file",					// + 15
		"No such SQL exist",						// + 16
		"Method in superclass called. Please override this method",	// + 17
		"Invalid parameter for executeQuery",						// + 18
		"Invalid parameter for executeUpdate",						// + 19
		"Failed to save PMODE file"
	};

	public String getErrorString() {
		return getErrorString(_code);
	}

	public static String getErrorString(int code) {
		String errStr = "Unknown MXS Exception";
		// DB
		if (code >= DAO_UNKNOWN_ERROR
				&& code <= PMODE_FILE_SAVE_FAILURE) {
			int normalize = code - OFFSET;
			errStr = _errString[normalize];
		}

		return errStr;
	}
}
