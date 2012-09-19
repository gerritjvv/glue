package org.glue.modules.hadoop.impl

import org.apache.hadoop.io.compress.CompressionCodec
import org.apache.hadoop.io.compress.CompressionInputStream
import org.apache.hadoop.io.compress.Decompressor

/**
 * 
 * Encapsulates the CompressionCodec and Decompressor in the Decompressor Pool
 *
 */
@Typed
class DecompressorValue {

	
	CompressionCodec codec
	Decompressor decompressor
	
	public CompressionInputStream createInputStream(InputStream input){
		return codec.createInputStream(input, decompressor)
	}
	
}
