package org.icatproject.ids.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.icatproject.ids.plugin.StorageInterface;
import org.icatproject.ids.plugin.StorageType;
import org.icatproject.utils.CheckedProperties;
import org.icatproject.utils.CheckedProperties.CheckedPropertyException;

public class FileStorage implements StorageInterface {

	private final int BUFSIZ = 2048;

	StorageType storageType;
	File STORAGE_ZIP_DIR;
	File STORAGE_DIR;
	File STORAGE_PREPARED_DIR;

	public FileStorage(File properties, StorageType type) throws IOException {
		try {
			CheckedProperties props = new CheckedProperties();
			props.loadFromFile(properties.getPath());
			STORAGE_ZIP_DIR = props.getFile("zipDir");
			checkDir(STORAGE_ZIP_DIR, properties);
			storageType = type;
			if (type == StorageType.MAIN) {
				STORAGE_DIR = props.getFile("dir");
				checkDir(STORAGE_DIR, properties);
				STORAGE_PREPARED_DIR = props.getFile("preparedDir");
				checkDir(STORAGE_PREPARED_DIR, properties);
			}
		} catch (CheckedPropertyException e) {
			throw new IOException("CheckedPropertException " + e.getMessage());
		}
	}

	private void checkDir(File dir, File props) throws IOException {
		if (!dir.isDirectory()) {
			throw new IOException(dir + " as specified in " + props + " is not a directory");
		}
	}

	@Override
	public InputStream getDataset(String location) throws IOException {
		File zippedDs = new File(new File(STORAGE_ZIP_DIR, location), "files.zip");
		if (!zippedDs.exists()) {
			throw new FileNotFoundException(zippedDs.getAbsolutePath());
		}
		return new BufferedInputStream(new FileInputStream(zippedDs));
	}

	@Override
	public void putDataset(String location, InputStream is) throws IOException {
		File zippedDs = new File(new File(STORAGE_ZIP_DIR, location), "files.zip");
		File zippedDsDir = zippedDs.getParentFile();
		zippedDsDir.mkdirs();
		zippedDs.createNewFile();
		writeInputStreamToFile(zippedDs, is);
	}

	@Override
	public void deleteDataset(String location) throws IOException {
		File zippedDs = new File(new File(STORAGE_ZIP_DIR, location), "files.zip");
		zippedDs.delete();
	}

	@Override
	public boolean datasetExists(String location) throws IOException {
		File zippedDs = new File(new File(STORAGE_ZIP_DIR, location), "files.zip");
		return zippedDs.exists();
	}

	@Override
	public InputStream getDatafile(String location) throws FileNotFoundException {
		if (STORAGE_DIR == null) {
			throw new UnsupportedOperationException(String.format(
					"Storage %s doesn't support single Datafiles", storageType));
		}
		File file = new File(STORAGE_DIR, location);
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		return new BufferedInputStream(new FileInputStream(file));
	}

	@Override
	public long putDatafile(String location, InputStream is) throws IOException {
		if (STORAGE_DIR == null) {
			throw new UnsupportedOperationException(String.format(
					"Storage %s doesn't support single Datafiles", storageType));
		}
		File file = new File(STORAGE_DIR, location);
		writeInputStreamToFile(file, is);
		return file.length();
	}

	@Override
	public void deleteDatafile(String location) throws IOException {
		if (STORAGE_DIR == null) {
			throw new UnsupportedOperationException(String.format(
					"Storage %s doesn't support single Datafiles", storageType));
		}
		File file = new File(STORAGE_DIR, location);
		file.delete();
	}

	@Override
	public boolean datafileExists(String location) throws IOException {
		if (STORAGE_DIR == null) {
			throw new UnsupportedOperationException(String.format(
					"Storage %s doesn't support single Datafiles", storageType));
		}
		File file = new File(STORAGE_DIR, location);
		return file.exists();
	}

	@Override
	public InputStream getPreparedZip(String zipName, long offset) throws IOException {
		if (STORAGE_PREPARED_DIR == null) {
			throw new UnsupportedOperationException(String.format(
					"Storage %s doesn't support preparation of zip files for users", storageType));
		}
		File preparedZip = new File(STORAGE_PREPARED_DIR, zipName);
		if (!preparedZip.exists()) {
			throw new FileNotFoundException(preparedZip.getAbsolutePath());
		}
		if (offset >= preparedZip.length()) {
			throw new IllegalArgumentException("Offset (" + offset
					+ " bytes) is larger than file size (" + preparedZip.length() + " bytes)");
		}
		BufferedInputStream res = new BufferedInputStream(new FileInputStream(preparedZip));
		res.skip(offset);
		return res;
	}

	@Override
	public void putPreparedZip(String zipName, InputStream is) throws IOException {
		if (STORAGE_PREPARED_DIR == null) {
			throw new UnsupportedOperationException(String.format(
					"Storage %s doesn't support preparation of zip files for users", storageType));
		}
		File file = new File(STORAGE_PREPARED_DIR, zipName);
		writeInputStreamToFile(file, is);
	}

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

}
