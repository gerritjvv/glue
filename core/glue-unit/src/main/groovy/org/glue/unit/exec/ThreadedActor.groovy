package org.glue.unit.exec


import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

import org.apache.log4j.Logger

/**
 * This class acts as an actor, but is implemented using java threads.<br/>
 * Why:<br/>
 * If contains one pool to which events can be delivered (messages send asynchronously)<br/>
 * But it differs in that events are processed by a limited number of running threads.<br/>
 * These threads suspend when the pool is empty.<br/>
 * <p/>
 * Events listeners</br>
 * On Error: set the onErrorListener closure, receive T object
 * On Completion : set the onCompletedListener, receive T object
 * <br/>
 * Threads and blocking.<br/>
 * This actor uses the blocking methods of the BlockingQueue i.e. put, and take.<br/>
 * Put will wait until space is available to insert in to the Queue. In the case of an Unbounded queue this will always succeed,
 * provided enough memory is available.
 */
@Typed
abstract class ThreadedActor<T> {

	static final Logger LOG = Logger.getLogger(ThreadedActor)

	protected final BlockingQueue<T> queue
	protected ExecutorService service
	protected int threads

	Closure onErrorListener

	Closure onExecCompletedListener


	/**
	 * A CacchedThreadPool is created.<br/>
	 * ThreadedActor is created with a LinkedBlockingDeque.
	 * @param threads number of threads that reads from the pool
	 */
	public ThreadedActor(int threads){
		this(threads, Executors.newCachedThreadPool())
	}


	/**
	 * ThreadedActor is created with a LinkedBlockingDeque.
	 * @param threads number of thread that reads from the pool
	 * @param service ExecutorService
	 */
	public ThreadedActor(int threads, ExecutorService service){
		this.threads = threads
		this.service = service
		queue = new LinkedBlockingDeque()
	}

	/**
	 * @param threads number of thread that reads from the pool
	 * @param service ExecutorService
	 */
	public ThreadedActor(int threads, ExecutorService service, BlockingQueue<T> queue){
		this.threads = threads
		this.service = service
		this.queue = queue
	}

	void start(){

		//create n number of threads that will read from the queue
		(1..threads).each{
			LOG.debug("Creating thread $it of $threads")
			Runnable runnable = {
				//always loop, unless and InterruptedException is thrown
				while(true){
					def obj = queue.take()
					try{
						react( obj )
						//notify the on execution complete listener

						callWithoutError(onExecCompletedListener, obj)
					}catch(InterruptedException iexp){
						//exit thread
						LOG.error(iexp.toString(), iexp)
						return
					}catch(Throwable t){
						//notify the on error listener
						callWithoutError(onErrorListener, obj, t)
						LOG.error(t.toString(), t)
					}
				}
			} as Runnable
		
			service.submit runnable
		}
	}


	void callWithoutError(Closure closure, Object obj, Throwable t = null){
		try{
			if(closure){
				if(t && closure.getMaximumNumberOfParameters() > 1){
					closure(obj, t)
				}else{
					closure(obj)
				}
			}
		}catch(exp){
			LOG.error(exp.toString(), exp)
		}
	}

	/**
	 * Called when a new  item is seen in the pool.<br/>
	 * This method can be called by any number of threads.<br/>
	 * Throwing an exception is this method, will cause it to be printed<br/>
	 * via the log4j logger.
	 * @param obj
	 */
	abstract void react(T obj)

	/**
	 * Stop but does not wait
	 */
	void stop(){

		service.shutdown()
	}

	boolean isShutdown(){
		return service.isShutdown()
	}

	@Typed(TypePolicy.MIXED)
	void awaitTermination(waitTime, TimeUnit unit){
		service.awaitTermination waitTime, unit
	}

	/**
	 * Kills the current execution service
	 */
	void kill(){
		service.shutdownNow()
	}

	/**
	 * Adds a QueuedWorkflow to the end of the queue
	 * @param qwf
	 */
	void add(T qwf){
		queue.put(qwf)
	}

	/**
	 * Adds a QueuedWorkflow to the end of the queue
	 * @param qwf
	 */
	void leftShift(T qwf){
		add(qwf)
	}

}
