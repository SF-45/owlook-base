package space.sadfox.owlook.base.jaxb;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBException;

public abstract class JAXBEntity implements ChangeHistoryKeeping {

	private JAXBHelper<?> jaxbHelper;
	private ChangeHistory changeHistory;
	private List<EntityChangeListener> changeListeners = new ArrayList<>();

	public JAXBHelper<?> getJaxbHelper() {
		return jaxbHelper;
	}

	void setJaxbHelper(JAXBHelper<?> jaxbHelper) {
		this.jaxbHelper = jaxbHelper;
	}

	public final Path getPath() {
		return getJaxbHelper().getPath();
	}

	public final ChangeHistory getChangeHistory() {
		if (changeHistory == null)
			changeHistory = new ChangeHistory(this);
		return changeHistory;
	}

	public final void save() throws JAXBException, IOException {
		getJaxbHelper().save();
	}

	public final String getFileName() {
		String fileName = getPath().getFileName().toString();
		int ind = fileName.lastIndexOf(".");
		return fileName.substring(0, ind);
	}

	public abstract String getTitle();

	public abstract void setTitle(String title);

	public abstract void validate() throws JAXBEntityValidateException;

	public abstract void initialize();

	public abstract void syncWith(JAXBEntity entity);

	public final void addEntityChangeListener(EntityChangeListener listener) {
		changeListeners.add(listener);
	}

	public final void removeEntityChangeListener(EntityChangeListener listener) {
		changeListeners.remove(listener);
	}

	public final void notifyEntityChangeListeners(EntityChangeListener.Change change) {
		changeListeners.forEach(listener -> listener.change(change));
	}

}
