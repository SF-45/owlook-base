package space.sadfox.owlook.base;

public enum Extensions {
	OWL(".owl"),
	OWLOOK_MODULE_PACK(".owlm");
	
	private String extension;
	
	private Extensions(String s) {
		extension = s;
	}
	
	public String get() {
		return extension;
	}
	
	@Override
	public String toString() {
		return extension;
	}

}
