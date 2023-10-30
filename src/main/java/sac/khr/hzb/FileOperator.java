package sac.khr.hzb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileOperator {

	public Map<String, Integer> readFile(String fileName) {

		Map<String, Integer> map = new HashMap<>();

		try {
			try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
				stream.forEach(s -> {
					if (map.containsKey(s)) {
						Integer count = map.get(s);
						map.put(s, ++count);
					}else {
						map.put(s, 1);
					}
					//System.out.println(s);
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return map;
	}

}
