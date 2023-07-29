package space.sadfox.owlook.base.jaxb;

import jakarta.xml.bind.JAXBException;

public class JAXBEntityValidateException extends JAXBException {

	private static final long serialVersionUID = 1L;

	public JAXBEntityValidateException(String message, String errorCode, Throwable exception) {
		super(message, errorCode, exception);
	}

	public JAXBEntityValidateException(String message, String errorCode) {
		super(message, errorCode);
	}

	public JAXBEntityValidateException(String message, Throwable exception) {
		super(message, exception);
	}

	public JAXBEntityValidateException(String message) {
		super(message);
	}

	public JAXBEntityValidateException(Throwable exception) {
		super(exception);
	}

}
