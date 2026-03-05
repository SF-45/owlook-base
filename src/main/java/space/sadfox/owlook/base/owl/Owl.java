package space.sadfox.owlook.base.owl;

import static space.sadfox.owlook.base.owl.DirectoryStructure.ENTITY_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.HEAD_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.INFO_DIR;
import static space.sadfox.owlook.base.owl.DirectoryStructure.INFO_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.RESOURCES_DIR;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.base.jaxb.ActionTimer;
import space.sadfox.owlook.base.jaxb.JAXBHelper;

public final class Owl<T extends OwlEntity> implements HollowOwl {
  private static final Logger log = LoggerFactory.getLogger(Owl.class);
  public static final String EXTENSION = ".owl";

  private OwlFileSystem fileSystem;

  private final Path location;
  private final OwlInfo info;
  private final OwlHead head;

  private Class<T> target;
  private T entity;

  private SaveExceptionHandler saveExceptionHandler;
  private final ActionTimer autoSaveTimer;

  public Owl(Path owlFile, Class<T> target)
      throws IOException, JAXBException, OwlEntityInitializeException {
    log.info("Open Owl {}", owlFile);
    autoSaveTimer = new ActionTimer(() -> {
      try {
        save();
      } catch (Exception e) {
        saveExceptionHandler.handle(e);
      }
    });
    try (OwlFileSystem fileSystem = new OwlFileSystem(owlFile)) {
      location = fileSystem.location;
      this.fileSystem = fileSystem;

      log.debug("Parse Owl.info");
      info = JAXBHelper.unmarshalInstance(fileSystem.infoPath, OwlInfo.class);
      log.debug("Parse Owl.head");
      head = JAXBHelper.unmarshalInstance(fileSystem.headPath, OwlHead.class);
      head.setHollowOwl(this);
      head.getChangeHistory().addListener(change -> autoSaveAction());

      if (target != null) {
        this.target = target;
        log.debug("Parse Owl.entity {}", target);
        entity = JAXBHelper.unmarshalInstance(fileSystem.entityPath, target);
        entity.setOwl(this);
        entity.initialize();
        entity.getChangeHistory().addListener(change -> autoSaveAction());
        log.debug("Complete opening Owl {}", owlFile);
      } else {
        log.debug("Complete opening HollowOwl {}", owlFile);
      }
    }
  }

  public T entity() {
    return entity;
  }

  public Class<T> entityClass() {
    return target;
  }

  @Override
  public OwlInfo info() {
    return info;
  }

  @Override
  public OwlHead head() {
    return head;
  }

  @Override
  public String toString() {
    return "Owl{info=" + info + "\nhead=" + head + "}";
  }

  @Override
  public Path location() {
    return location;
  }

  public String fileName() {
    String fileName = location.getFileName().toString();
    int ind = fileName.indexOf(EXTENSION);
    return fileName.substring(0, ind);
  }

  public OwlResource openResource() throws IOException {
    try (OwlFileSystem fs = getOwlFileSystem()) {
      return fs.openResource();
    }
  }

  public synchronized void save() throws JAXBException, IOException {
    try (OwlFileSystem fs = getOwlFileSystem()) {
      log.debug("Save owl: {}", this);
      JAXBHelper.marshalInstance(fs.entityPath, target, entity);
      JAXBHelper.marshalInstance(fs.headPath, OwlHead.class, head);
    }
  }

  private void autoSaveAction() {
    if (isEnableAutoSave()) {
      autoSaveTimer.runOrResetTimer();
    }
  }

  public void setAutoSaveDelay(int sec) {
    autoSaveTimer.setDuration(sec);
  }

  public void enableAutoSave(SaveExceptionHandler handler) {
    saveExceptionHandler = handler;
  }

  public void disableAutoSave() {
    saveExceptionHandler = null;
  }

  public boolean isEnableAutoSave() {
    return saveExceptionHandler != null;
  }

  public static <T extends OwlEntity> Owl<T> create(Path path, Class<T> target)
      throws FileAlreadyExistsException, ReflectiveOperationException, IOException, JAXBException,
      OwlEntityInitializeException {
    log.info("Create new Owl: {} Target: {}", path, target);
    OwlInfo info = new OwlInfo();
    String fileName = "";
    Path directory;

    if (Files.isDirectory(path)) {
      fileName = info.id() + EXTENSION;
      log.debug("File name not specified. Created with owl ID: ", fileName);
      directory = path;
    } else {
      fileName = path.getFileName().toString();
      fileName = fileName.endsWith(EXTENSION) ? fileName : fileName + EXTENSION;
      directory = path.getParent();
    }

    Path newOwlFile = directory.resolve(fileName);
    if (Files.exists(newOwlFile)) {
      log.warn("Attempting to create a Owl with a filename that already exists: {}", newOwlFile);
      throw new FileAlreadyExistsException(newOwlFile.toString());
    }

    log.debug("Create new OwlEntity from {}", target);
    T entity = target.getConstructor().newInstance();
    info.createdModule = target.getModule().getName();
    info.createdTime = System.currentTimeMillis();
    info.targetClass = target.getName();
    info.owlName = entity.getEntityName();

    log.debug("Open OutputStream from {}", newOwlFile);
    try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(newOwlFile))) {
      zipOut.putNextEntry(new ZipEntry(INFO_DIR.get()));

      zipOut.putNextEntry(new ZipEntry(INFO_FILE.get()));
      JAXBHelper.marshalInstance(zipOut, OwlInfo.class, info);

      zipOut.putNextEntry(new ZipEntry(RESOURCES_DIR.get()));

      zipOut.putNextEntry(new ZipEntry(HEAD_FILE.get()));
      JAXBHelper.marshalInstance(zipOut, OwlHead.class, new OwlHead());

      zipOut.putNextEntry(new ZipEntry(ENTITY_FILE.get()));
      JAXBHelper.marshalInstance(zipOut, target, entity);

    } catch (Exception e) {
      Files.deleteIfExists(newOwlFile);
      throw e;
    }
    log.debug("Complete create Owl: {} Target: {}", newOwlFile, target);

    return new Owl<>(newOwlFile, target);
  }

  public static HollowOwl getHollowOwl(Path owlFile) throws IOException, JAXBException {
    Owl<?> owl = null;
    try {
      owl = new Owl<>(owlFile, null);
    } catch (OwlEntityInitializeException e) {
      throw new IOException(e);
    }
    return owl;
  }

  public HollowOwl getHollowOwl() {
    return this;
  }

  private OwlFileSystem getOwlFileSystem() throws IOException {
    if (fileSystem == null || !fileSystem.isOpened()) {
      fileSystem = new OwlFileSystem(location);
    }
    return fileSystem;
  }

  public void syncWith(Owl<T> owl) {
    if (!owl.entityClass().equals(entityClass())) {
      log.warn("Attempt to synchronize incompatible Owls {} with {}", this.entityClass(), owl.entityClass());
      return;
    }
    entity().syncWith(owl.entity());
    head().syncWith(owl.head());
  }

  @FunctionalInterface
  public interface SaveExceptionHandler {
    void handle(Exception exception);
  }

}
