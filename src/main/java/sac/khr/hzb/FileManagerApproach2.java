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
import java.util.stream.IntStream;

/*
 * As a first argument pass absolute path of folder, from where you want to read files
 * Otherwise, it will default pick files from src/test/resources/
 * 
 * 
 */
public class FileManagerApproach2 {

	public static void main(String[] args) {

		FileManagerApproach2 fileManager = new FileManagerApproach2();
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

	private CompletableFuture<Map<String, Integer>> submitAsyncReq(String contextPath, File fileEntry,
			ExecutorService executorService) {

		return CompletableFuture.supplyAsync(() -> new FileOperator().readFile(contextPath + fileEntry.getName()),
				executorService);
	}

	/**
	 * 2nd way to solve this, when payload is very big - this helps in reduce
	 * turnaround time
	 * 
	 * @param folder
	 * @param contextPath
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public Map<String, Integer> process(final File folder, String contextPath)
			throws InterruptedException, ExecutionException {

		ExecutorService executorService = Executors.newCachedThreadPool();
		List<CompletableFuture<Map<String, Integer>>> futureList = new ArrayList<>();
		Map<Integer, Boolean> processState = new HashMap<>();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				process(fileEntry, contextPath);
			} else {
				futureList.add(submitAsyncReq(contextPath, fileEntry, executorService));
			}
		}
		executorService.shutdown();

		Map<String, Integer> resultMap = doMerge(processState, futureList);

		// printData(resultMap);

		executorService.awaitTermination(20, TimeUnit.SECONDS);

		return resultMap;
	}

	private Map<String, Integer> doMerge(Map<Integer, Boolean> processState,
			List<CompletableFuture<Map<String, Integer>>> futureList) throws InterruptedException, ExecutionException {

		Map<String, Integer> resultMap = new HashMap<>();

		while (!check(processState, futureList.size())) {
			for (int i = 0; i < futureList.size(); i++) {
				CompletableFuture<Map<String, Integer>> future = futureList.get(i);
				if (future.isDone() && !processState.containsKey(i)) {
					processState.put(i, Boolean.TRUE);
					Map<String, Integer> localMap = future.get();
					for (Entry<String, Integer> entry : localMap.entrySet()) {
						resultMap.merge(entry.getKey(), entry.getValue(), Integer::sum);
					}
				}
			}
		}

		return resultMap;
	}

	private boolean check(Map<Integer, Boolean> processState, int size) {
		return IntStream.range(0, size).allMatch(i -> processState.containsKey(i) && processState.get(i));
	}

	private static void printData(Map<String, Integer> re) {

		for (Entry<String, Integer> element : re.entrySet()) {
			System.out.println(element.getKey() + "-" + element.getValue());
		}

	}

}
