package space.sadfox.owlook.base.owl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class OwlResource implements AutoCloseable {
  private final OwlFileSystem parentFileSystem;
  private final BooleanProperty open = new SimpleBooleanProperty(true);

  OwlResource(OwlFileSystem parentFileSystem) {
    this.parentFileSystem = parentFileSystem;
  }

  public Path resourcePath(String resourceName) {
    return parentFileSystem.resources.resolve(resourceName);
  }

  public InputStream resourceInputStream(String resourceName) throws IOException {
    return Files.newInputStream(parentFileSystem.resources.resolve(resourceName));
  }

  public BufferedInputStream resourceBufferedInputStream(String resourceName) throws IOException {
    return new BufferedInputStream(resourceInputStream(resourceName));
  }

  public OutputStream resourceOutputStream(String resourceName) throws IOException {
    return Files.newOutputStream(parentFileSystem.resources.resolve(resourceName));
  }

  public BufferedOutputStream resourceBufferedOutputStream(String resourceName) throws IOException {
    return new BufferedOutputStream(resourceOutputStream(resourceName));
  }

  public boolean isResourceExist(String resourceName) {
    Path res = parentFileSystem.resources.resolve(resourceName);
    return Files.exists(res);
  }

  @Override
  public void close() {
    if (open.get()) {
      open.set(false);
    }
  }

  public ReadOnlyBooleanProperty openProperty() {
    return open;
  }

  public Boolean isOpen() {
    return open.get();
  }
}
