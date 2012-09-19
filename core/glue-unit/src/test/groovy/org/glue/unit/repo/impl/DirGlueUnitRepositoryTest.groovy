package org.glue.unit.repo.impl;
import static org.junit.Assert.*

import org.glue.unit.om.GlueUnit
import org.glue.unit.om.impl.DefaultGlueUnitBuilder
import org.glue.unit.om.impl.GlueModuleFactoryImpl
import org.junit.Test

/**
 * 
 * This class tests the iterating capabilities of the DirGlueUnitRepository.
 *
 */
class DirGlueUnitRepositoryTest {

	/**
	 * Iterate through all files
	 */
	@Test
	public void testIterator(){
		ConfigObject moduleFactoryConfig = new ConfigObject();
		def glueModuleFactory = new GlueModuleFactoryImpl(moduleFactoryConfig)

		File dir1 = new File("src/test/resources/test-flow-repo")
		File dir2 = new File("src/test/resources/test-flow-repo2")

		def repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			dir1.getAbsolutePath(),
			dir2.getAbsolutePath()
		])

		Iterator<GlueUnit> iterator = repo.iterator()

		int count = 0
		while(iterator.hasNext()){
			println iterator.next()
			count++
		}

		assertEquals(4, count)
	}

	/**
	 * Iterate with a max limit
	 */
	@Test
	public void testIteratorFromMax(){
		ConfigObject moduleFactoryConfig = new ConfigObject();
		def glueModuleFactory = new GlueModuleFactoryImpl(moduleFactoryConfig)

		File dir1 = new File("src/test/resources/test-flow-repo")
		File dir2 = new File("src/test/resources/test-flow-repo2")

		def repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			dir1.getAbsolutePath(),
			dir2.getAbsolutePath()
		])

		Iterator<GlueUnit> iterator = repo.iterator(0, 2)

		int count = 0
		while(iterator.hasNext()){
			println iterator.next()
			count++
		}

		assertEquals(2, count)
	}

	/**
	 * Check that if max is over the file count its treated correctly in that its down sized to be equal to the number of files.
	 */
	@Test
	public void testIteratorFromMaxLargerThanSize(){
		ConfigObject moduleFactoryConfig = new ConfigObject();
		def glueModuleFactory = new GlueModuleFactoryImpl(moduleFactoryConfig)

		File dir1 = new File("src/test/resources/test-flow-repo")
		File dir2 = new File("src/test/resources/test-flow-repo2")

		def repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			dir1.getAbsolutePath(),
			dir2.getAbsolutePath()
		])

		Iterator<GlueUnit> iterator = repo.iterator(0, 100)

		int count = 0
		while(iterator.hasNext()){
			println iterator.next()
			count++
		}

		assertEquals(4, count)
	}

	/**
	 * Check that if from is out of bounds an empty iterator is returned
	 */
	@Test
	public void testIteratorFromOutOfBounds(){
		ConfigObject moduleFactoryConfig = new ConfigObject();
		def glueModuleFactory = new GlueModuleFactoryImpl(moduleFactoryConfig)

		File dir1 = new File("src/test/resources/test-flow-repo")
		File dir2 = new File("src/test/resources/test-flow-repo2")

		def repo = new DirGlueUnitRepository(new DefaultGlueUnitBuilder(), [
			dir1.getAbsolutePath(),
			dir2.getAbsolutePath()
		])

		Iterator<GlueUnit> iterator = repo.iterator(100, 100)

		int count = 0
		while(iterator.hasNext()){
			println iterator.next()
			count++
		}

		assertEquals(0, count)
	}
}
