package org.glue.modules

import org.apache.commons.io.IOUtils
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectResult
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectSummary

/**
 * 
 * Module providing access to the amazon s3 service.
 * 
 */
@Typed(TypePolicy.DYNAMIC)
class S3Module implements GlueModule{

	Map<String, AWSCredentials> configs = [:]
	String defaultKey

	/**
	 * Contains the default buckets defined key=server name, value = bucket
	 */
	Map<String, String> buckets = [:]
	Map<String, AmazonS3Client> clients = [:]


	S3Object getFileAsObject(String file){
		getFileAsObject(null, null, file)
	}
	S3Object getFileAsObject(String bucket, String file){
		getFileAsObject(null, bucket, file)
	}
	S3Object getFileAsObject(String server, String bucket, String file){
		return getClient(server).getObject(getBucket(server, bucket), file)
	}


	long getSize(String file){
		getSize(null, null, file)
	}
	long getSize(String bucket, String file){
		getSize(null, bucket, file)
	}
	long getSize(String server, String bucket, String file){
		return getClient(server).getObjectMetadata(getBucket(server, bucket), file)?.getContentLength();
	}


	ObjectMetadata getFileMetaData(String file){
		getFileMetaData(null, null, file)
	}
	ObjectMetadata getFileMetaData(String bucket, String file){
		getFileMetaData(null, bucket, file)
	}
	ObjectMetadata getFileMetaData(String server, String bucket, String file){
		return getClient(server).getObjectMetadata(getBucket(server, bucket), file)
	}

	void getFile(String file, String dest){
		getFile(null, null, file, dest)
	}

	void getFile(String bucket, String file, String dest){
		getFile(null, bucket, file, dest)
	}

	/**
	 * Downloads the file from S3 to the location destination file
	 * @param file
	 * @param dest
	 */
	void getFile(String server, String bucket, String file, String dest){
		S3Object obj = getFileAsObject(server, getBucket(server, bucket), file)

		def input = new BufferedInputStream(obj.getObjectContent())
		def output = new BufferedOutputStream(new FileOutputStream(dest))
		try{
			IOUtils.copy(input, output)
		}finally{
			input.close()
			output.close()
		}
	}



	void deleteFile(String file){
		deleteFile(null, null, file)
	}


	void deleteFile(String bucket, String file){
		deleteFile(null, bucket, file)
	}

	void deleteFile(String server, String bucket, String file){
		getClient(server).deleteObject(getBucket(server, bucket), file)
	}

	
	PutObjectResult putFile(InputStream input, ObjectMetadata metadata, String dest){
		putFile(null, null, input, metadata, dest)	
	}
	
	PutObjectResult putFile(String bucket, InputStream input, ObjectMetadata metadata, String dest){
		putFile(null, bucket, input, metadata, dest)
	}
	
	PutObjectResult putFile(String server, String bucket, InputStream input, ObjectMetadata metadata, String dest){
		return getClient(server).putObject(getBucket(server, bucket), dest, input)
	}



	PutObjectResult putFile(String file, String dest){
		putFile(null, null, file, dest)
	}

	PutObjectResult putFile(String bucket, String file, String dest){
		putFile(null, bucket, file, dest)
	}

	PutObjectResult putFile(String server, String bucket, String file, String dest){
		return getClient(server).putObject(getBucket(server, bucket), dest, new File(file))
	}

	Bucket createBucket(String server=null, String bucket){
		return getClient(server).createBucket(bucket)
	}

	boolean bucketExist(String server=null, String bucket){
		return getClient(server).doesBucketExist(bucket)
	}

	void deleteBucket(String server=null, String bucket){
		getClient(server).deleteBucket(bucket)
	}

	String getBucketLocation(String server=null, String bucket){
		return getClient(server).getBucketLocation(bucket)
	}

	List<Bucket> listBuckets(String server=null){
		return getClient(server).listBuckets()
	}


	List<String> listFiles(){
		listFiles(null, null, "/")
	}

	List<String> listFiles(String path){
		listFiles(null, null, path)
	}

	List<String> listFiles(String bucket, String path){
		listFiles(null, bucket, path)
	}

	List<String> listFiles(String server, String bucket, String path){
		println "Bucket: ${getBucket(server, bucket)} "
		ObjectListing lists = (path)? getClient(server).listObjects(getBucket(server, bucket), path) : getClient(server).listObjects(getBucket(server, bucket))

		return lists?.getObjectSummaries()?.collect { S3ObjectSummary summary -> summary.getKey() }
	}

	private String getBucket(String serverName=null, String bucket=null){
		if(bucket)
			return bucket
		else{
			String lname = (serverName) ? serverName : defaultKey
			String defaultBucket = buckets[lname]

			if(!defaultBucket)
				throw new ModuleConfigurationException("No bucket specified and no default bucket defined in the configuration for $lname")

			return defaultBucket
		}
	}

	/**
	 * Ensures that only one AmazonS3Client per server name is created
	 * @return
	 */
	public synchronized AmazonS3Client getClient(String name=null){
		String lname = name

		if(!lname) lname = defaultKey

		if(!configs[lname])
			throw new ModuleConfigurationException("No configuration found for $lname");



		AmazonS3Client client = clients[lname]

		if(!client){
			if(!configs[lname])
				throw new ModuleConfigurationException("Configs should not be empty here, please check your configuration")

			client = new AmazonS3Client(configs[lname])
			clients[lname] = client
		}

		return client
	}


	void init(ConfigObject config){

		if(!config?.servers)
			throw new ModuleConfigurationException("No servers defined");


		config?.servers.each { name, ConfigObject conf ->

			def secretKey = conf?.secretKey?.toString()
			def accessKey = conf?.accessKey?.toString()

			if(!secretKey)
				throw new ModuleConfigurationException("No secretKey defined for $name")

			if(!accessKey)
				throw new ModuleConfigurationException("No accessKey defined for $name")


			configs[name] = new BasicAWSCredentials(accessKey, secretKey)


			if(conf?.bucket)
				buckets[name] = conf?.bucket.toString()

			if(conf?.isDefault || !defaultKey)
				defaultKey = name
		}

		if(!defaultKey)
			throw new ModuleConfigurationException("No default server was defined")
	}

	void onUnitStart(GlueUnit unit, GlueContext context){
	}
	void onUnitFinish(GlueUnit unit, GlueContext context){
	}
	void onUnitFail(GlueUnit unit, GlueContext context){
	}

	Boolean canProcessRun(GlueProcess process, GlueContext context){
	}
	void onProcessStart(GlueProcess process,GlueContext context){
	}
	void onProcessFinish(GlueProcess process, GlueContext context){
	}
	void onProcessFail(GlueProcess process, GlueContext context, Throwable t){
	}

	void onProcessKill(GlueProcess process, GlueContext context){
	}

	String getName(){
	}

	void destroy(){

		clients.each { String key, AmazonS3Client client -> client.shutdown() }
	}

	public Map getInfo(){
		[:]
	}

	void configure(String unitId, ConfigObject config){
	}
}
