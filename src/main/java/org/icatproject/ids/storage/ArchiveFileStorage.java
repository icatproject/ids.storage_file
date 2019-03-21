package org.icatproject.ids.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.icatproject.ids.plugin.AbstractArchiveStorage;
import org.icatproject.ids.plugin.DfInfo;
import org.icatproject.ids.plugin.DsInfo;
import org.icatproject.ids.plugin.MainStorageInterface;

public class ArchiveFileStorage extends AbstractArchiveStorage {

	Path baseDir;

	public ArchiveFileStorage(Properties props) throws IOException {
		String fname = Utils.resolveEnvs(props.getProperty("plugin.archive.dir"));
		if (fname == null) {
			throw new IOException("\"plugin.archive.dir\" is not defined");
		}
		baseDir = new File(fname).toPath();
		Utils.checkDir(baseDir);
	}

	@Override
	public void delete(DsInfo dsInfo) throws IOException {
		String location = dsInfo.getInvId() + "/" + dsInfo.getDsId();
		Path path = baseDir.resolve(location);
		try {
			Files.delete(path);
			Files.delete(path.getParent());
		} catch (DirectoryNotEmptyException | NoSuchFileException e) {
		}
	}

	@Override
	public void put(DsInfo dsInfo, InputStream inputStream) throws IOException {
		String location = dsInfo.getInvId() + "/" + dsInfo.getDsId();
		Path path = baseDir.resolve(location);
		Files.createDirectories(path.getParent());
		Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public void get(DsInfo dsInfo, Path path) throws IOException {
		String location = dsInfo.getInvId() + "/" + dsInfo.getDsId();
		Files.copy(baseDir.resolve(location), path, StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public void delete(String location) throws IOException {
		Path path = baseDir.resolve(location);
		try {
			Files.delete(path);
			/* Try deleting empty directories */
			path = path.getParent();
			while (!path.equals(baseDir)) {
				Files.delete(path);
				path = path.getParent();
			}
		} catch (DirectoryNotEmptyException | NoSuchFileException e) {
		}
	}

	@Override
	public void put(InputStream is, String location) throws IOException {
		Path path = baseDir.resolve(location);
		Files.createDirectories(path.getParent());
		Files.copy(new BufferedInputStream(is), path);
	}

	@Override
	public Set<DfInfo> restore(MainStorageInterface mainStorageInterface, List<DfInfo> dfInfos) {
		Set<DfInfo> failures = new HashSet<>();
		for (DfInfo dfInfo : dfInfos) {
			String location = dfInfo.getDfLocation();
			try (InputStream is = Files.newInputStream(baseDir.resolve(dfInfo.getDfLocation()))) {
				mainStorageInterface.put(is, location);
			} catch (IOException e) {
				failures.add(dfInfo);
			}
		}
		return failures;
	}

}
