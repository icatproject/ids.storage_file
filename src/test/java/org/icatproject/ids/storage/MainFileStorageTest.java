package org.icatproject.ids.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Properties;

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
		Files.createDirectories(dir.resolve("40"));
		Thread.sleep(1000);
		Files.createDirectories(dir.resolve("30"));
		Thread.sleep(1000);
		Files.createDirectories(dir.resolve("20").resolve("7"));
		Thread.sleep(1000);
		Files.createDirectories(dir.resolve("20").resolve("17"));
		Thread.sleep(1000);
		Files.createDirectories(dir.resolve("20").resolve("27"));
	}

	@Test
	public void testSpace() throws Exception {
		assertTrue(mainFileStorage.getUsedSpace() > 1000L);
	}

	@Test
	public void testInv() throws Exception {
		assertEquals(Arrays.asList(40L, 30L, 20L), mainFileStorage.getInvestigations());
	}

	@Test
	public void testDs() throws Exception {
		assertEquals(Arrays.asList(7L, 17L, 27L), mainFileStorage.getDatasets(20L));
	}

}
