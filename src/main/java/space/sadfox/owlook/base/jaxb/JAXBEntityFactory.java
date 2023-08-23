package space.sadfox.owlook.base.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.xml.bind.JAXBException;

public class JAXBEntityFactory<T extends JAXBEntity2> {
	
	private final Class<T> target;
	
	public JAXBEntityFactory(Class<T> target) {
		this.target = target;
	}
	
	
	
	
	public T instanceOf(InputStream inputStream) throws JAXBException {
		return JAXBHelper2.unmarshalInstance(inputStream, target);
	}
	
	public T instanceOf(Path path) throws JAXBException, IOException, ReflectiveOperationException {
		T instance = null;
		if (Files.exists(path)) {
			try (InputStream in = Files.newInputStream(path)) {
				instance = instanceOf(in);
			}
		} else {
			instance = unlocatedInstance();
		}
		
		instance.setLocation(path);
		return instance;
	}
	
	/**
	 * 
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public T unlocatedInstance() throws ReflectiveOperationException {
		T newInstance = target.getConstructor().newInstance();
		return newInstance;
	}

}
