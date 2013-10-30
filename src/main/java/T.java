import java.io.File;
import java.io.IOException;

import org.icatproject.ids.storage.MainFileStorage;

public class T {

	public static void main(String[] args) throws IOException {
		new MainFileStorage(new File("/tmp"));
	}

}
