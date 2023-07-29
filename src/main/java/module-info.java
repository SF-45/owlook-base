module owlook.base {
	exports space.sadfox.owlook.base.jaxb;
	exports space.sadfox.owlook.base.moduleapi;

	requires transitive jakarta.xml.bind;
	requires transitive javafx.base;
}