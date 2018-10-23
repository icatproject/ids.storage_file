package org.icatproject.ids.storage;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Properties;

import org.icatproject.ids.plugin.DfInfo;
import org.icatproject.ids.plugin.DsInfo;
import org.junit.BeforeClass;
import org.junit.Test;

public class MainFileStorageTest {

	/*
	 * Note: this code assumes that the size of a directory in the
	 * file system is 4096 bytes or less.  We actually need:
	 *   file_size > 6*directory_size
	 *   high0 > 4*file_size + 6*directory_size
	 *   high1 < 4*file_size
	 *   low1 < high1
	 *   low1 > 3*file_size + 6*directory_size
	 *   low2 < 3*file_size
	 *   low2 > 2*file_size + 6*directory_size
	 */
	private static final int file_size = 32768;
	private static final int high0 = 163840;
	private static final int high1 = 129024;
	private static final int low1 = 124928;
	private static final int low2 = 94208;

	public class DeleteVisitor extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
			if (e == null) {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			} else {
				// directory iteration failed
				throw e;
			}
		}

	}

	private static DeleteVisitor treeDeleteVisitor = new MainFileStorageTest().new DeleteVisitor();

	private static MainFileStorage mainFileStorage;

	@BeforeClass
	public static void beforeClass() throws IOException, ClassNotFoundException, InterruptedException {

		Path dir = Paths.get(System.getProperty("testHome"), "storage_file");
		Files.createDirectories(dir);
		Properties props = new Properties();
		props.setProperty("plugin.main.dir", dir.toString());
		mainFileStorage = new MainFileStorage(props);

		if (dir != null) {
			if (Files.exists(dir)) {
				Files.walkFileTree(dir, treeDeleteVisitor);
			}
			Files.createDirectories(dir);
		}

		Files.createDirectories(dir.resolve("1/1"));

		Files.createDirectories(dir.resolve("1/2"));

		Files.createDirectories(dir.resolve("2/3"));

		byte[] buffer = new byte[file_size];
		Files.copy(new ByteArrayInputStream(buffer), dir.resolve("1/1/f1"));
		Thread.sleep(1000);
		Files.copy(new ByteArrayInputStream(buffer), dir.resolve("1/2/f2"));
		Thread.sleep(1000);
		Files.copy(new ByteArrayInputStream(buffer), dir.resolve("1/2/f3"));
		Thread.sleep(1000);
		Files.copy(new ByteArrayInputStream(buffer), dir.resolve("2/3/f4"));

	}

	@Test
	public void testGetDatafilesToArchive() throws Exception {
		List<DfInfo> dfInfos = mainFileStorage.getDatafilesToArchive(low1, high0);
		assertEquals(0, dfInfos.size());

		dfInfos = mainFileStorage.getDatafilesToArchive(low1, high1);
		assertEquals(1, dfInfos.size());
		assertEquals("1/1/f1", dfInfos.get(0).getDfLocation());

		dfInfos = mainFileStorage.getDatafilesToArchive(low2, high1);
		assertEquals(2, dfInfos.size());
		assertEquals("1/1/f1", dfInfos.get(0).getDfLocation());
		assertEquals("1/2/f2", dfInfos.get(1).getDfLocation());

		dfInfos = mainFileStorage.getDatafilesToArchive(0, high1);
		assertEquals(4, dfInfos.size());
	}

	@Test
	public void testGetDatasetsToArchive() throws Exception {
		List<DsInfo> dsInfos = mainFileStorage.getDatasetsToArchive(low1, high0);
		assertEquals(0, dsInfos.size());

		dsInfos = mainFileStorage.getDatasetsToArchive(low1, high1);
		assertEquals(1, dsInfos.size());
		assertEquals("1/1", dsInfos.get(0).toString());

		dsInfos = mainFileStorage.getDatasetsToArchive(low2, high1);
		assertEquals(2, dsInfos.size());
		assertEquals("1/1", dsInfos.get(0).toString());
		assertEquals("1/2", dsInfos.get(1).toString());

		dsInfos = mainFileStorage.getDatasetsToArchive(0, high1);
		assertEquals(3, dsInfos.size());
		assertEquals("1/1", dsInfos.get(0).toString());
		assertEquals("1/2", dsInfos.get(1).toString());
		assertEquals("2/3", dsInfos.get(2).toString());
	}

}
