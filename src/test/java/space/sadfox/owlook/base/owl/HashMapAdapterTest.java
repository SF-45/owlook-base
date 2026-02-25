package space.sadfox.owlook.base.owl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;

import org.junit.jupiter.api.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import space.sadfox.owlook.base.owl.HashMapAdapter.Element;
import space.sadfox.owlook.base.owl.HashMapAdapter.Elements;

public class HashMapAdapterTest {

  private final HashMapAdapter hsAdapter = new HashMapAdapter();
  private final Elements elements = new Elements();
  private final HashMap<String, StringProperty> hashMap = new HashMap<>();

  HashMapAdapterTest() {
    putElemet("key1", "value1");
    putElemet("key2", "value2");
    putElemet("key3", "value3");
  }

  private void putElemet(String key, String value) {
    elements.elements.add(new Element(key, value));
    hashMap.put(key, new SimpleStringProperty(value));
  }

  @Test
  void testUnmarshal() {
    try {
      var unmarshalHashMap = hsAdapter.unmarshal(elements);
      assertEquals(unmarshalHashMap.get("key1").get(), "value1");
      assertEquals(unmarshalHashMap.get("key2").get(), "value2");
      assertEquals(unmarshalHashMap.get("key3").get(), "value3");
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  void testMarshal() {
    try {
      var marshalElements = hsAdapter.marshal(hashMap);
      assertEquals(elements.elements, marshalElements.elements);
    } catch (Exception e) {
      fail(e);
    }
  }

}
