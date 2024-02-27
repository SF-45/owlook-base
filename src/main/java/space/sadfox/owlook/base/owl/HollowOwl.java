package space.sadfox.owlook.base.owl;

import java.nio.file.Path;

public interface HollowOwl {
  OwlInfo info();

  OwlHead head();

  Path location();
}
