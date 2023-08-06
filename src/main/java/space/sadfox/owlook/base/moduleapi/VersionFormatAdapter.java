package space.sadfox.owlook.base.moduleapi;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class VersionFormatAdapter extends XmlAdapter<String, VersionFormat> {

	@Override
	public VersionFormat unmarshal(String v) throws Exception {
		return VersionFormat.of(v);
	}

	@Override
	public String marshal(VersionFormat v) throws Exception {
		return v.toString();
	}


}
