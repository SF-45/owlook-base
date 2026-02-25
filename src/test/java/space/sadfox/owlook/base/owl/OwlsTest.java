package space.sadfox.owlook.base.owl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class OwlsTest {

  @TempDir
  Path tmpDir;

  @Test
  void testFindOwlFiles() throws IOException {
    Files.createFile(tmpDir.resolve("owl1" + Owl.EXTENSION));
    Files.createFile(tmpDir.resolve("owl2" + Owl.EXTENSION));
    Files.createFile(tmpDir.resolve("owl3" + Owl.EXTENSION));

    var owls = Owls.findOwlFiles(tmpDir, 1);

    assertEquals(owls.size(), 3);
  }
}
