package space.sadfox.owlook.base.moduleapi;

public class PackageException extends Exception {

	private static final long serialVersionUID = 1756580511739998673L;

	public PackageException() {
		super();
	}

	public PackageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PackageException(String message, Throwable cause) {
		super(message, cause);
	}

	public PackageException(String message) {
		super(message);
	}

	public PackageException(Throwable cause) {
		super(cause);
	}

}
