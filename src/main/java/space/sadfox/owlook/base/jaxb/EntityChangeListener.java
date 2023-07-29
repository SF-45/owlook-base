package space.sadfox.owlook.base.jaxb;


@FunctionalInterface
public interface EntityChangeListener {
	public abstract static class Change {
		public abstract boolean wasModify();
		public abstract boolean wasRemoved();
		public abstract JAXBEntity getEntity();
	}

	void change(Change change);
}
