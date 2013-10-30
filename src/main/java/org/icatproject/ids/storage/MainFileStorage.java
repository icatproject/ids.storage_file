package org.icatproject.ids.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

import org.icatproject.Datafile;
import org.icatproject.ids.plugin.DsInfo;
import org.icatproject.ids.plugin.MainStorageInterface;
import org.icatproject.utils.CheckedProperties;
import org.icatproject.utils.CheckedProperties.CheckedPropertyException;

public class MainFileStorage implements MainStorageInterface {

	private final int BUFSIZ = 2048;

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
	public DfInfo put(DsInfo dsInfo, String name, InputStream is) throws IOException {
		String location = dsInfo.getFacilityName() + "/" + dsInfo.getInvName() + "/"
				+ dsInfo.getVisitId() + "/" + dsInfo.getDsName() + "/" + name;

		Path path = baseDir.resolve(location);
		Files.createDirectories(path.getParent());

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		CRC32 crc = new CRC32();
		long len = 0;
		try {
			bos = new BufferedOutputStream(Files.newOutputStream(path));
			int bytesRead = 0;
			byte[] buffer = new byte[BUFSIZ];
			bis = new BufferedInputStream(is);

			while ((bytesRead = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, bytesRead);
				crc.update(buffer, 0, bytesRead);
				len += bytesRead;
			}
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}

		return new DfInfo(location, len, crc.getValue());
	}

	@Override
	public InputStream get(DsInfo dsInfo) throws IOException {

		return null;
	}

	@Override
	public boolean exists(DsInfo dsInfo) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	// public InputStream getDataset(String location) throws IOException {
	// File zippedDs = new File(new File(STORAGE_ZIP_DIR, location), "files.zip");
	// if (!zippedDs.exists()) {
	// throw new FileNotFoundException(zippedDs.getAbsolutePath());
	// }
	// return new BufferedInputStream(new FileInputStream(zippedDs));
	// }

	// public void putDataset(String location, InputStream is) throws IOException {
	// File zippedDs = new File(new File(STORAGE_ZIP_DIR, location), "files.zip");
	// File zippedDsDir = zippedDs.getParentFile();
	// zippedDsDir.mkdirs();
	// zippedDs.createNewFile();
	// writeInputStreamToFile(zippedDs, is);
	// }

	// public void deleteDataset(String location) throws IOException {
	// File zippedDs = new File(new File(STORAGE_ZIP_DIR, location), "files.zip");
	// zippedDs.delete();
	// }

	// public boolean datasetExists(String location) throws IOException {
	// File zippedDs = new File(new File(STORAGE_ZIP_DIR, location), "files.zip");
	// return zippedDs.exists();
	// }

	// public InputStream getDatafile(String location) throws FileNotFoundException {
	// if (STORAGE_DIR == null) {
	// throw new UnsupportedOperationException(String.format(
	// "Storage %s doesn't support single Datafiles", storageType));
	// }
	// File file = new File(STORAGE_DIR, location);
	// if (!file.exists()) {
	// throw new FileNotFoundException(file.getAbsolutePath());
	// }
	// return new BufferedInputStream(new FileInputStream(file));
	// }

	// public long putDatafile(String location, InputStream is) throws IOException {
	// if (STORAGE_DIR == null) {
	// throw new UnsupportedOperationException(String.format(
	// "Storage %s doesn't support single Datafiles", storageType));
	// }
	// File file = new File(STORAGE_DIR, location);
	// writeInputStreamToFile(file, is);
	// return file.length();
	// }

	// public void deleteDatafile(String location) throws IOException {
	// if (STORAGE_DIR == null) {
	// throw new UnsupportedOperationException(String.format(
	// "Storage %s doesn't support single Datafiles", storageType));
	// }
	// File file = new File(STORAGE_DIR, location);
	// file.delete();
	// }

	// public boolean datafileExists(String location) throws IOException {
	// if (STORAGE_DIR == null) {
	// throw new UnsupportedOperationException(String.format(
	// "Storage %s doesn't support single Datafiles", storageType));
	// }
	// File file = new File(STORAGE_DIR, location);
	// return file.exists();
	// }

	// public InputStream getPreparedZip(String zipName) throws IOException {
	// if (STORAGE_PREPARED_DIR == null) {
	// throw new UnsupportedOperationException(String.format(
	// "Storage %s doesn't support preparation of zip files for users", storageType));
	// }
	// File preparedZip = new File(STORAGE_PREPARED_DIR, zipName);
	// if (!preparedZip.exists()) {
	// throw new FileNotFoundException(preparedZip.getAbsolutePath());
	// }
	// return new BufferedInputStream(new FileInputStream(preparedZip));
	// }

	// public void putPreparedZip(String zipName, InputStream is) throws IOException {
	// if (STORAGE_PREPARED_DIR == null) {
	// throw new UnsupportedOperationException(String.format(
	// "Storage %s doesn't support preparation of zip files for users", storageType));
	// }
	// File file = new File(STORAGE_PREPARED_DIR, zipName);
	// writeInputStreamToFile(file, is);
	// }

	private void writeInputStreamToFile(File file, InputStream is) throws IOException {
		File fileDir = file.getParentFile();
		fileDir.mkdirs();

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			int bytesRead = 0;
			byte[] buffer = new byte[BUFSIZ];
			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(new FileOutputStream(file));

			// write bytes to output stream
			while ((bytesRead = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, bytesRead);
			}
		} finally {
			if (bos != null) {
				bos.close();
			}
		}
	}

	public boolean exists(Datafile df) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean exists(String location) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public void delete(String location) throws IOException {
		// TODO Auto-generated method stub

	}

	public InputStream get(String location) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
