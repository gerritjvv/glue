package org.glue.modules
import com.amazonaws.SDKGlobalConfiguration
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.event.ProgressEventType
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.TransferManager
import org.apache.commons.io.IOUtils
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

import java.security.MessageDigest
/**
 *
 * Module providing access to the amazon s3 service.
 *
 */
@Typed(TypePolicy.DYNAMIC)
class S3Module implements GlueModule {

    Map<String, AWSCredentials> configs = [:]
    String defaultKey

    def regionMap = ["tokyo": "ap-northeast-1",
                     "singapore": "ap-southeast-1",
                     "sydney": "ap-southeast-2",
                     "frankfurt": "eu-central-1",
                     "ireland": "eu-west-1",
                     "sao paulo": "sa-east-1",
                     "saopaulo": "sa-east-1",
                     "n. virginia": "us-east-1",
                     "virginia": "us-east-1",
                     "n. california": "us-west-1",
                     "california": "us-west-1",
                     "oregon": "us-west-2"]

    /**
     * Contains the default buckets defined key=server name, value = bucket
     */
    Map<String, String> buckets = [:]
    Map<String, AmazonS3Client> clients = [:]

    def s3configFile

    def regions = [:]

    def transferManagers = [:]

    S3Object getFileAsObject(String file) {
        getFileAsObject(null, null, file)
    }

    S3Object getFileAsObject(String bucket, String file) {
        getFileAsObject(null, bucket, file)
    }

    S3Object getFileAsObject(String server, String bucket, String file) {
        return getClient(server).getObject(getBucket(server, bucket), file)
    }


    String getMD5(String file) {
        getFileMetaData(file).getETag()
    }

    long getSize(String file) {
        getSize(null, null, file)
    }

    long getSize(String bucket, String file) {
        getSize(null, bucket, file)
    }

    long getSize(String server, String bucket, String file) {
        return getClient(server).getObjectMetadata(getBucket(server, bucket), file)?.getContentLength();
    }


    ObjectMetadata getFileMetaData(String file) {
        getFileMetaData(null, null, file)
    }

    ObjectMetadata getFileMetaData(String bucket, String file) {
        getFileMetaData(null, bucket, file)
    }

    ObjectMetadata getFileMetaData(String server, String bucket, String file) {
        return getClient(server).getObjectMetadata(getBucket(server, bucket), file)
    }

    void getFile(String file, String dest) {
        getFile(null, null, file, dest)
    }

    void getFile(String bucket, String file, String dest) {
        getFile(null, bucket, file, dest)
    }

    /**
     * Downloads the file from S3 to the location destination file
     * @param file
     * @param dest
     */
    void getFile(String server, String bucket, String file, String dest) {
        S3Object obj = getFileAsObject(server, getBucket(server, bucket), file)

        def input = new BufferedInputStream(obj.getObjectContent())
        def output = new BufferedOutputStream(new FileOutputStream(dest))
        try {
            IOUtils.copy(input, output)
        } finally {
            input.close()
            output.close()
        }
    }


    void deleteFile(String file) {
        deleteFile(null, null, file)
    }


    void deleteFile(String bucket, String file) {
        deleteFile(null, bucket, file)
    }

    void deleteFile(String server, String bucket, String file) {
        getClient(server).deleteObject(getBucket(server, bucket), file)
    }


    PutObjectResult putFile(InputStream input, ObjectMetadata metadata, String dest) {
        putFile(null, null, input, metadata, dest)
    }

    PutObjectResult putFile(String bucket, InputStream input, ObjectMetadata metadata, String dest) {
        putFile(null, bucket, input, metadata, dest)
    }

    PutObjectResult putFile(String server, String bucket, InputStream input, ObjectMetadata metadata, String dest) {
        return getClient(server).putObject(getBucket(server, bucket), dest, input)
    }


    PutObjectResult putFile(String file, String dest) {
        putFile(null, null, file, dest)
    }

    PutObjectResult putFile(String bucket, String file, String dest) {
        putFile(null, bucket, file, dest)
    }

    PutObjectResult streamFile(InputStream input, String dest) {
        streamFile(null, null, input, dest, -1)
    }

    PutObjectResult streamFile(String bucket, InputStream input, String dest) {
        streamFile(null, bucket, input, dest, -1)
    }

    /**
     * InputStream is closed after close
     */
    PutObjectResult streamFile(String server, String bucket, InputStream input, String dest, long contentSize) {
        def bytesTransfered = 0
        def i = 0

        def meta = new ObjectMetadata();
        if(contentSize > 0)
            meta.setContentLength(contentSize)

        def putReq = new PutObjectRequest(getBucket(server, bucket), dest, input, meta)

        putReq.withGeneralProgressListener(new com.amazonaws.event.ProgressListener() {
            @Override
            void progressChanged(com.amazonaws.event.ProgressEvent progressEvent) {
                switch (progressEvent.getEventType()) {
                    case ProgressEventType.TRANSFER_CANCELED_EVENT:
                        println("Error: " + progressEvent.getEventType()); break;
                    case ProgressEventType.TRANSFER_FAILED_EVENT:
                        println("Error: " + progressEvent.getEventType()); break;
                    case ProgressEventType.TRANSFER_COMPLETED_EVENT:
                        println(fileSize + " of " + fileSize)
                        println("Complete")
                        break;
                    default:
                        bytesTransfered += progressEvent.getBytesTransferred()
                        if (i++ % 100 == 0)
                            println(bytesTransfered + " of " + fileSize)
                }
            }
        })

        PutObjectResult res = getClient(server).putObject(putReq)
    }

    PutObjectResult putFile(String server, String bucket, String file, String dest) {
        def localFile = new File(file)
        def fileSize = localFile.length()
        def md5Local = localMD5(file)
        try {

            //remove any duplicate path variables
            def path = new File(getBucket(server, bucket) + "/" + dest).path

            if (s3configFile) {
                println("s3cmd -c " + s3configFile + " sync " + file)
                def p = [
                        "s3cmd",
                        "-c",
                        s3configFile,
                        "sync",
                        file,
                        "s3://" + path
                ].execute()
                p.waitForProcessOutput(System.out, System.err)

                if (p.exitValue())
                    throw new RuntimeException("Error running s3cmd")
            } else {
                println("Java API put")
                for (int retryCount = 0; retryCount < 3; retryCount++) {
                    def bytesTransfered = 0
                    def i = 0

                    if(dest.startsWith('/'))
                        dest = dest.substring(1, dest.length())

                    def putReq = new PutObjectRequest(getBucket(server, bucket), new File(dest).path, localFile)
                            .withMetadata(new ObjectMetadata())

                    putReq.withGeneralProgressListener(new com.amazonaws.event.ProgressListener() {
                        @Override
                        void progressChanged(com.amazonaws.event.ProgressEvent progressEvent) {
                            switch (progressEvent.getEventType()) {
                                case ProgressEventType.TRANSFER_CANCELED_EVENT:
                                    println("Error: " + progressEvent.getEventType()); break;
                                case ProgressEventType.TRANSFER_FAILED_EVENT:
                                    println("Error: " + progressEvent.getEventType()); break;
                                case ProgressEventType.TRANSFER_COMPLETED_EVENT:
                                    println(fileSize + " of " + fileSize)
                                    println("Complete")
                                    break;
                                default:
                                    bytesTransfered += progressEvent.getBytesTransferred()
                                    if (i++ % 100 == 0)
                                        println(bytesTransfered + " of " + fileSize)
                            }
                        }
                    })

                    PutObjectResult res = getClient(server).putObject(putReq)

                    println("md5Local:  $md5Local  == ${getMD5(dest)}  ; $dest")

                    //check that the md5 checksum is correct
                    if (md5Local.equals(getMD5(dest)))
                        return res
                    else {
                        Thread.sleep(1000)
                        if (md5Local.equals(getMD5(dest)))
                            return res
                        else
                            println("MD5s for remote and local do not match -- retrying ${retryCount + 1} of 3")
                    }
                }

                throw new RuntimeException("Unable to copy file to remote location")
            }
        } catch (Throwable t) {
            t.printStackTrace()
            throw t
        }

    }

    private String localMD5(String file) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        new File(file).withInputStream() { is ->
            byte[] buffer = new byte[8192]
            int read = 0
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] md5sum = digest.digest()
        BigInteger bigInt = new BigInteger(1, md5sum)
        return bigInt.toString(16)
    }

    Bucket createBucket(String server = null, String bucket) {
        return getClient(server).createBucket(bucket)
    }

    boolean bucketExist(String server = null, String bucket) {
        return getClient(server).doesBucketExist(bucket)
    }

    void deleteBucket(String server = null, String bucket) {
        getClient(server).deleteBucket(bucket)
    }

    String getBucketLocation(String server = null, String bucket) {
        return getClient(server).getBucketLocation(bucket)
    }

    List<Bucket> listBuckets(String server = null) {
        return getClient(server).listBuckets()
    }


    List<String> listFiles() {
        listFiles(null, null, "/")
    }

    List<String> listFiles(String path) {
        listFiles(null, null, path)
    }

    List<String> listFiles(String bucket, String path) {
        listFiles(null, bucket, path)
    }

    List<String> listFiles(String server, String bucket, String path) {
        println "Bucket: ${getBucket(server, bucket)} "
        ObjectListing lists = (path) ? getClient(server).listObjects(getBucket(server, bucket), path) : getClient(server).listObjects(getBucket(server, bucket))

        return lists?.getObjectSummaries()?.collect { S3ObjectSummary summary -> summary.getKey() }
    }

    private String getBucket(String serverName = null, String bucket = null) {
        if (bucket)
            return bucket
        else {
            String lname = (serverName) ? serverName : defaultKey
            String defaultBucket = buckets[lname]

            if (!defaultBucket)
                throw new ModuleConfigurationException("No bucket specified and no default bucket defined in the configuration for $lname")

            return defaultBucket
        }
    }

    public synchronized  TransferManager getTransferManager(String name = null){
        String lname = name;
        if(!lname) lname = defaultKey

        AmazonS3Client client = getClient(lname)
        TransferManager manager = transferManagers[lname]
        if(manager == null){
            manager = new TransferManager(client)
            transferManagers[lname] = manager
        }

        return manager
    }

    /**
     * Ensures that only one AmazonS3Client per server name is created
     * @return
     */
    public synchronized AmazonS3Client getClient(String name = null) {
        String lname = name

        if (!lname) lname = defaultKey

        if (!configs[lname])
            throw new ModuleConfigurationException("No configuration found for $lname");



        AmazonS3Client client = clients[lname]

        if (!client) {
            if (!configs[lname])
                throw new ModuleConfigurationException("Configs should not be empty here, please check your configuration")

            client = new AmazonS3Client(configs[lname])
            client.setRegion(regions[lname])
            clients[lname] = client
        }

        return client
    }


    private String getRegionCode(String nameOrCode){
        def regionCode = regionMap[nameOrCode.toLowerCase()]
        if(regionCode)
            return regionCode
        else
            return nameOrCode
    }

    void init(ConfigObject config) {

        System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        System.setProperty(SDKGlobalConfiguration.ENFORCE_S3_SIGV4_SYSTEM_PROPERTY, "true");


        if (!config?.servers)
            throw new ModuleConfigurationException("No servers defined");


        if (config?.s3cmd)
            s3configFile = config.s3cmd


        config?.servers.each { name, ConfigObject conf ->

            def secretKey = conf?.secretKey?.toString()
            def accessKey = conf?.accessKey?.toString()

            def region = conf?.region?.toString();
            def domain = conf?.domain?.toString();

            if (!secretKey)
                throw new ModuleConfigurationException("No secretKey defined for $name")

            if (!accessKey)
                throw new ModuleConfigurationException("No accessKey defined for $name")

            if (!region)
                throw new ModuleConfigurationException("No region defined for $name")


            if (!domain)
                throw new ModuleConfigurationException("No domain defined for $name")


            configs[name] = new BasicAWSCredentials(accessKey, secretKey)
            regions[name] = new com.amazonaws.regions.Region(getRegionCode(region), domain)

            if (conf?.bucket)
                buckets[name] = conf?.bucket.toString()

            if (conf?.isDefault || !defaultKey)
                defaultKey = name

        }

        if (!defaultKey)
            throw new ModuleConfigurationException("No default server was defined")
    }

    void onUnitStart(GlueUnit unit, GlueContext context) {
    }

    void onUnitFinish(GlueUnit unit, GlueContext context) {
    }

    void onUnitFail(GlueUnit unit, GlueContext context) {
    }

    Boolean canProcessRun(GlueProcess process, GlueContext context) {
    }

    void onProcessStart(GlueProcess process, GlueContext context) {
    }

    void onProcessFinish(GlueProcess process, GlueContext context) {
    }

    void onProcessFail(GlueProcess process, GlueContext context, Throwable t) {
    }

    void onProcessKill(GlueProcess process, GlueContext context) {
    }

    String getName() {
    }

    void destroy() {
        transferManagers.each {String key, TransferManager manager -> manager.shutdownNow()}
        clients.each { String key, AmazonS3Client client -> client.shutdown() }
    }

    public Map getInfo() {
        [:]
    }

    void configure(String unitId, ConfigObject config) {
    }
}
