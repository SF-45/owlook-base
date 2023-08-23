package space.sadfox.owlook.base.moduleapi;

import java.util.List;

import space.sadfox.owlook.base.jaxb.JAXBEntity2;
import space.sadfox.owlook.base.owl.OwlEntity;


public interface OwlookModule {
	String getModuleDescription();
	String getModuleVersion();
	void initModule();
	List<Class<? extends OwlEntity>> getOwlEntities() throws ModuleHasNoProvideEntities;
	Class<? extends JAXBEntity2> getConfigTarget() throws ModuleHasNoConfiguration;
}
