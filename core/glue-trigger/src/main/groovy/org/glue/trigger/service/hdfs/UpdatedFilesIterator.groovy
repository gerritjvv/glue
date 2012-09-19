package org.glue.trigger.service.hdfs

import org.glue.trigger.persist.TriggerStore

/**
*
* Is a Wrapper that allows the polling module to send the updated files to the glue work flow.
*
*/
@Typed(TypePolicy.MIXED)
class UpdatedFilesIterator implements Iterator<String>, Iterable<String>{

	TriggerStore triggerStore

	Set<String> files = []
	Iterator<String> filesIt

	UpdatedFilesIterator(String unitName, TriggerStore triggerStoreModule){
		this.triggerStore = triggerStoreModule


		triggerStoreModule.listReadyFiles(unitName , { files << it} )

		filesIt = files?.iterator()
	}

	boolean hasNext(){
		filesIt?.hasNext()
	}

	String next(){
		filesIt?.next()
	}

	Iterator<String> iterator(){
		files?.iterator()
	}

	void each(Closure closure){
		files.each { String file ->	closure(file) }
	}

	void remove(){
		throw new RuntimeException("This method is not implemented or meant to be used here")
	}
}