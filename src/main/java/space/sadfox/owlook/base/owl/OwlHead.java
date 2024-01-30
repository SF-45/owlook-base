package space.sadfox.owlook.base.owl;

import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import space.sadfox.owlook.base.jaxb.ChangeHistory;
import space.sadfox.owlook.base.jaxb.ChangeHistoryKeeping;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OwlHead implements ChangeHistoryKeeping {
	
	private final ChangeHistory<OwlHead> changeHistory;
	private HollowOwl hollowOwl;
	
	private final StringProperty title = new SimpleStringProperty();
	
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
		//TODO:
	}

	@Override
	public String toString() {
		return "OwlHead [getTitle()=" + getTitle() + "]";
	}

	
	
	
	
}
