module owlook.base {
	exports space.sadfox.owlook.base.jaxb;
	exports space.sadfox.owlook.base.moduleapi;
	
	opens space.sadfox.owlook.base.moduleapi to jakarta.xml.bind;

	requires transitive jakarta.xml.bind;
	requires transitive javafx.base;
}