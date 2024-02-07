package space.sadfox.owlook.base.owl;

import java.nio.file.Path;

public interface HollowOwl extends AutoCloseable {
  OwlInfo info();

  OwlHead head();

  Path location();
}
