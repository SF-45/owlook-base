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

    public Element() {}

    public Element(String key, String value) {
      this.key = key;
      this.value = value;
    }

    @XmlAttribute
    public String key;
    @XmlValue
    public String value;
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
