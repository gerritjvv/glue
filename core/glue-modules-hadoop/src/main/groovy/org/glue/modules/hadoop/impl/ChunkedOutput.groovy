package org.glue.modules.hadoop.impl

import org.apache.hadoop.conf.Configurable
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.compress.CompressionCodec
import org.apache.hadoop.io.compress.CompressionCodecFactory
import org.apache.hadoop.io.compress.Compressor

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
	BufferedOutputStream writer
	File currentFile

	final int batchCheck
	int records = 0

	final Compressor compressor
	final CompressionCodec codec

	final String extension

	final Closure callback

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
		def fact = new CompressionCodecFactory(conf)

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

	private final void resetWriter() throws FileNotFoundException, IOException {

		final File file = new File(dir, prefix + "-" + System.currentTimeMillis()
				+ extension + "_")

		if (compressor != null)
			compressor.reset()

		writer = new BufferedOutputStream(
				codec.createOutputStream(
				new FileOutputStream(file), compressor))

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
		writer.close();
		String name = currentFile.getAbsolutePath();
		def fileName = new File(name.substring(0, name.length() - 1))

		currentFile.renameTo(fileName);

		callback(fileName)
	}

	
	public void write(String seq) throws IOException {
		checkRoll();
		writer.write(seq.getBytes("UTF-8"))
	}

	public void close() throws IOException {
		doRoll();
	}


	def  write(byte[] data) throws IOException {
		checkRoll();
		writer.write(data);
	}
}
