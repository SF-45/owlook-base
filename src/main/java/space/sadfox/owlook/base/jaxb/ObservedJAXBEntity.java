package space.sadfox.owlook.base.jaxb;

import java.util.List;

public abstract class ObservedJAXBEntity extends JAXBEntity2 implements ChangeHistoryKeeping {
	
	@FunctionalInterface
	public interface SaveExceptionHandler {
		void handle(Exception exception);
	}
	
	private ChangeHistory<ObservedJAXBEntity> changeHistory;
	private final ChangeHistoryListener<ObservedJAXBEntity> autosaveAction;

	private SaveExceptionHandler saveExceptionHandler;
	
	public ObservedJAXBEntity() {
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
}
