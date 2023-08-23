package space.sadfox.owlook.base.moduleapi;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.module.ModuleDescriptor.Provides;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import space.sadfox.owlook.base.Extensions;

public class OwlookModulePacker {

	public final OwlookModuleInfo MODILE_INFO;
	private final Path owlModulePath;
	public final List<Path> INCLUDE_LIBRARIES = new ArrayList<>();

	public OwlookModulePacker(OwlookModuleInfo moduleInfo, Path owlModulePath) throws PackageException {
		MODILE_INFO = moduleInfo;
		Set<ModuleReference> modSet = ModuleFinder.of(owlModulePath).findAll();
		if (modSet.size() != 1) {
			StringBuilder errStr = new StringBuilder(
					"Path " + owlModulePath + " found " + modSet.size() + " modules\n");
			for (ModuleReference ref : modSet) {
				errStr.append(ref.descriptor().name() + "\n");
			}
			throw new PackageException(errStr.toString());
		}
		
		ModuleReference ref = modSet.iterator().next();

		Provides owlookModuleProvider = null;
		
		for (Provides provider : ref.descriptor().provides()) {
			if (provider.service().equals(OwlookModule.class.getName())) {
				owlookModuleProvider = provider;
				break;
			}
		}
		
		if (owlookModuleProvider == null) {
			throw new PackageException("This module not provide OwlookModule");
		}else if (owlookModuleProvider.providers().size() > 1) {
			throw new PackageException("This module provide more than one (" + owlookModuleProvider.providers().size() + ") OwlookModule\n" + String.join("\n", owlookModuleProvider.providers()));
		} 

		this.owlModulePath = Path.of(ref.location().get());
		MODILE_INFO.moduleName = ref.descriptor().name();

	}

	public final void pack(Path outDir) throws IOException {
		if (!Files.isDirectory(outDir)) {
			throw new NotDirectoryException(outDir.toString());
		}

		Path out = outDir.resolve(MODILE_INFO.moduleName() + "-" + MODILE_INFO.version() + Extensions.OWLOOK_MODULE_PACK);
		try (OutputStream outZip = new BufferedOutputStream(Files.newOutputStream(out))) {
			pack(outZip);
		}

	}

	public final void pack(OutputStream out) throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(out);
		zipOut.putNextEntry(new ZipEntry("META-INF/"));
		zipOut.putNextEntry(new ZipEntry("META-INF/MODULE-INF"));

		try {
			JAXBContext context = JAXBContext.newInstance(OwlookModuleInfo.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(MODILE_INFO, zipOut);
		} catch (JAXBException e) {
			throw new IOException(e);
		}

		zipOut.putNextEntry(new ZipEntry("main.jar"));
		try (InputStream in = Files.newInputStream(owlModulePath)) {
			in.transferTo(zipOut);
		}
		zipOut.putNextEntry(new ZipEntry("lib/"));

		for (Path libPath : INCLUDE_LIBRARIES) {
			zipOut.putNextEntry(new ZipEntry("lib/" + libPath.getFileName()));
			try (InputStream in = Files.newInputStream(libPath)) {
				in.transferTo(zipOut);
			}
		}

		zipOut.finish();
	}

}
