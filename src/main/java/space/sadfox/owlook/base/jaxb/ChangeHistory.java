package space.sadfox.owlook.base.jaxb;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import space.sadfox.owlook.base.jaxb.ChangeHistoryListener.Change;

public class ChangeHistory<E extends ChangeHistoryKeeping> {

	private final E parent;
	private final Stack<Changer> back = new Stack<>();
	private final Stack<Changer> forward = new Stack<>();
	private final BooleanProperty dontlisen = new SimpleBooleanProperty(false);
	
	private final List<ChangeHistoryListener<E>> listeners = new ArrayList<>();
	
	public ChangeHistory(E parent) {
		this.parent = parent;
		register(parent);
	}

	private <T> void register(Property<T> property) {

		checkAndRegister(property.getValue());
		property.addListener((property2, oldValue, newValue) -> {
			if (dontlisen.get())
				return;
			back.push(new Changer() {

				@Override
				public void undo() {
					property.setValue(oldValue);

				}

				@Override
				public void todo() {
					property.setValue(newValue);

				}
			});
			forward.clear();
			notifyWasModify();

		});
	}

	private <T> void register(ObservableList<T> observableList) {

		observableList.forEach(this::checkAndRegister);

		observableList.addListener((ListChangeListener<Object>) change -> {
			while (change.next()) {
				if (change.wasAdded()) {
					change.getAddedSubList().forEach(this::checkAndRegister);
				}
			}
		});
		observableList.addListener((ListChangeListener<T>) change -> {
			if (dontlisen.get())
				return;
			while (change.next()) {
				if (change.wasAdded()) {
					change.getAddedSubList().forEach(element -> {
						back.push(new Changer() {

							@Override
							public void undo() {
								observableList.remove(element);
							}

							@Override
							public void todo() {
								observableList.add(element);
							}
						});
					});
				}
				if (change.wasRemoved()) {
					change.getRemoved().forEach(element -> {
						back.push(new Changer() {

							@Override
							public void undo() {
								observableList.add(element);
							}

							@Override
							public void todo() {
								observableList.remove(element);
							}
						});
					});
				}
			}
			forward.clear();
			notifyWasModify();
		});
	}

	private <K, V> void register(ObservableMap<K, V> observableMap) {

		observableMap.forEach((k, v) -> {
			checkAndRegister(k);
			checkAndRegister(v);
		});

		observableMap.addListener((MapChangeListener<Object, Object>) change -> {
			if (change.wasAdded()) {
				checkAndRegister(change.getKey());
				checkAndRegister(change.getValueAdded());
			}
		});

		observableMap.addListener((MapChangeListener<K, V>) change -> {
			if (change.wasAdded()) {
				back.push(new Changer() {

					@Override
					public void undo() {
						observableMap.remove(change.getKey(), change.getValueAdded());

					}

					@Override
					public void todo() {
						observableMap.put(change.getKey(), change.getValueAdded());

					}
				});
			}
			if (change.wasRemoved()) {
				back.push(new Changer() {

					@Override
					public void undo() {
						observableMap.put(change.getKey(), change.getValueRemoved());

					}

					@Override
					public void todo() {
						observableMap.remove(change.getKey(), change.getValueRemoved());

					}
				});
			}
			forward.clear();
			notifyWasModify();
		});
	}

	private <T> void register(ObservableSet<T> observableSet) {

		observableSet.forEach(this::checkAndRegister);
		observableSet.addListener((SetChangeListener<Object>) change -> {
			if (change.wasAdded()) {
				checkAndRegister(change.getElementAdded());
			}
		});

		observableSet.addListener((SetChangeListener<T>) change -> {
			if (change.wasAdded()) {
				back.push(new Changer() {

					@Override
					public void undo() {
						observableSet.remove(change.getElementAdded());

					}

					@Override
					public void todo() {
						observableSet.add(change.getElementAdded());

					}
				});
			}
			if (change.wasRemoved()) {
				back.push(new Changer() {

					@Override
					public void undo() {
						observableSet.add(change.getElementRemoved());

					}

					@Override
					public void todo() {
						observableSet.remove(change.getElementRemoved());

					}
				});
			}
			forward.clear();
			notifyWasModify();
		});
	}

	private void register(ChangeHistoryKeeping entity) {
		if (entity.getProperties() != null) {
			entity.getProperties().forEach(this::checkAndRegister);
		}
	}

	private void checkAndRegister(Object o) {
		if (o instanceof ChangeHistoryKeeping) {
			register((ChangeHistoryKeeping) o);
		} else if (o instanceof Property<?>) {
			register((Property<?>) o);
		} else if (o instanceof ObservableList<?>) {
			register((ObservableList<?>) o);
		} else if (o instanceof ObservableMap<?, ?>) {
			register((ObservableMap<?, ?>) o);
		} else if (o instanceof ObservableSet<?>) {
			register((ObservableSet<?>) o);
		}
	}

	public void back() {
		if (back.size() == 0)
			return;
		Changer changer = back.pop();
		dontlisen.set(true);
		changer.undo();
		dontlisen.set(false);
		forward.push(changer);
		notifyListeners(new Change<E>() {

			@Override
			public boolean wasModify() {
				return false;
			}

			@Override
			public boolean wasUndo() {
				return true;
			}

			@Override
			public boolean wasRedo() {
				return false;
			}

			@Override
			public E getParent() {
				return parent;
			}

		} );
	}

	public void allBack() {
		for (int i = 0; i < getBackSize(); i++) {
			back();
		}
	}

	public int getBackSize() {
		return back.size();
	}

	public void forward() {
		if (forward.size() == 0)
			return;
		Changer changer = forward.pop();
		dontlisen.set(true);
		changer.todo();
		dontlisen.set(false);
		back.push(changer);
		notifyListeners(new Change<E>() {

			@Override
			public boolean wasModify() {
				return false;
			}

			@Override
			public boolean wasUndo() {
				return false;
			}

			@Override
			public boolean wasRedo() {
				return true;
			}

			@Override
			public E getParent() {
				return parent;
			}

		});
	}

	public void allForward() {
		for (int i = 0; i < getForwardSize(); i++) {
			forward();
		}
	}

	public int getForwardSize() {
		return forward.size();
	}

	public void forgetСhanges() {
		back.clear();
		forward.clear();
	}
	
	public void addListener(ChangeHistoryListener<E> listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ChangeHistoryListener<E> listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(ChangeHistoryListener.Change<E> change) {
		listeners.forEach(listener -> listener.change(change));
	}
	
	private void notifyWasModify() {
		notifyListeners(new Change<E>() {

			@Override
			public boolean wasModify() {
				return true;
			}

			@Override
			public boolean wasUndo() {
				return false;
			}

			@Override
			public boolean wasRedo() {
				return false;
			}

			@Override
			public E getParent() {
				return parent;
			}

		});
	}

}
