package de.uni_mannheim.informatik.dws.dwslib.framework;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * This class provides a framework to process a given set of inputs in parallel
 * using a set of threads.
 * 
 * @author Robert
 * 
 */
public abstract class Processor<E> {

	private int threads;
	private static Logger log;
	private List<E> objectToProcess;

	public Processor(int threads) {
		if (threads < 1) {
			System.out
					.println("Number of threads will be set to number of avaible processors.");
			this.threads = Runtime.getRuntime().availableProcessors();
		} else {
			this.threads = threads;
		}
		try {
			log = Logger.getLogger(getClass().getEnclosingClass()
					.getSimpleName());
		} catch (NullPointerException ne) {
			System.out.println("Could not obtain class name");
			log = Logger.getLogger("Processor.java");
		}
	}

	/**
	 * Keeps track of the process and the current status of the
	 * {@link ThreadPoolExecutor}
	 * 
	 * @param executor
	 *            the {@link ThreadPoolExecutor} including all threads to be
	 *            processed
	 * @param startTime
	 *            The time the executor was started.
	 * @return The difference between the total number of threads and the number
	 *         of finished threads.
	 */
	private long printState(ThreadPoolExecutor executor, long startTime) {
		long total = executor.getTaskCount();
		long finished = executor.getCompletedTaskCount();
		long runtime = (System.currentTimeMillis() - startTime);
		long perItem = (long) (((float) runtime) / finished);
		long left = (long) (((float) runtime / finished) * (total - finished));
		
		if((((float) runtime) / finished)==Float.POSITIVE_INFINITY)
		{
			perItem = -1;
			left = -1;
		}
			
		System.out.println(new Date()
				 + " Runtime: " + DurationFormatUtils.formatDuration(runtime, "HH:mm:ss.S")
				 + " --> Total: " + total
				 + ", Done: " + finished
				 + ", " + (perItem==-1 ? "..." : DurationFormatUtils.formatDuration(perItem, "HH:mm:ss.S")) + " / item"
				 + ", Finished in: " + (left==-1 ? "..." : DurationFormatUtils.formatDuration(left, "HH:mm:ss.S")));
		 
		/*System.out
				.printf("Runtime: %ds --> Total: %d, Done: %d, %ss / item, Finished in: %ds \n",
						runtime, 
						total, 
						finished,
						String.format("%.2f", ((float) runtime) / finished),
						(int) (((float) runtime / finished) * (total - finished)));*/

		return total - finished;
	}

	protected abstract List<E> fillListToProcess();

	public void process() throws FileNotFoundException, IOException {
		long startTime = new Date().getTime();
		log.log(Level.INFO, new Date() + " " + "Starting.");

		objectToProcess = fillListToProcess();

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(threads);
		for (E object : objectToProcess) {
			executor.submit(new Worker<E>(object, this, executor));
		}
		long stillTodo = printState(executor, startTime);
		while (stillTodo != 0) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			stillTodo = printState(executor, startTime);
		}

		executor.shutdown();
		log.log(Level.INFO, new Date() + " " + "Done.");
	}

	private class Worker<E> implements Runnable {
		private E object;
		private Processor<E> p;
		private ThreadPoolExecutor executor;

		public Worker(E object, Processor<E> p, ThreadPoolExecutor executor) {
			this.object = object;
			this.p = p;
			this.executor = executor;
		}

		public void run() {
			try
			{
				p.process(object);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				requeue();
			}
		}
		
		private void requeue()
		{
			log.log(Level.INFO, new Date() + " requeue worker for " + object.toString());
			executor.submit(this);
		}

	}

	protected abstract void process(E object);

}
