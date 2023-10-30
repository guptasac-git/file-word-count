package sac.khr.hzb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;


@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("local")
class FileManagerTest {
	
	FileManagerApproach1 fileManagerApproach1 = new FileManagerApproach1();
	FileManagerApproach2 fileManagerApproach2 = new FileManagerApproach2();
	static String contextPath = "";
	
	@BeforeEach
	private void init() {
		Path resourceDirectory = Paths.get("src", "test", "resources");
		contextPath = resourceDirectory.toFile().getAbsolutePath();
	}
	
	@Test
	void testProcessUsingMethod() throws InterruptedException, ExecutionException {
		
		long startTime = System.currentTimeMillis();
		Map<String, Integer> result1 = fileManagerApproach1.process(new File(contextPath), contextPath+"/");
		long endTIme = System.currentTimeMillis();
		System.out.println("Total time taken by method-1 is " + (endTIme - startTime) / 1000 + " seconds\n");
		
		startTime = System.currentTimeMillis();
		Map<String, Integer> result2 = fileManagerApproach2.process(new File(contextPath), contextPath+"/");
		endTIme = System.currentTimeMillis();
		System.out.println("Total time taken by method-2 is " + (endTIme - startTime) / 1000 + " seconds\n");
		
		assertEquals(result1.get("collocate"), result2.get("collocate"));
		assertEquals(result1.get("anamorphosis"), result2.get("anamorphosis"));
		assertEquals(result1, result2);
	}
	
}

