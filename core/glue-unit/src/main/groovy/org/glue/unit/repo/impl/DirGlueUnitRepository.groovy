package org.glue.unit.repo.impl

import groovy.util.ConfigSlurper

import java.io.File
import java.util.List
import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger
import org.glue.unit.om.GlueUnit
import org.glue.unit.om.GlueUnitBuilder
import org.glue.unit.repo.GlueUnitRepository

/**
 * 
 *  Implements a local file system directory repository for glue.<br/>
 *  Allows multiple directories to be specified.<br/>
 *
 */
@Typed(TypePolicy.MIXED)
class DirGlueUnitRepository implements GlueUnitRepository{

	private static final Logger LOG = Logger.getLogger(DirGlueUnitRepository.class)
	
	List<File> fileDirectories
	
	ConfigSlurper configSlurper = new ConfigSlurper()
	GlueUnitBuilder glueUnitBuilder

	@Typed(TypePolicy.DYNAMIC)
	DirGlueUnitRepository(GlueUnitBuilder glueUnitBuilder, List<String> directories){
		this.glueUnitBuilder = glueUnitBuilder
		fileDirectories = [] as ArrayList
		
		//for each file check that it exists and is a directory
		directories.each { String dir ->
			
			File file = new File(dir)
			if(! ( file.exists() || file.isDirectory() )){
				throw new FileNotFoundException("File directory ${dir} does not exist or is not a directory")
			}
			
			fileDirectories << file
			
		}
		
	   LOG.info "Assigned ${fileDirectories.size()} directories to DirGlueUnitRepository"
		
	}
	
	/**
	* Returns an Iterator for the glue units.
	* @return
	*/
   Iterator<GlueUnit> iterator(){
	   return new DirGlueUnitIterator(glueUnitBuilder, fileDirectories)
   }
   
   /**
   * Returns an Iterator for the glue units.
   * @return
   */
  Iterator<GlueUnit> iterator(int from, int max){
	  return new DirGlueUnitIterator(glueUnitBuilder, fileDirectories, from, max)
  }
  
   /**
	* Returns the total number of glue units
	* @return
	*/
   int size(){
	   AtomicInteger total = new AtomicInteger(0)
	   
	   fileDirectories.eachFileRecursive { file ->
		   
		   if(file.name.endsWith(".groovy")){
			   total.getAndIncrement()
		   }
		   
	   }
	   
	   return total.get()
   }
   
   /**
	* Searches for a glue unit definition by its name
	* @param name
	* @return
	*/
   GlueUnit find(String name){
	   File file = findFile(name)
	   
	   if(file != null){
		   return createGlueUnit(file)
	   }else{
	   		return null
	   }
	   
   }
   
   /**
    * Instantiates a GlueUnit from a File
    * @param file
    * @return
    */
   private GlueUnit createGlueUnit(File file){
	   
	   //usetoURI.toURL for avoid permgen memory error with groovy and config slurper
	   GlueUnit unit = glueUnitBuilder.build(file.toURI().toURL())  
	   
	   String fileName = file.name
	   if( fileName.endsWith(".groovy")){
		   int index = fileName.indexOf(".groovy")-1
		   fileName = fileName[0..index]
	   }
	   
	   unit.name = fileName
	   return unit
   }
   
   /**
    * Loops recursively through each directory and for the first file it finds returns.
    */
   private File findFile(String fileName){
	   
	   String unitFileName = FilenameUtils.removeExtension(new File(fileName).name);
	   
	   //String name = (unitFileName.endsWith(".groovy")) ? unitFileName : "${unitFileName}.groovy"
	   
	   File[] retFile = new File[1];
	   
	   fileDirectories.each { File dir ->
		   dir.eachFileRecurse { File file ->
			   final String currFileName = FilenameUtils.removeExtension(file.name)
			   
			   if((file.name.endsWith(".groovy")
				   || 
				   file.name.endsWith(".jython")
				   ||
				   file.name.endsWith(".scala")
				   ||
				   file.name.endsWith(".js")
				   ||
				   file.name.endsWith(".jrb")
				   ||
				   file.name.endsWith(".jruby")
				   ||
				   file.name.endsWith(".jgo")
				   || file.name.endsWith(".jtcl")
				   )
			   	   && currFileName == unitFileName
			   ){
				   retFile[0] = file
			   }
		   }
		   
	   }
		
	   return retFile[0]
   }
	
   public String toString(){
	   "Directory Repository:[ " + fileDirectories.join(', ') + "]"
   }

}
