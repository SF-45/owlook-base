package space.sadfox.owlook.base.jaxb;

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
import space.sadfox.owlook.base.jaxb.EntityChangeListener.Change;

public class ChangeHistory {

	private final JAXBEntity parent;
	private final Stack<Changer> back = new Stack<>();
	private final Stack<Changer> forward = new Stack<>();
	private final BooleanProperty dontlisen = new SimpleBooleanProperty(false);
	
	public ChangeHistory(JAXBEntity parent) {
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
			notifyListeners();

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
			notifyListeners();
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
			notifyListeners();
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
			notifyListeners();
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
		notifyListeners();
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
		notifyListeners();
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

	private void notifyListeners() {
		parent.notifyEntityChangeListeners(new Change() {
			
			@Override
			public boolean wasRemoved() {
				return false;
			}
			
			@Override
			public boolean wasModify() {
				return true;
			}

			@Override
			public JAXBEntity getEntity() {
				return parent;
			}
		});
	}

}
