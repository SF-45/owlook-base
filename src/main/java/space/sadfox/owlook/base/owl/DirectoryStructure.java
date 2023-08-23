package space.sadfox.owlook.base.owl;

enum DirectoryStructure {
	INFO_DIR("META-INF/"),
	RESOURCES_DIR("res/"),
	
	ENTITY_FILE("entity"),
	HEAD_FILE("head"),
	INFO_FILE(DirectoryStructure.INFO_DIR.get() + "OWL_INFO");

	private String value;

	private DirectoryStructure(String s) {
		value = s;
	}

	public String get() {
		return value;
	}
}
