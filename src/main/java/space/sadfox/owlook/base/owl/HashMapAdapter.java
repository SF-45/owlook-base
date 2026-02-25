package space.sadfox.owlook.base.owl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import space.sadfox.owlook.base.owl.HashMapAdapter.Elements;

public class HashMapAdapter extends XmlAdapter<Elements, HashMap<String, StringProperty>> {
  public static class Element {

    public Element() {
    }

    public Element(String key, String value) {
      this.key = key;
      this.value = value;
    }

    @XmlAttribute
    public String key;
    @XmlValue
    public String value;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Element other = (Element) obj;
      if (key == null) {
        if (other.key != null)
          return false;
      } else if (!key.equals(other.key))
        return false;
      if (value == null) {
        if (other.value != null)
          return false;
      } else if (!value.equals(other.value))
        return false;
      return true;
    }
  }

  public static class Elements {
    @XmlElement(name = "property")
    public List<Element> elements = new ArrayList<>();
  }

  @Override
  public HashMap<String, StringProperty> unmarshal(Elements v) throws Exception {
    HashMap<String, StringProperty> hashMap = new HashMap<>();
    v.elements.forEach(element -> {
      hashMap.put(element.key, new SimpleStringProperty(element.value));
    });

    return hashMap;
  }

  @Override
  public Elements marshal(HashMap<String, StringProperty> v) throws Exception {
    Elements elements = new Elements();

    v.forEach((key, value) -> {
      elements.elements.add(new Element(key, value.get()));
    });

    return elements;
  }

}
