package space.sadfox.owlook.base.moduleapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class OwlookModuleInfo {

	static class RequireOwlookModule {

		RequireOwlookModule(List<String> values) {
			this.values = values;
		}
		RequireOwlookModule() {
		}

		@XmlValue
		List<String> values = new ArrayList<>();

	}

	public static class RequireOwlookModuleAdapter extends XmlAdapter<RequireOwlookModule, List<String>> {

		@Override
		public List<String> unmarshal(RequireOwlookModule v) throws Exception {
			return v.values;
		}

		@Override
		public RequireOwlookModule marshal(List<String> v) throws Exception {
			return new RequireOwlookModule(v);
		}

	}

	@XmlElement
	String name;
	@XmlElement
	String moduleName;
	@XmlElement
	String description;
	@XmlElement
	String version;

	@XmlElement(name = "requiresOwlookModules")
	@XmlJavaTypeAdapter(RequireOwlookModuleAdapter.class)
	final List<String> requiresOwlookModules = new ArrayList<>();

	OwlookModuleInfo() {

	}

	public OwlookModuleInfo(String name, String description, String version, String ... requiresOwlookModules) {
		this.name = name;
		this.description = description;
		this.version = version;
		this.requiresOwlookModules.addAll(Arrays.asList(requiresOwlookModules));
	}

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public String version() {
		return version;
	}

	public String moduleName() {
		return moduleName;
	}

	public List<String> requiresOwlookModules() {
		return Collections.unmodifiableList(requiresOwlookModules);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("OwlookModuleInfo\n");
		builder.append("Name = " + name + "\n");
		builder.append("Module Name = " + moduleName + "\n");
		builder.append("Description = " + description + "\n");
		builder.append("Version = " + version + "\n");
		if (requiresOwlookModules.size() > 0) {
			builder.append("Requires Owlook Modules:\n");
			for (String requireModule : requiresOwlookModules) {
				builder.append("\t- " + requireModule + "\n");
			}
		}
		return builder.toString();
	}
	
	

}
