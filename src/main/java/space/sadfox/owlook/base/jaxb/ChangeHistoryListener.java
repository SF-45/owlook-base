package space.sadfox.owlook.base.jaxb;

@FunctionalInterface
public interface ChangeHistoryListener<E extends ChangeHistoryKeeping> {
	public abstract static class Change<E> {
		public abstract boolean wasModify();
		public abstract boolean wasUndo();
		public abstract boolean wasRedo();
		public abstract E getParent();
	}
	
	void change(Change<E> change);
}
