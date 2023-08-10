package space.sadfox.owlook.base.moduleapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;



public class OwlookModulePacks {
	public static List<Path> findModuleFiles(Path parent, int deep) throws IOException {
		return Files.find(parent, deep, (p, attr) -> p.getFileName().toString().endsWith(".owlm"))
				.map(Path::toAbsolutePath).collect(Collectors.toList());
	}
	public static List<OwlookModulePack> getModulePacks(Path... moduleFiles) {
		return getModulePacks(Arrays.asList(moduleFiles));
	}

	public static List<OwlookModulePack> getModulePacks(Collection<Path> moduleFiles) {
		List<OwlookModulePack> modulePacks = new ArrayList<>();
		for (Path moduleFile : moduleFiles) {
			try {
				modulePacks.add(new OwlookModulePack(moduleFile));
			} catch (IOException e) {
				
			}
		}
		return modulePacks;
	}
}
