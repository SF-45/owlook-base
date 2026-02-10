package space.sadfox.owlook.base.owl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

@XmlAccessorType(XmlAccessType.NONE)
public class LazyOwlEntity extends OwlEntity {

  private ObservableMap<String, StringProperty> properties = FXCollections.observableHashMap();

  public final HashMap<String, StringProperty> getLazyProperties() {
    return new HashMap<>(properties);
  }

  @XmlJavaTypeAdapter(HashMapAdapter.class)
  final void setLazyProperties(HashMap<String, StringProperty> properties) {
    this.properties = FXCollections.observableMap(properties);
  }

  public final StringProperty getLazyProperty(String key, String defaultValue) {
    if (properties.containsKey(key)) {
      return properties.get(key);
    } else {
      StringProperty defaultValueProperty = new SimpleStringProperty(defaultValue);
      properties.put(key, defaultValueProperty);
      return defaultValueProperty;
    }
  }

  public final void clearLazyProperties() {
    properties.clear();
  }

  @Override
  public List<Object> getProperties() {
    var prop = new ArrayList<>();
    prop.add(properties);
    return prop;
  }

  @Override
  protected void initialize() throws OwlEntityInitializeException {}

  @Override
  public void syncWith(OwlEntity entity) {
    if (!(entity instanceof LazyOwlEntity)) {
      return;
    }
    LazyOwlEntity low = (LazyOwlEntity) entity;
    properties.clear();
    low.properties.forEach((key, value) -> {
      properties.put(key, new SimpleStringProperty(value.get()));
    });
  }
}
