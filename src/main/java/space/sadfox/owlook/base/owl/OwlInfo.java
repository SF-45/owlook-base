package space.sadfox.owlook.base.owl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class OwlInfo {
	@XmlElement
	String createdModule;
	@XmlElement
	Long createdTime;
	@XmlElement
	String targetClass;
	@XmlElement
	String owlName;
	
	OwlInfo() {
	}
	
	public String createdModule() {
		return createdModule;
	}
	
	public Long createdTime() {
		return createdTime;
	}
	
	public String targetClass() {
		return targetClass;
	}
	
	public String owlName() {
		return owlName;
	}
}
