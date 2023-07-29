package space.sadfox.owlook.base.moduleapi;

public class ModuleHasNoConfiguration extends Exception {

	private static final long serialVersionUID = 5890139728817160258L;

	public ModuleHasNoConfiguration() {
		super();
	}

	public ModuleHasNoConfiguration(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ModuleHasNoConfiguration(String message, Throwable cause) {
		super(message, cause);
	}

	public ModuleHasNoConfiguration(String message) {
		super(message);
	}

	public ModuleHasNoConfiguration(Throwable cause) {
		super(cause);
	}
	

}
