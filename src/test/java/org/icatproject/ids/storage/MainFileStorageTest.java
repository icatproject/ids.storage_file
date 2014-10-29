package org.icatproject.ids.storage;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Properties;

import org.icatproject.ids.plugin.DfInfo;
import org.icatproject.ids.plugin.DsInfo;
import org.junit.BeforeClass;
import org.junit.Test;

public class MainFileStorageTest {

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
	public static void beforeClass() throws IOException, ClassNotFoundException,
			InterruptedException {
		File testPropertyFile = new File(MainFileStorageTest.class.getClassLoader()
				.getResource("main.test.properties").getFile());
		mainFileStorage = new MainFileStorage(testPropertyFile);
		Properties props = new Properties();
		props.load(MainFileStorageTest.class.getClassLoader().getResourceAsStream(
				"main.test.properties"));
		Path dir = new File(props.getProperty("dir")).toPath();
		if (dir != null) {
			if (Files.exists(dir)) {
				Files.walkFileTree(dir, treeDeleteVisitor);
			}
			Files.createDirectories(dir);
		}

		Files.createDirectories(dir.resolve("40").resolve("1"));
		Files.copy(new ByteArrayInputStream("We don't like as very much".getBytes()),
				dir.resolve("40").resolve("1").resolve("a"));
		Files.copy(new ByteArrayInputStream("We don't like bs very much either".getBytes()), dir
				.resolve("40").resolve("1").resolve("b"));
		Thread.sleep(1000);
		Files.createDirectories(dir.resolve("30").resolve("2"));
		Files.copy(new ByteArrayInputStream("Cs are quite nice".getBytes()), dir.resolve("30")
				.resolve("2").resolve("c"));
		Thread.sleep(1000);
		Files.createDirectories(dir.resolve("20").resolve("7"));
		Files.copy(new ByteArrayInputStream("We don't like as very much".getBytes()),
				dir.resolve("20").resolve("7").resolve("a"));
		Files.copy(new ByteArrayInputStream("We don't like bs very much either".getBytes()), dir
				.resolve("20").resolve("7").resolve("b"));
		Files.copy(new ByteArrayInputStream("Ds are overrated".getBytes()), dir.resolve("20")
				.resolve("7").resolve("d"));
		Thread.sleep(1000);
		Files.createDirectories(dir.resolve("20").resolve("17"));
		Files.copy(new ByteArrayInputStream("We don't like as very much".getBytes()),
				dir.resolve("20").resolve("17").resolve("a"));
		Thread.sleep(1000);
		Files.createDirectories(dir.resolve("20").resolve("27"));
		Files.copy(new ByteArrayInputStream("We don't like as very much".getBytes()),
				dir.resolve("20").resolve("27").resolve("a"));
	}

	@Test
	public void testGetDatafilesToArchive() throws Exception {
		List<DfInfo> dfInfos = mainFileStorage.getDatafilesToArchive(37000, 37070);
		System.out.println(dfInfos);
		assertEquals(0, dfInfos.size());

		dfInfos = mainFileStorage.getDatafilesToArchive(37000, 37060);
		System.out.println(dfInfos);
		assertEquals(3, dfInfos.size());

		dfInfos = mainFileStorage.getDatafilesToArchive(36950, 37060);
		System.out.println(dfInfos);
		assertEquals(5, dfInfos.size());

		dfInfos = mainFileStorage.getDatafilesToArchive(0, 37060);
		System.out.println(dfInfos);
		assertEquals(8, dfInfos.size());
	}

	@Test
	public void testGetDatasetsToArchive() throws Exception {
		List<DsInfo> dsInfos = mainFileStorage.getDatasetsToArchive(37000, 37070);
		System.out.println(dsInfos);
		assertEquals(0, dsInfos.size());

		dsInfos = mainFileStorage.getDatasetsToArchive(37000, 37060);
		System.out.println(dsInfos);
		assertEquals(2, dsInfos.size());

		dsInfos = mainFileStorage.getDatasetsToArchive(36950, 37060);
		System.out.println(dsInfos);
		assertEquals(4, dsInfos.size());

		dsInfos = mainFileStorage.getDatasetsToArchive(0, 37060);
		System.out.println(dsInfos);
		assertEquals(5, dsInfos.size());
	}

}
