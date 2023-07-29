package space.sadfox.owlook.base.jaxb;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class JAXBHelper<T extends JAXBEntity> {

	@FunctionalInterface
	public interface SaveExceptionHandler {
		void handle(Exception exception);
	}

	private T instance;

	private Path path;

	private JAXBContext context;

	private final EntityChangeListener autosaveAction;

	private SaveExceptionHandler saveExceptionHandler;

	@SuppressWarnings("unchecked")
	public JAXBHelper(Path path, Class<T> target) throws JAXBException, IOException {
		this.path = path;
		context = JAXBContext.newInstance(target);

		autosaveAction = change -> {
			if (change.wasModify() && saveExceptionHandler != null) {
				try {
					change.getEntity().save();
				} catch (JAXBException | IOException e) {
					saveExceptionHandler.handle(e);
				}
			}
		};

		if (Files.exists(path)) {
			Unmarshaller unmarshaller = context.createUnmarshaller();
			instance = (T) unmarshaller.unmarshal(new FileInputStream(path.toFile()));
		} else {
			try {
				instance = target.getConstructor().newInstance();
				save();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new JAXBException(e);
			}
		}
		instance.setJaxbHelper(this);
		instance.getChangeHistory();
		instance.validate();
		instance.initialize();

	}

	public T getInstance() {
		return instance;
	}

	public void enableAutoSave(SaveExceptionHandler handler) {
		saveExceptionHandler = handler;
		getInstance().addEntityChangeListener(autosaveAction);
	}

	public void disableAutoSave() {
		saveExceptionHandler = null;
		getInstance().removeEntityChangeListener(autosaveAction);
	}

	public boolean isEnableAutoSave() {
		return saveExceptionHandler == null;
	}

	public void save() throws JAXBException, IOException {
		if (!Files.exists(getPath().getParent()))
			Files.createDirectory(getPath().getParent());
		marshalInstance(Files.newOutputStream(getPath()));
	}

	private void marshalInstance(OutputStream outputStream) throws JAXBException, IOException {
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(instance, outputStream);
	}

	public void renameEntity(String fileName) throws JAXBException, IOException {
		Files.deleteIfExists(getPath());
		setPath(getPath().getParent().resolve(fileName));
		save();
	}

	public Path getPath() {
		return path;
	}
	
	private void setPath(Path path) {
		this.path = path;
	}
}
