package space.sadfox.owlook.base.moduleapi;

public interface OwlookModuleComponent {
	default String getIdentifier() {
		return this.getClass().getName();
	}
	String getComponentName();
	String getComponentDescription();

}
