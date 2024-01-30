package space.sadfox.owlook.base.moduleapi;

import space.sadfox.owlook.base.jaxb.ObservedJAXBEntity;


public interface OwlookModule {
	String getModuleDescription();
	String getModuleVersion();
	void initModule();
	Class<? extends ObservedJAXBEntity> getConfigTarget() throws ModuleHasNoConfiguration;
}
