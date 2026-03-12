package space.sadfox.owlook.base.owl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class LazyOwlEntity extends OwlEntity {

  private ObservableMap<String, StringProperty> properties = FXCollections.observableHashMap();
  private final ObservableMap<Object, ObjectProperty<Object>> cache = FXCollections.observableHashMap();

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

  public final ObjectProperty<Object> getCachePropery(Object key) {
    return cache.get(key);
  }

  public final Object getCacheValue(Object key) {
    return isCached(key) ? getCachePropery(key).get() : null;
  }

  public final boolean isCached(Object key) {
    return cache.containsKey(key);
  }

  public final ObjectProperty<?> setCacheValue(Object key, Object value) {
    if (isCached(key)) {
      final var cacheProperty = getCachePropery(key);
      cacheProperty.set(value);
      return cacheProperty;
    } else {
      final var cacheProperty = new SimpleObjectProperty<>(value);
      cache.put(key, cacheProperty);
      return cacheProperty;
    }
  }

  @Override
  public List<Object> getProperties() {
    var prop = new ArrayList<>();
    prop.add(properties);
    return prop;
  }

  @Override
  protected void initialize() throws OwlEntityInitializeException {
  }

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
