package space.sadfox.owlook.base.jaxb;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
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

  private final Map<Object, Runnable> unsubscribeActions = new IdentityHashMap<>();

  private final List<ChangeHistoryListener<E>> listeners = new ArrayList<>();

  public ChangeHistory(E parent) {
    this.parent = parent;
    register(parent);
  }

  private <T> void register(Property<T> property) {

    checkAndRegister(property.getValue());
    final ChangeListener<T> chListener = (observable, oldValue, newValue) -> {
      checkAndRegister(newValue);
      unsubscribeProperty(oldValue);
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

    };
    unsubscribeActions.put(property, () -> property.removeListener(chListener));
    property.addListener(chListener);
  }

  private <T> void register(ObservableList<T> observableList) {

    observableList.forEach(this::checkAndRegister);

    final ListChangeListener<T> listChListener = change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          change.getAddedSubList().forEach(this::checkAndRegister);
        }
        if (change.wasRemoved()) {
          change.getRemoved().forEach(this::unsubscribeProperty);
        }
        if (dontlisen.get()) {
          return;
        }
        if (change.wasAdded()) {
          final int from = change.getFrom();
          final var addedSubList = new ArrayList<>(change.getAddedSubList());
          back.push(new Changer() {

            @Override
            public void undo() {
              observableList.removeAll(addedSubList);
            }

            @Override
            public void todo() {
              observableList.addAll(from, addedSubList);
            }
          });
        }
        if (change.wasRemoved()) {
          final int from = change.getFrom();
          final var removed = new ArrayList<>(change.getRemoved());
          back.push(new Changer() {

            @Override
            public void undo() {
              observableList.addAll(from, removed);
            }

            @Override
            public void todo() {
              observableList.removeAll(removed);
            }
          });
        }
        forward.clear();
        notifyWasModify();
      }
    };
    unsubscribeActions.put(observableList, () -> observableList.removeListener(listChListener));
    observableList.addListener(listChListener);
  }

  private <K, V> void register(ObservableMap<K, V> observableMap) {
    observableMap.forEach((k, v) -> {
      checkAndRegister(k);
      checkAndRegister(v);
    });

    final MapChangeListener<K, V> mapChListener = change -> {
      if (change.wasAdded() && !change.wasRemoved()) {
        checkAndRegister(change.getKey());
        checkAndRegister(change.getValueAdded());
      } else if (change.wasAdded() && change.wasRemoved()) {
        unsubscribeProperty(change.getValueRemoved());
      } else if (!change.wasAdded() && change.wasRemoved()) {
        unsubscribeProperty(change.getKey());
        unsubscribeProperty(change.getValueRemoved());
      }

      if (dontlisen.get()) {
        return;
      }

      if (change.wasAdded() && !change.wasRemoved()) {
        final var key = change.getKey();
        final var value = change.getValueAdded();
        back.push(new Changer() {

          @Override
          public void undo() {
            observableMap.remove(key);
          }

          @Override
          public void todo() {
            observableMap.put(key, value);
          }

        });
      } else if (change.wasAdded() && change.wasRemoved()) {
        final var key = change.getKey();
        final var newValue = change.getValueAdded();
        final var oldValue = change.getValueRemoved();
        back.push(new Changer() {

          @Override
          public void undo() {
            observableMap.put(key, oldValue);
          }

          @Override
          public void todo() {
            observableMap.put(key, newValue);
          }

        });
      } else if (!change.wasAdded() && change.wasRemoved()) {
        final var key = change.getKey();
        final var value = change.getValueRemoved();
        back.push(new Changer() {

          @Override
          public void undo() {
            observableMap.put(key, value);
          }

          @Override
          public void todo() {
            observableMap.remove(key);
          }

        });
      }
      forward.clear();
      notifyWasModify();
    };
    unsubscribeActions.put(observableMap, () -> observableMap.removeListener(mapChListener));
    observableMap.addListener(mapChListener);
  }

  private <T> void register(ObservableSet<T> observableSet) {

    observableSet.forEach(this::checkAndRegister);
    final SetChangeListener<T> setChListener = change -> {
      if (change.wasAdded()) {
        checkAndRegister(change.getElementAdded());
      } else if (change.wasRemoved()) {
        unsubscribeProperty(change.getElementRemoved());
      }

      if (dontlisen.get()) {
        return;
      }

      if (change.wasAdded()) {
        final var newValue = change.getElementAdded();
        back.push(new Changer() {

          @Override
          public void undo() {
            observableSet.remove(newValue);
          }

          @Override
          public void todo() {
            observableSet.add(newValue);
          }

        });
      } else if (change.wasRemoved()) {
        final var oldValue = change.getElementRemoved();
        back.push(new Changer() {

          @Override
          public void undo() {
            observableSet.add(oldValue);
          }

          @Override
          public void todo() {
            observableSet.remove(oldValue);
          }

        });
      }

      forward.clear();
      notifyWasModify();
    };
    unsubscribeActions.put(observableSet, () -> observableSet.removeListener(setChListener));
    observableSet.addListener(setChListener);
  }

  private void register(ChangeHistoryKeeping entity) {
    if (entity.getProperties() != null) {
      entity.getProperties().forEach(this::checkAndRegister);
    }
  }

  private void checkAndRegister(Object o) {
    if (o instanceof ChangeHistoryKeeping) {
      register((ChangeHistoryKeeping) o);
    }
    if (unsubscribeActions.containsKey(o)) {
      return;
    }
    if (o instanceof Property<?>) {
      register((Property<?>) o);
    } else if (o instanceof ObservableList<?>) {
      register((ObservableList<?>) o);
    } else if (o instanceof ObservableMap<?, ?>) {
      register((ObservableMap<?, ?>) o);
    } else if (o instanceof ObservableSet<?>) {
      register((ObservableSet<?>) o);
    }
  }

  private void unsubscribeProperty(Object o) {
    if (unsubscribeActions.containsKey(o)) {
      unsubscribeActions.get(o).run();
      unsubscribeActions.remove(o);
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

    });
  }

  public void allBack() {
    final int backSize = getBackSize();
    for (int i = 0; i < backSize; i++) {
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
    final int forwardSize = getForwardSize();
    for (int i = 0; i < forwardSize; i++) {
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
