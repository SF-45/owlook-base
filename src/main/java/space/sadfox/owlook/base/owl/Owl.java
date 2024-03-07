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
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.base.jaxb.JAXBHelper;

public final class Owl<T extends OwlEntity> implements HollowOwl {

  private class AutoSaveTimer implements Runnable {
    private int duration = 0;
    private int counter = 1;
    public final Thread thread;

    AutoSaveTimer(int durationSec) {
      duration = durationSec;
      thread = new Thread(this);
      thread.start();
    }

    @Override
    public void run() {
      while (counter < duration) {
        counter++;
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      try {
        save();
      } catch (JAXBException | IOException e) {
        saveExceptionHandler.handle(e);
      }
    }

    public void resetTimer() {
      counter = 1;
    }
  }

  public static final String EXTENSION = ".owl";

  private OwlFileSystem fileSystem;

  private final Path location;
  private final OwlInfo info;
  private final OwlHead head;

  private Class<T> target;
  private T entity;

  private SaveExceptionHandler saveExceptionHandler;
  private int autoSaveDelay = 0;
  private AutoSaveTimer autoSaveTimer;

  public Owl(Path owlFile, Class<T> target)
      throws IOException, JAXBException, OwlEntityInitializeException {
    try (OwlFileSystem fileSystem = new OwlFileSystem(owlFile)) {
      location = fileSystem.location;
      this.fileSystem = fileSystem;

      info = JAXBHelper.unmarshalInstance(fileSystem.infoPath, OwlInfo.class);
      head = JAXBHelper.unmarshalInstance(fileSystem.headPath, OwlHead.class);
      head.setHollowOwl(this);
      head.getChangeHistory().addListener(change -> autoSaveAction());

      if (target != null) {
        this.target = target;
        entity = JAXBHelper.unmarshalInstance(fileSystem.entityPath, target);
        entity.initialize();
        entity.setOwl(this);
        entity.getChangeHistory().addListener(change -> autoSaveAction());
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
      System.out.println(new Date(System.currentTimeMillis()));
      JAXBHelper.marshalInstance(fs.entityPath, target, entity);
      JAXBHelper.marshalInstance(fs.headPath, OwlHead.class, head);
    }
  }

  private void autoSaveAction() {
    if (isEnableAutoSave()) {
      if (autoSaveTimer == null || !autoSaveTimer.thread.isAlive()) {
        autoSaveTimer = new AutoSaveTimer(autoSaveDelay);
      } else {
        autoSaveTimer.resetTimer();
      }
    }
  }

  public void setAutoSaveDelay(int sec) {
    autoSaveDelay = sec;
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

  public static <T extends OwlEntity> Owl<T> create(Path directory, String name, Class<T> target)
      throws IOException, JAXBException, ReflectiveOperationException,
      OwlEntityInitializeException {
    if (!Files.isDirectory(directory)) {
      throw new IOException("Is not directory: " + directory);
    }
    OwlInfo info = new OwlInfo();
    T entity = target.getConstructor().newInstance();
    info.createdModule = target.getModule().getName();
    info.createdTime = System.currentTimeMillis();
    info.targetClass = target.getName();
    info.owlName = entity.getEntityName();
    if (name == null || name == "") {
      name = info.id().toString();
    }

    Path newOwlFile = directory.resolve(name + EXTENSION);

    if (Files.exists(newOwlFile)) {
      throw new FileAlreadyExistsException(name + EXTENSION);
    }

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

    return new Owl<>(newOwlFile, target);
  }

  public static <T extends OwlEntity> Owl<T> create(Path directory, Class<T> target)
      throws IOException, JAXBException, ReflectiveOperationException,
      OwlEntityInitializeException {
    return create(directory, null, target);
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
    if (fileSystem.isOpened()) {
      return fileSystem;
    } else {
      return new OwlFileSystem(location);
    }
  }

  @FunctionalInterface
  public interface SaveExceptionHandler {
    void handle(Exception exception);
  }


}
