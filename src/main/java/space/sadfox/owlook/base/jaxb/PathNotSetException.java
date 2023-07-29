package space.sadfox.owlook.base.jaxb;

import java.io.IOException;

public class PathNotSetException extends IOException {

	private static final long serialVersionUID = -8761067259355447517L;

	public PathNotSetException() {
		super();
	}

	public PathNotSetException(String message, Throwable cause) {
		super(message, cause);
	}

	public PathNotSetException(String message) {
		super(message);
	}

	public PathNotSetException(Throwable cause) {
		super(cause);
	}
	

}
