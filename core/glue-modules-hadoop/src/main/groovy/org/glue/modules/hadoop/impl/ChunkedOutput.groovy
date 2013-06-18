package org.glue.modules.hadoop.impl

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configurable
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.compress.BZip2Codec
import org.apache.hadoop.io.compress.CompressionCodec
import org.apache.hadoop.io.compress.CompressionCodecFactory
import org.apache.hadoop.io.compress.CompressionOutputStream
import org.apache.hadoop.io.compress.Compressor
import org.apache.hadoop.io.compress.GzipCodec
import org.apache.commons.io.IOUtils

/**
 * 
 * Writes out data into compressed chunked files.<br/>
 * This class is not thread safe and should only be accessed by one thread at a time.<br/>
 * 
 */

@Typed(TypePolicy.STATIC)
class ChunkedOutput {

	final File dir
	final String prefix

	final int chunkSize
	CompressionOutputStream writer
	File currentFile

	final int batchCheck
	int records = 0

	final Compressor compressor
	final CompressionCodec codec

	final String extension

	final Closure callback

	final Random random = new Random();
	
	int counter = 0
	
	/**
	 * Writes out chunked files 
	 * @param dir
	 * @param prefix
	 * @param chunkSize
	 * @param compression
	 * @param callback
	 * @throws IOException
	 */
	public ChunkedOutput(File dir, String prefix, int chunkSize, String compression, Closure callback)
	throws IOException {
		this.dir = dir
		this.prefix = prefix
		this.chunkSize = chunkSize

		if (chunkSize < 1000) {
			batchCheck = 10
		} else if (chunkSize < 10000) {
			batchCheck = 1000
		} else {
			batchCheck = 10000
		}

		Configuration conf = new Configuration()

		def codecs =  conf.get("io.compression.codecs", BZip2Codec.class.getName())
		codecs += "," + GzipCodec.class.getName()
		def fact = new CompressionCodecFactory(conf)
		
		conf.set("io.compression.codecs", codecs)
		
		codec = fact.getCodec(new Path("mypath.${compression.toLowerCase()}"))

		if(!codec)
			throw new RuntimeException("No codec found for compression $compression")

		if(codec instanceof Configurable)
			((Configurable)codec).setConf(conf)

		compressor = codec.createCompressor()

		def extensionStr = codec.getDefaultExtension()

		if(!extensionStr.startsWith('.'))
			extension = ".$extensionStr"
		else
			extension = extensionStr

		this.callback = callback
		resetWriter()
	}

	
	private final void waitRandom(){
			Thread.sleep(random.nextInt(2) * 1000)
	}
	
	private final void resetWriter() throws FileNotFoundException, IOException {
		//we wait random to avoid name conflicts between multiple threads and processes plus append a nextInt(100) suffix
		waitRandom()
		final File file = new File(dir, prefix + "-" + System.nanoTime() + "." + (counter++)
				+ extension + "_")

		
		writer = 
				codec.createOutputStream(
				new FileOutputStream(file), compressor)

		currentFile = file;
	}

	private synchronized void checkRoll() throws IOException {
		if (records++ >= batchCheck) {
			writer.flush();
			records = 0;
		}

		if (currentFile.length() >= chunkSize) {
			// close the file
			doRoll();
			resetWriter();
		}
	}

	private synchronized void doRoll() throws IOException {
		
		try{
			writer.finish() //call finish on CompressionOutputStream
		}catch(Exception e){}
		
		IOUtils.closeQuietly(writer); //then close
		
		if(compressor != null)
			compressor.reset()
		
		String name = currentFile.getAbsolutePath();
		def fileName = new File(name.substring(0, name.length() - 1))

		currentFile.renameTo(fileName);
		FileUtils.moveFile(currentFile, fileName)
		
		callback(fileName)
	}

	
	public synchronized void write(String seq) throws IOException {
		checkRoll();
		writer.write(seq.getBytes("UTF-8"))
	}

	public synchronized void close() throws IOException {
		doRoll();
	}


	def synchronized void write(byte[] data) throws IOException {
		checkRoll();
		writer.write(data);
	}
}
