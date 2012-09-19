package org.glue.unit.repo.impl

import org.glue.unit.om.GlueModuleFactory
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder;
import org.glue.unit.om.impl.GlueUnitImpl

/**
 * 
 * Iterator for files in a directory.
 *
 */
@Typed
class DirGlueUnitIterator  implements Iterator<GlueUnit>{

	Iterator<File> fileIt
	ConfigSlurper configSlurper = new ConfigSlurper()
	GlueUnitBuilder glueUnitBuilder

	int from = 0
	int max = -1

	/**
	 * 
	 * @param glueUnitBuilder
	 * @param dirs
	 * @param from the start index from where to start iterating, if from is bigger than the number of files nothing is returned.
	 * @param max if -1 all files are returned if any other value a maximum of these files are returned
	 */
	public DirGlueUnitIterator(GlueUnitBuilder glueUnitBuilder, List<File> dirs, int from = 0, int max = -1){
		this.glueUnitBuilder = glueUnitBuilder
		this.from = from
		this.max = max

		if(from < 0){
			from = 0
		}

		if(max == 0){
			max = -1
		}

		def files = []

		dirs.each { File dir ->



			dir.eachFileRecurse { File file ->

				if(file.name.endsWith(".groovy") ){
					files << file
				}
			}
		}

		if(from > files.size()){
			files = []
		}else if(max > 0){
			//only apply range if max is larger than zero

			if(from > files.size()){
				files = []
			}else{
				//ensure that the max is within bounds
				//note that we subtract 1 from max to convert it to an index
				int toIndex = from + (max-1)
				if(toIndex > files.size()){
					toIndex = files.size() - 1
				}

				files = files[from..toIndex]
			}
		}


		fileIt = files.iterator()
	}

	public GlueUnit next() {
		File file = fileIt.next()

		GlueUnit unit = null

		if(file){
		  try{
			unit = glueUnitBuilder.build(file.toURI().toURL())

			//we must make sure that the name of the GlueUnit is equal to that of its file
			String fileName = file.name
			if( fileName.endsWith(".groovy")){
				int index = fileName.indexOf(".groovy")-1
				fileName = fileName[0..index]
			}

			unit.name = fileName
		  }catch(Throwable t){
		     println "Error building GlueUnit $file $t"
			 //call next again to retrieve the next glue unit
			 if(hasNext())
			 	unit = next()
		  }
		}

		return unit
	}

	public boolean hasNext(){
		fileIt.hasNext()
	}

	public void remove(){
		fileIt.remove()
	}
}
