package space.sadfox.owlook.base.owl;

import static space.sadfox.owlook.base.owl.DirectoryStructure.ENTITY_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.HEAD_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.INFO_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.RESOURCES_DIR;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

class OwlFileSystem implements AutoCloseable {
  private final FileSystem fileSystem;

  public final Path location;
  public final Path root;
  public final Path resources;
  public final Path entityPath;
  public final Path headPath;
  public final Path infoPath;

  private final IntegerProperty openedCount = new SimpleIntegerProperty(1);
  private boolean closeRequest = false;

  private static final Logger log = LoggerFactory.getLogger(OwlFileSystem.class);

  OwlFileSystem(final Path owlFile) throws FileNotFoundException, IOException {
    log.debug("Open OwlFileSystem for: {}", owlFile);
    if (Files.notExists(owlFile)) {
      throw new FileNotFoundException(owlFile.toString());
    }
    location = owlFile.toAbsolutePath();

    Map<String, String> env = new HashMap<>();
    env.put("create", "true");
    URI uri = URI.create("jar:" + location.toUri());
    log.debug("FileSystem URI: {}", uri);
    fileSystem = FileSystems.newFileSystem(uri, env);

    root = fileSystem.getPath("/");
    resources = root.resolve(RESOURCES_DIR.get());
    entityPath = root.resolve(ENTITY_FILE.get());
    headPath = root.resolve(HEAD_FILE.get());
    infoPath = root.resolve(INFO_FILE.get());

    openedCount.addListener((property, oldValue, newValue) -> {
      if (newValue.intValue() <= 0) {
        try {
          fileSystem.close();
          log.debug("Closing OwlFileSystem for: {}", owlFile);
        } catch (IOException e) {
          log.error("Error closing OwlFileSystem for: {}", owlFile, e);
        }
      } else {
      }
    });
  }

  public boolean isOpened() {
    return fileSystem.isOpen();
  }

  public OwlResource openResource() {
    OwlResource res = new OwlResource(this);
    incrementOpened();
    log.debug("Open OwlResource for: {}, total resources opened: {}", location, openedCount.get());
    res.openProperty().addListener((property, oldValue, newValue) -> {
      if (newValue) {
        incrementOpened();
      } else {
        decrementOpened();
      }
    });
    return res;
  }

  private void incrementOpened() {
    openedCount.set(openedCount.get() + 1);
  }

  private void decrementOpened() {
    openedCount.set(openedCount.get() - 1);
  }

  @Override
  public void close() {
    if (!closeRequest) {
      decrementOpened();
      log.debug("OwlFileSystem close request for: {}, total resources opened: {}", location, openedCount.get());
      closeRequest = true;
    }
  }

}
