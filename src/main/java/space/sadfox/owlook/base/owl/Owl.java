package space.sadfox.owlook.base.owl;

import static space.sadfox.owlook.base.owl.DirectoryStructure.*;

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
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.xml.bind.JAXBException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import space.sadfox.owlook.base.Extensions;
import space.sadfox.owlook.base.jaxb.JAXBHelper2;

public class Owl<T extends OwlEntity> implements AutoCloseable {

	private final FileSystem owlFileSystem;
	private final BooleanProperty openedProperty = new SimpleBooleanProperty(true);

	private final Path location;
	private final OwlInfo info;
	private final OwlHead<T> head;

	private Class<T> target;
	private T entity;

	private final Path root;
	private final Path resources;
	private final Path entityPath;
	private final Path headPath;

	private SaveExceptionHandler saveExceptionHandler;

	public Owl(Path owlFile, Class<T> target) throws IOException, JAXBException, OwlEntityInitializeException {
		this(owlFile);
		this.target = target;

		try (InputStream in = Files.newInputStream(entityPath)) {
			entity = JAXBHelper2.unmarshalInstance(in, target);
		}
		entity.initialize();
		entity.setOwl(this);
		entity.getChangeHistory().addListener(change -> {
			if (isEnableAutoSave()) {
				try {
					save();
				} catch (Exception e) {
					saveExceptionHandler.handle(e);
				}
			}
		});
	}

	private Owl(Path owlFile) throws IOException, JAXBException {
		if (Files.notExists(owlFile)) {
			throw new FileNotFoundException(owlFile.toString());
		}

		location = owlFile.toAbsolutePath();

		URI uri = URI.create("jar:file:" + location);
		owlFileSystem = FileSystems.newFileSystem(uri, new HashMap<>());

		root = owlFileSystem.getPath("/");
		resources = root.resolve(RESOURCES_DIR.get());
		entityPath = root.resolve(ENTITY_FILE.get());
		headPath = root.resolve(HEAD_FILE.get());

		Path infoFile = root.resolve(INFO_FILE.get());
		try (InputStream in = Files.newInputStream(infoFile)) {
			info = JAXBHelper2.unmarshalInstance(in, OwlInfo.class);
		}

		try (InputStream in = Files.newInputStream(headPath)) {
			head = JAXBHelper2.unmarshalInstance(in, OwlHead.class);
		}
		head.setOwl(this);
		head.getChangeHistory().addListener(change -> {
			if (isEnableAutoSave()) {
				try {
					save();
				} catch (Exception e) {
					saveExceptionHandler.handle(e);
				}
			}
		});

	}

	public T entity() {
		return entity;
	}

	public Class<T> entityClass() {
		return target;
	}

	public OwlInfo info() {
		return info;
	}

	public OwlHead<T> head() {
		return head;
	}

	public boolean isOpened() {
		return openedProperty.get();
	}
	
	public ReadOnlyBooleanProperty openedProperty() {
		return openedProperty;
	}

	public Path location() {
		return location;
	}

	public String fileName() {
		String fileName = location.getFileName().toString();
		int ind = fileName.indexOf(Extensions.OWL.get());
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
		JAXBHelper2.marshalInstance(entityPath, target, entity);
		JAXBHelper2.marshalInstance(headPath, OwlHead.class, head);
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
		openedProperty.set(false);
	}

	/**
	 * 
	 * @param <T>
	 * @param directory
	 * @param name
	 * @param target
	 * @return
	 * @throws FileAlreadyExistsException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws ReflectiveOperationException
	 */
	public static <T extends OwlEntity> Owl<T> create(Path directory, String name, Class<T> target) throws Exception {
		if (!Files.isDirectory(directory)) {
			throw new IOException("Is not directory: " + directory);
		}

		Path newOwlFile = directory.resolve(name + Extensions.OWL.get());

		if (Files.exists(newOwlFile)) {
			throw new FileAlreadyExistsException(name + Extensions.OWL.get());
		}

		try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(newOwlFile))) {
			zipOut.putNextEntry(new ZipEntry(INFO_DIR.get()));
			zipOut.putNextEntry(new ZipEntry(INFO_FILE.get()));
			OwlInfo info = new OwlInfo();
			info.createdModule = target.getModule().getName();
			info.createdTime = System.currentTimeMillis();
			info.targetClass = target.getName();
			JAXBHelper2.marshalInstance(zipOut, OwlInfo.class, info);

			zipOut.putNextEntry(new ZipEntry(RESOURCES_DIR.get()));

			zipOut.putNextEntry(new ZipEntry(HEAD_FILE.get()));
			OwlHead<T> head = new OwlHead<>();
			JAXBHelper2.marshalInstance(zipOut, OwlHead.class, head);

			zipOut.putNextEntry(new ZipEntry(ENTITY_FILE.get()));
			T entity = target.getConstructor().newInstance();
			JAXBHelper2.marshalInstance(zipOut, target, entity);

		} catch (Exception e) {
			Files.deleteIfExists(newOwlFile);
			throw e;
		}

		return new Owl<>(newOwlFile, target);
	}

	public static OwlInfo extractInfo(Path owlFile) throws IOException, JAXBException {
		try (Owl<OwlEntity> owl = new Owl<>(owlFile)) {
			return owl.info();
		}
	}

	@FunctionalInterface
	public interface SaveExceptionHandler {
		void handle(Exception exception);
	}
}
