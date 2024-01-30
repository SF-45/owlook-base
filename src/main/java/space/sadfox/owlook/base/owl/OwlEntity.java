package space.sadfox.owlook.base.owl;

import java.util.List;

import space.sadfox.owlook.base.jaxb.ChangeHistory;
import space.sadfox.owlook.base.jaxb.ChangeHistoryKeeping;

public abstract class OwlEntity implements ChangeHistoryKeeping {
	private final ChangeHistory<OwlEntity> changeHistory;
	private Owl<?> owl;
	
	public OwlEntity() {
		changeHistory = new ChangeHistory<>(this);
	}

	public final ChangeHistory<OwlEntity> getChangeHistory() {
		return changeHistory;
	}
	
	public final Owl<?> getOwl() {
		return owl;
	}
	
	final void  setOwl(Owl<?> owl) {
		this.owl = owl;
	}
	
	@Override
	public abstract List<Object> getProperties();
	
	public String getEntityName() {
		return getClass().getSimpleName();
	}
	
	protected abstract void initialize() throws OwlEntityInitializeException;
	public abstract void syncWith(OwlEntity entity);
}
