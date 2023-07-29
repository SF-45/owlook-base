package space.sadfox.owlook.base.moduleapi;

public class OwlookModuleException extends Exception {

	private static final long serialVersionUID = 8560009537100244040L;

	public OwlookModuleException() {
		super();
	}

	public OwlookModuleException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public OwlookModuleException(String message, Throwable cause) {
		super(message, cause);
	}

	public OwlookModuleException(String message) {
		super(message);
	}

	public OwlookModuleException(Throwable cause) {
		super(cause);
	}

}
