package space.sadfox.owlook.base.owl;

import static space.sadfox.owlook.base.owl.DirectoryStructure.ENTITY_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.HEAD_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.INFO_DIR;
import static space.sadfox.owlook.base.owl.DirectoryStructure.INFO_FILE;
import static space.sadfox.owlook.base.owl.DirectoryStructure.RESOURCES_DIR;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.base.jaxb.JAXBHelper;

public final class Owl<T extends OwlEntity> implements HollowOwl {

  public static final String EXTENSION = ".owl";

  private final FileSystem owlFileSystem;

  private final Path location;
  private final OwlInfo info;
  private final OwlHead head;

  private Class<T> target;
  private T entity;

  private final Path root;
  private final Path resources;
  private final Path entityPath;
  private final Path headPath;

  private SaveExceptionHandler saveExceptionHandler;

  public Owl(Path owlFile, Class<T> target)
      throws IOException, JAXBException, OwlEntityInitializeException {
    this(owlFile);
    this.target = target;

    entity = JAXBHelper.unmarshalInstance(entityPath, target);
    entity.initialize();
    entity.setOwl(this);
    entity.getChangeHistory().addListener(change -> autoSaveAction());
  }

  private Owl(Path owlFile) throws IOException, JAXBException {
    if (Files.notExists(owlFile)) {
      throw new FileNotFoundException(owlFile.toString());
    }

    location = owlFile.toAbsolutePath();

    Map<String, String> env = new HashMap<>();
    env.put("create", "true");
    URI uri = URI.create("jar:file:" + location);
    owlFileSystem = FileSystems.newFileSystem(uri, env);

    root = owlFileSystem.getPath("/");
    resources = root.resolve(RESOURCES_DIR.get());
    entityPath = root.resolve(ENTITY_FILE.get());
    headPath = root.resolve(HEAD_FILE.get());

    Path infoFile = root.resolve(INFO_FILE.get());
    info = JAXBHelper.unmarshalInstance(infoFile, OwlInfo.class);
    head = JAXBHelper.unmarshalInstance(headPath, OwlHead.class);
    head.setHollowOwl(this);
    head.getChangeHistory().addListener(change -> autoSaveAction());

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

  public boolean isOpened() {
    return owlFileSystem.isOpen();
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

  public Path resourcePath(String resourceName) {
    return resources.resolve(resourceName);
  }

  public InputStream resourceInputStream(String resourceName) throws IOException {
    return Files.newInputStream(resources.resolve(resourceName));
  }

  public BufferedInputStream resourceBufferedInputStream(String resourceName) throws IOException {
    return new BufferedInputStream(resourceInputStream(resourceName));
  }

  public OutputStream resourceOutputStream(String resourceName) throws IOException {
    return Files.newOutputStream(resources.resolve(resourceName));
  }

  public BufferedOutputStream resourceBufferedOutputStream(String resourceName) throws IOException {
    return new BufferedOutputStream(resourceOutputStream(resourceName));
  }

  public void save() throws JAXBException, IOException {
    if (!isOpened())
      return;
    System.out.println(new Date(System.currentTimeMillis()));
    JAXBHelper.marshalInstance(entityPath, target, entity);
    JAXBHelper.marshalInstance(headPath, OwlHead.class, head);
  }

  private void autoSaveAction() {
    if (isEnableAutoSave()) {
      try {
        save();
      } catch (Exception e) {
        saveExceptionHandler.handle(e);
      }
    }
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

  @Override
  public void close() throws IOException {
    if (owlFileSystem != null) {
      owlFileSystem.close();
    }
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
    Owl<OwlEntity> owl = null;
    try {
      owl = new Owl<>(owlFile);
      return owl;
    } finally {
      if (owl != null) {
        owl.close();
      }
    }
  }

  public HollowOwl getHollowOwl() {
    return this;
  }

  @FunctionalInterface
  public interface SaveExceptionHandler {
    void handle(Exception exception);
  }


}
