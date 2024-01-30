package space.sadfox.owlook.base.jaxb;

import java.util.Date;
import java.util.List;

public abstract class ObservedJAXBEntity extends JAXBEntity implements ChangeHistoryKeeping {
	
	@FunctionalInterface
	public interface SaveExceptionHandler {
		void handle(Exception exception);
	}
	
	private ChangeHistory<ObservedJAXBEntity> changeHistory;
	private ChangeHistoryListener<ObservedJAXBEntity> autosaveAction;

	private SaveExceptionHandler saveExceptionHandler;

	public abstract List<Object> getProperties();
	
	public void enableAutoSave(SaveExceptionHandler handler) {
		saveExceptionHandler = handler;
		changeHistory.addListener(autosaveAction);
	}

	public void disableAutoSave() {
		changeHistory.removeListener(autosaveAction);
		saveExceptionHandler = null;
	}

	public boolean isEnableAutoSave() {
		return saveExceptionHandler == null;
	}
	
	public ChangeHistory<ObservedJAXBEntity> getChangeHistory() {
		return changeHistory; 
	}

	@Override
	protected void initialization() {
		changeHistory = new ChangeHistory<>(this);
		autosaveAction = change -> {
			if (saveExceptionHandler != null) {
				try {
					save();
				} catch (Exception e) {
					saveExceptionHandler.handle(e);
				}
			}
		};
	}
	
	
}
