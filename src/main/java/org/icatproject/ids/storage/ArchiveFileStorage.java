package org.icatproject.ids.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.icatproject.ids.plugin.ArchiveStorageInterface;
import org.icatproject.ids.plugin.DsInfo;
import org.icatproject.utils.CheckedProperties;
import org.icatproject.utils.CheckedProperties.CheckedPropertyException;

public class ArchiveFileStorage implements ArchiveStorageInterface {

	Path baseDir;

	public ArchiveFileStorage(File properties) throws IOException {
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
		Files.delete(baseDir.resolve(location));
	}

	@Override
	public void put(DsInfo dsInfo, InputStream inputStream) throws IOException {
		String location = dsInfo.getFacilityName() + "/" + dsInfo.getInvName() + "/"
				+ dsInfo.getVisitId() + "/" + dsInfo.getDsName();
		Files.copy(inputStream, baseDir.resolve(location), StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public InputStream get(DsInfo dsInfo) throws IOException {
		String location = dsInfo.getFacilityName() + "/" + dsInfo.getInvName() + "/"
				+ dsInfo.getVisitId() + "/" + dsInfo.getDsName();
		return Files.newInputStream(baseDir.resolve(location));
	}

}
