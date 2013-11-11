package org.icatproject.ids.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.icatproject.ids.plugin.DsInfo;
import org.icatproject.ids.plugin.MainStorageInterface;
import org.icatproject.utils.CheckedProperties;
import org.icatproject.utils.CheckedProperties.CheckedPropertyException;

public class MainFileStorage implements MainStorageInterface {

	Path baseDir;

	public MainFileStorage(File properties) throws IOException {
		try {
			CheckedProperties props = new CheckedProperties();
			props.loadFromFile(properties.getPath());

			baseDir = props.getFile("dir").toPath();
			checkDir(baseDir, properties);

		} catch (CheckedPropertyException e) {
			throw new IOException("CheckedPropertException " + e.getMessage());
		}
	}

	private void checkDir(Path dir, File props) throws IOException {
		if (!Files.isDirectory(dir)) {
			throw new IOException(dir + " as specified in " + props + " is not a directory");
		}
	}

	@Override
	public void delete(DsInfo dsInfo) throws IOException {
		String location = dsInfo.getFacilityName() + "/" + dsInfo.getInvName() + "/"
				+ dsInfo.getVisitId() + "/" + dsInfo.getDsName();
		Path path = baseDir.resolve(location);
		TreeDeleteVisitor treeDeleteVisitor = new TreeDeleteVisitor();
		if (Files.exists(path)) {
			Files.walkFileTree(path, treeDeleteVisitor);
		}
		/* Try deleting empty directories */
		path = path.getParent();
		try {
			while (!path.equals(baseDir)) {
				Files.delete(path);
				path = path.getParent();
			}
		} catch (IOException e) {
			// Directory probably not empty
		}

	}

	@Override
	public void delete(String location) throws IOException {
		Path path = baseDir.resolve(location);
		Files.delete(path);
		/* Try deleting empty directories */
		path = path.getParent();
		try {
			while (!path.equals(baseDir)) {
				Files.delete(path);
				path = path.getParent();
			}
		} catch (IOException e) {
			// Directory probably not empty
		}
	}

	@Override
	public boolean exists(DsInfo dsInfo) throws IOException {
		String location = dsInfo.getFacilityName() + "/" + dsInfo.getInvName() + "/"
				+ dsInfo.getVisitId() + "/" + dsInfo.getDsName();
		return Files.exists(baseDir.resolve(location));
	}

	@Override
	public List<String> getLocations(DsInfo dsInfo) throws IOException {
		String location = dsInfo.getFacilityName() + "/" + dsInfo.getInvName() + "/"
				+ dsInfo.getVisitId() + "/" + dsInfo.getDsName();
		Path path = baseDir.resolve(location);
		TreeAddToZipVisitor visitor = new TreeAddToZipVisitor(baseDir);
		if (Files.exists(path)) {
			Files.walkFileTree(path, visitor);
		}
		return visitor.getLocations();
	}

	@Override
	public InputStream get(String location) throws IOException {
		return Files.newInputStream(baseDir.resolve(location));
	}

	@Override
	public String put(DsInfo dsInfo, String name, InputStream is) throws IOException {
		String location = dsInfo.getFacilityName() + "/" + dsInfo.getInvName() + "/"
				+ dsInfo.getVisitId() + "/" + dsInfo.getDsName() + "/" + name;

		Path path = baseDir.resolve(location);
		Files.createDirectories(path.getParent());
		Files.copy(new BufferedInputStream(is), path);
		return location;
	}

}
