package space.sadfox.owlook.base.moduleapi;

import java.util.List;

import space.sadfox.owlook.base.jaxb.JAXBEntity;


public interface OwlookModule {
	String getModuleDescription();
	String getModuleVersion();
	void initModule();
	List<Class<? extends JAXBEntity>> getJaxbEntities() throws ModuleHasNoProvideEntities;
	Class<? extends JAXBEntity> getConfigTarget() throws ModuleHasNoConfiguration;
}
