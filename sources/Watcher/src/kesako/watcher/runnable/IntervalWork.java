/*
 * Copyright 2012 Frederic SACHOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kesako.watcher.runnable;

import org.apache.log4j.Logger;
/**
 * Abstract class that construct a new interval thread that will run on the given interval
 * with the given name.
 * @author Frederic SACHOT
 */
public abstract class IntervalWork implements Runnable {
	/**
	 * Log4J logger of the class.
	 */
	private static final Logger logger = Logger.getLogger(IntervalWork.class);
	/**
	 * Whether or not this thread is active.
	 */
	private boolean active = false;
	/**
	 * The interval in milliseconds to run this thread
	 */
	private int interval = -1;
	/**
	 * The name of this pool (for loggin/display purposes).
	 */
	private String threadName;
	/**
	 * This instance's thread
	 */
	private Thread runner;

	/**
	 * Construct a new interval thread that will run on the given interval
	 * with the given name.
	 * @param intervalSeconds the number of seconds to run the thread on
	 * @param name the name of the thread
	 */
	public IntervalWork(int intervalSeconds, String name) {
		logger.debug("Worker constructor name="+name+" interval="+intervalSeconds);
		this.interval = intervalSeconds * 1000;
		this.threadName = giveThreadName(name);
	}

	/**
	 * Start the thread on the specified interval.
	 */
	public void startWorking() {
		logger.debug("start worker");
		active = true;
		//If we don't have a thread yet, make one and start it.
		if (runner == null && interval > 0) {
			logger.debug("runner creation");
			runner = new Thread(this);
			runner.setName(this.threadName);
			runner.setPriority(Thread.MIN_PRIORITY);
			runner.start();          
		}
	}

	/**
	 * Stop the interval thread.
	 */
	public void stopWorking() {
		logger.debug("stop worker");
		active = false;
	}

	/**
	 */
	public void run() {
		logger.debug("run : "+runner.getName());
		//Make this a relatively low level thread
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

		//Pause this thread for the amount of the interval
		while (active) {
			try {
				logger.debug(runner.getName()+" : "+runner.getState());
				doWork();
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				logger.debug(runner.getName()+" : Interruption : "+runner.getState());				
			} catch (Exception e) {
				logger.debug(runner.getName()+" ERROR ",e);				
			}
		}
	}

	/**
	 * String representation of this object. Just the name given to it an instantiation. 
	 * @return the string name of this pool
	 */
	public String toString() {
		return threadName;
	}
	/**
	 * Return the name of the Thread
	 */
	public String getThredName(){
		return threadName;
	}
	/**
	 * The interval has expired and now it's time to do something.
	 */
	protected abstract void doWork();
	/**
	 * Set the interval between two works.
	 * @param interval int value in seconds
	 */
	public void setInterval(int interval) {
		this.interval = interval*1000;
	}
	/**
	 * Return a valid Thread name. A Thread name is a string prefix by "Th_". 
	 * If name is not prefixed by "Th_", the String "Th_" is added in front of name.
	 * @param nom name of the thread. If name is not prefixed by "Th_", the String "Th_" is added in front of name.
	 * @return the valid thread name.
	 */
	public static String giveThreadName(String nom){
		String nomThread;
		if(nom.trim().startsWith("Th_")){
			nomThread = nom;
		}else{
			nomThread="Th_"+nom;
		}
		return nomThread.replace(" ", "_");
	}
	
}

