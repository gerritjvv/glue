package org.glue.modules.hadoop.impl

import java.util.concurrent.ConcurrentHashMap

import org.apache.commons.pool.KeyedPoolableObjectFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.compress.CompressionCodecFactory
import org.apache.hadoop.io.compress.Decompressor
import org.eclipse.jdt.core.dom.ThisExpression;
import org.glue.unit.exceptions.ModuleConfigurationException

/**
 * 
 * Implements the factory for creating and managing Decompressor instances.
 *
 */
@Typed
class DecompressorPoolableObjectFactory implements KeyedPoolableObjectFactory{

	Map<String, String> hdfsConfigurations
	/**
	 * Each cluster has its own compression codecs
	 */
	Map<String, CompressionCodecFactory> codecFactoryMap =  new ConcurrentHashMap<String, CompressionCodecFactory>()
	String defaultConfigurationName

	DecompressorPoolableObjectFactory(String defaultConfigurationName, Map<String, String> hdfsConfigurations){
		this.defaultConfigurationName = defaultConfigurationName
		this.hdfsConfigurations = hdfsConfigurations
	}

	void activateObject(Object key, Object obj){
	}

	void destroyObject(Object key, Object obj){
		((DecompressorValue)obj).decompressor.end()
	}

	/**
	 * Returns a new Decompressor
	 */
	Object makeObject(Object key){
		DecompressorKey decomKey = (DecompressorKey)key
		DecompressorValue value

		println("DecompressorPoolableObjectFactory: key.file " + decomKey.file)
		def codec = getCompressionCodecFactory(decomKey.clusterName).getCodec(new Path(decomKey.file))
		if(codec){

			Decompressor dec = codec.createDecompressor()
			value = new DecompressorValue(codec:codec, decompressor:dec)
		}

		return value
	}

	void passivateObject(Object key, Object obj){
		((DecompressorValue)obj).decompressor.reset()
	}

	boolean	validateObject(Object key, Object obj){
	}

	/**
	 * Returns the correct compression codec factory for the cluster
	 * @param clusterName
	 * @return CompressionCodecFactory
	 */
	CompressionCodecFactory getCompressionCodecFactory(String clusterName){

		if(!clusterName){
			clusterName = defaultConfigurationName
		}

		CompressionCodecFactory fact = codecFactoryMap[clusterName]

		if(!fact){

			synchronized (codecFactoryMap) {
				fact = codecFactoryMap[clusterName]
				if(!fact){
					fact = new CompressionCodecFactory(getHdfsConf())
					codecFactoryMap[clusterName] = fact
				}
			}
		}

		return fact
	}

	/**
	 * Loads the appropriate hdfs configuration
	 */
	private Configuration getHdfsConf(String configName=null) {
		//Loading hdfsConfig
		println "Loading HdfsModule Configuration form scratch $configName"
		try{

			Properties props = new Properties();

			def hdfsPropFileName= (configName) ? hdfsConfigurations[configName] : null;

			if(!hdfsPropFileName) hdfsPropFileName=hdfsConfigurations[defaultConfigurationName];

			if(!hdfsPropFileName){
				throw new ModuleConfigurationException("Cannot find file  $hdfsPropFileName please make sure a default cluster is defined")
			}

			File myFile = new File(hdfsPropFileName)
			if(!myFile.exists()){
				throw new ModuleConfigurationException("Cannot find file  $hdfsPropFileName")
			}

			new File (hdfsPropFileName).withInputStream { input ->
				props.load(input);
			}


			Configuration conf = new Configuration();
			props.each { String key, String val ->
				conf.set( key, val)
			}

			return conf
		}catch(Throwable t){
			throw new ModuleConfigurationException(t.toString(), null, t)
		}
	}
}
