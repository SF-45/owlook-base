package space.sadfox.owlook.base.owl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Owls {
  public static List<Path> findOwlFiles(Path parent, int deep) throws IOException {
    return Files.find(parent, deep, (p, attr) -> p.getFileName().toString().endsWith(Owl.EXTENSION))
        .map(Path::toAbsolutePath).collect(Collectors.toList());
  }

}
