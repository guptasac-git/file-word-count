package sac.khr.hzb;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * As a first argument pass absolute path of folder, from where you want to read files
 * Otherwise, it will default pick files from src/test/resources/
 * 
 * 
 */
public class FileManagerApproach1 {

	public static void main(String[] args) {

		FileManagerApproach1 fileManager = new FileManagerApproach1();
		String contextPath = "";

		long startTime = System.currentTimeMillis();
		try {
			if (args != null & args.length > 0) {
				contextPath = args[0];
			} else {
				Path resourceDirectory = Paths.get("src", "test", "resources");
				contextPath = resourceDirectory.toFile().getAbsolutePath();
			}
			fileManager.process(new File(contextPath), contextPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTIme = System.currentTimeMillis();
		System.out.println("Total time taken by method-2 is " + (endTIme - startTime) / 1000 + " seconds\n");
	}

	/**
	 * 1st way to solve
	 * 
	 * @param folder
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public Map<String, Integer> process(final File folder, String contextPath)
			throws InterruptedException, ExecutionException {

		ExecutorService executorService = Executors.newCachedThreadPool();
		List<CompletableFuture<Map<String, Integer>>> futureList = new ArrayList<>();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				process(fileEntry, contextPath);
			} else {
				futureList.add(submitAsyncReq(contextPath, fileEntry, executorService));
			}
		}
		executorService.shutdown();

		List<Map<String, Integer>> resultList = getResult(futureList);

		Map<String, Integer> resultMap = doMerge(resultList);

		// printData(resultMap);

		executorService.awaitTermination(20, TimeUnit.SECONDS);

		return resultMap;
	}

	private Map<String, Integer> doMerge(List<Map<String, Integer>> resultList) {
		
		return resultList.stream().reduce(new HashMap<String, Integer>(), (m1, ele) -> {
			ele.entrySet().stream().forEach(entry -> m1.merge(entry.getKey(), entry.getValue(), Integer::sum));
			return m1;
		});
	}

	private List<Map<String, Integer>> getResult(List<CompletableFuture<Map<String, Integer>>> futureList) {
		
		return futureList.stream().map(f -> {
			try {
				return f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			} catch (ExecutionException e) {
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
	}

	private CompletableFuture<Map<String, Integer>> submitAsyncReq(String contextPath, File fileEntry, ExecutorService executorService) {

		return CompletableFuture.supplyAsync(
				() -> new FileOperator().readFile(contextPath + fileEntry.getName()), executorService);
	}

	private static void printData(Map<String, Integer> re) {

		for (Entry<String, Integer> element : re.entrySet()) {
			System.out.println(element.getKey() + "-" + element.getValue());
		}

	}

}
