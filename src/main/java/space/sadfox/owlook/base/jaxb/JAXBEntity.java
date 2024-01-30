package space.sadfox.owlook.base.jaxb;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.xml.bind.JAXBException;

public abstract class JAXBEntity {
	
	private Path location;
	
	public void save(OutputStream outputStream) throws JAXBException {
		JAXBHelper.marshalInstance(outputStream, this.getClass(), this);
	}
	
	public void save() throws JAXBException, UnlocatedEntityException, IOException {
		try (OutputStream out = Files.newOutputStream(getLocation())) {
			JAXBHelper.marshalInstance(out, this.getClass(), this);
		}
	}
	
	public Path getLocation() throws UnlocatedEntityException {
		if (location == null) {
			throw new UnlocatedEntityException();
		}
		return location;
	}
	
	void setLocation(Path location) {
		this.location = location;
	}
	
	protected abstract void initialization();
}
