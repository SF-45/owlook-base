package space.sadfox.owlook.base.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class JAXBHelper {
	
	public static <T, E extends T> void marshalInstance(OutputStream outputStream, Class<E> target, T instance) throws JAXBException {
		Marshaller marshaller = JAXBContext.newInstance(target).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(instance, outputStream);
	}
	
	public static <T, E extends T> void marshalInstance(Path path, Class<E> target, T instance) throws JAXBException, IOException {
		try (OutputStream out = Files.newOutputStream(path)) {
			marshalInstance(out, target, instance);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T unmarshalInstance(InputStream inputStream, Class<T> target) throws JAXBException {
		Unmarshaller unmarshaller = JAXBContext.newInstance(target).createUnmarshaller();
		return (T) unmarshaller.unmarshal(inputStream);
		
	}
	
	public static <T> T unmarshalInstance(Path path, Class<T> target) throws JAXBException, IOException {
		try (InputStream in = Files.newInputStream(path)) {
			return unmarshalInstance(in, target);
		}
		
	}

}
