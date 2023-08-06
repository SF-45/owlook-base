package space.sadfox.owlook.base.moduleapi;

import java.util.Objects;

public class VersionFormat implements Comparable<VersionFormat> {

	private final Integer compatibility;
	private final Integer primary;
	private final Integer secondary;
	private final String suffix;

	

	public VersionFormat(int compatibility, int primary, int secondary, String suffix) {
		this.compatibility = compatibility;
		this.primary = primary;
		this.secondary = secondary;
		this.suffix = suffix;
	}
	
	/**
	 * 
	 * @throws IllegalArgumentException 
	 * 	if format doesn't fit the x.x.xxx-suf
	 * @throws java.lang.NumberFormatException 
	 * 	if the version has letters outside the suffix
	 */
	public static VersionFormat of(String version) {
		String[] comp = version.split("\\.");
		

		if (comp.length != 3) {
			throw new IllegalArgumentException("[" + version + "] doesn't fit the format x.x.x[-suffix]");
		}

		int compatibility = 0;
		int primary = 0;
		int secondary = 0;
		String suffix = "";
		
		compatibility = Integer.parseInt(comp[0]);
		primary = Integer.parseInt(comp[1]);
		if (comp[2].contains("-")) {
			String[] secondaryComp = comp[2].split("-");
			secondary = Integer.parseInt(secondaryComp[0]);
			suffix = secondaryComp[1];
		} else {
			secondary = Integer.parseInt(comp[2]);
			suffix = "";
		}
		
		return new VersionFormat(compatibility, primary, secondary, suffix);
	}



	public int compatibility() {
		return compatibility;
	}

	public int primary() {
		return primary;
	}

	public int secondary() {
		return secondary;
	}

	public String suffix() {
		return suffix;
	}
	
	public boolean compatibleWith(VersionFormat o) {
		return compatibility.equals(o.compatibility);
	}
	
	public boolean isRelease() {
		return suffix.equals("");
	}

	@Override
	public int compareTo(VersionFormat o) {
		if (compatibility.compareTo(o.compatibility) != 0) {
			return compatibility.compareTo(o.compatibility);
		}
		if (primary.compareTo(o.primary) != 0) {
			return primary.compareTo(o.primary);
		}
		if (secondary.compareTo(o.secondary) != 0 ) {
			return secondary.compareTo(o.secondary);
		}
		if (suffix.equals("") && !o.suffix.equals("")) {
			return 1;
		}
		if (!suffix.equals("") && o.suffix.equals("")) {
			return -1;
		}
		return suffix.compareTo(o.suffix);

	}

	@Override
	public int hashCode() {
		return 17 + Objects.hash(compatibility, primary, secondary, suffix);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionFormat other = (VersionFormat) obj;
		return Objects.equals(compatibility, other.compatibility) && Objects.equals(primary, other.primary)
				&& Objects.equals(secondary, other.secondary) && Objects.equals(suffix, other.suffix);
	}
	
	@Override
	public String toString() {
		String suf = suffix == "" ? "" : "-" + suffix;
		return String.join(".", compatibility.toString(), primary.toString(), secondary.toString()) + suf;
	}
	
	
	
	

}
