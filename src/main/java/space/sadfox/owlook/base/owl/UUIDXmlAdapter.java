package space.sadfox.owlook.base.owl;

import java.util.UUID;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class UUIDXmlAdapter extends XmlAdapter<String, UUID> {

  @Override
  public UUID unmarshal(String v) throws Exception {
    return UUID.fromString(v);
  }

  @Override
  public String marshal(UUID v) throws Exception {
    return v.toString();
  }


}
