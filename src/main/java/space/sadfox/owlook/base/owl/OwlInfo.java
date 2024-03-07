package space.sadfox.owlook.base.owl;

import java.util.UUID;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
  @XmlElement
  @XmlJavaTypeAdapter(UUIDXmlAdapter.class)
  UUID id = UUID.randomUUID();

  OwlInfo() {}

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

  public UUID id() {
    return id;
  }

  @Override
  public String toString() {
    return "OwlInfo{createdModule=" + createdModule + ", targetClass=" + targetClass + ", owlName="
        + owlName + ", id=" + id + "}";
  }
}
