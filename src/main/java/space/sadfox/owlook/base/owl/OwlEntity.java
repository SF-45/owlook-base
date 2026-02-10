package space.sadfox.owlook.base.owl;

import java.util.List;
import space.sadfox.owlook.base.jaxb.ChangeHistory;
import space.sadfox.owlook.base.jaxb.ChangeHistoryKeeping;

public abstract class OwlEntity implements ChangeHistoryKeeping {
  private ChangeHistory<OwlEntity> changeHistory;
  private Owl<?> owl;

  public final ChangeHistory<OwlEntity> getChangeHistory() {
    if (changeHistory == null) {
      changeHistory = new ChangeHistory<>(this);
    }
    return changeHistory;
  }

  public final Owl<?> thisOwl() {
    return owl;
  }

  final void setOwl(Owl<?> owl) {
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
