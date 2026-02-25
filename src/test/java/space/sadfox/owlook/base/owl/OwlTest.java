package space.sadfox.owlook.base.owl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class OwlTest {

  @TempDir
  Path tmpDir;

  @Test
  void testCreate() {
    try {
      var testOwl = Owl.create(tmpDir, LazyOwlEntity.class);

      final String testOwlName = testOwl.location().getFileName().toString();

      testOwl.entity().getLazyProperty("key1", "value1");
      testOwl.entity().getLazyProperty("key2", "value2");

      final String testRes = "test data";
      try (OwlResource res = testOwl.openResource()) {
        try (OutputStream out = res.resourceOutputStream("testRes.txt")) {
          out.write(testRes.getBytes());
        }
      }

      testOwl.save();

      var openTestOwl = new Owl<>(tmpDir.resolve(testOwlName), LazyOwlEntity.class);

      assertEquals(openTestOwl.entity().getLazyProperty("key1", "").get(), "value1");
      assertEquals(openTestOwl.entity().getLazyProperty("key2", "").get(), "value2");

      String readTestRes;
      try (OwlResource res = openTestOwl.openResource()) {
        try (InputStream in = res.resourceInputStream("testRes.txt")) {
          readTestRes = new String(in.readAllBytes());
        }
      }
      assertEquals(readTestRes, testRes);

      assertThrows(FileAlreadyExistsException.class, () -> {
        Owl.create(tmpDir.resolve(testOwlName), LazyOwlEntity.class);
      });

    } catch (Exception e) {
      fail(e);
    }
  }
}
