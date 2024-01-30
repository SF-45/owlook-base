package space.sadfox.owlook.base.owl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Owls {
	public static List<Path> findOwlFiles(Path parent, int deep) throws IOException {
		return Files.find(parent, deep, (p, attr) -> p.getFileName().toString().endsWith(Owl.EXTENSION))
				.map(Path::toAbsolutePath).collect(Collectors.toList());
	}
	public static <T extends OwlEntity>  List<Owl<T>> getOwls(Class<T> target, Path... owlFiles) {
		return getOwls(target, Arrays.asList(owlFiles));
	}

	public static <T extends OwlEntity>  List<Owl<T>> getOwls(Class<T> target, Collection<Path> owlFiles) {
		List<Owl<T>> owls = new ArrayList<>();
		for (Path owlFile : owlFiles) {
			try {
				owls.add(new Owl<>(owlFile, target));
			} catch (Exception e) {
				
			}
		}
		return owls;
	}
}
