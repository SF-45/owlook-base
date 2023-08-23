package space.sadfox.owlook.base.jaxb;

import java.io.IOException;

public class UnlocatedEntityException extends IOException {

	private static final long serialVersionUID = -8761067259355447517L;

	public UnlocatedEntityException() {
		super();
	}

	public UnlocatedEntityException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnlocatedEntityException(String message) {
		super(message);
	}

	public UnlocatedEntityException(Throwable cause) {
		super(cause);
	}
	

}
