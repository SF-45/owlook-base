package space.sadfox.owlook.base.owl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import space.sadfox.owlook.base.jaxb.ChangeHistory;
import space.sadfox.owlook.base.jaxb.ChangeHistoryKeeping;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OwlHead implements ChangeHistoryKeeping {

  private final ChangeHistory<OwlHead> changeHistory;
  private HollowOwl hollowOwl;

  private final StringProperty title = new SimpleStringProperty("");
  private final ObjectProperty<UUID> parentOwl = new SimpleObjectProperty<>();

  public OwlHead() {
    changeHistory = new ChangeHistory<>(this);
  }

  @XmlElement
  public String getTitle() {
    return title.get();
  }

  public void setTitle(String title) {
    this.title.set(title);
  }

  public StringProperty titleProperty() {
    return title;
  }

  public ObjectProperty<UUID> parentOwlProperty() {
    return parentOwl;
  }

  @XmlElement
  @XmlJavaTypeAdapter(UUIDXmlAdapter.class)
  public UUID getParentOwl() {
    return parentOwl.get();
  }

  public void setParentOwl(UUID parentOwl) {
    this.parentOwl.set(parentOwl);
  }

  public ChangeHistory<OwlHead> getChangeHistory() {
    return changeHistory;
  }

  public HollowOwl getHollowOwl() {
    return hollowOwl;
  }

  void setHollowOwl(HollowOwl hollowOwl) {
    this.hollowOwl = hollowOwl;
  }

  @Override
  public List<Object> getProperties() {
    return Arrays.asList(title);
  };

  public void syncWith(OwlHead owlHead) {
    // TODO:
  }

  @Override
  public String toString() {
    return "OwlHead [getTitle()=" + getTitle() + "]";
  }



}
