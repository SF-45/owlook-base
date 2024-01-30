package space.sadfox.owlook.base.moduleapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public class OwlookModulePack implements AutoCloseable {
	public static final String EXTENSION = ".owlm";
	
	private final FileSystem packFileSystem;
	
	
	public final Path LOCATION;
	public final OwlookModuleInfo MODILE_INFO;
	public final Path ROOT;
	public final Path LIB;
	public final Path MODULE;
	private boolean opened = true;

	public OwlookModulePack(Path packPath) throws IOException {
		LOCATION = packPath.toAbsolutePath();

		URI uri = URI.create("jar:file:" + LOCATION);
		packFileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
		
		ROOT = packFileSystem.getPath("/");
		LIB = ROOT.resolve("lib/");
		MODULE = ROOT.resolve("main.jar");
		try {
			JAXBContext context = JAXBContext.newInstance(OwlookModuleInfo.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			Path moduleInfoPath = ROOT.resolve("/META-INF/MODULE-INF");
			try (InputStream in = Files.newInputStream(moduleInfoPath)) {
				MODILE_INFO = (OwlookModuleInfo) unmarshaller.unmarshal(in);
			}
		} catch (JAXBException e) {
			throw new IOException(e);
		}

	}
	
	public boolean isOpened() {
		return opened;
	}

	@Override
	public void close() throws IOException {
		packFileSystem.close();
		opened = false;

	}

}
