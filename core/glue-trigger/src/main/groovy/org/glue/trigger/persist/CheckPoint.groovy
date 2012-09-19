package org.glue.trigger.persist

import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus


@Typed
class CheckPoint {

	Date date
	long directoryCount = 0L
	long fileCount = 0L
	long spaceConsumed = 0L

	public static CheckPoint create(ContentSummary contentSummary, Date modificationTime){
		
		return new CheckPoint(directoryCount:contentSummary.directoryCount,
		fileCount:contentSummary.fileCount,
		spaceConsumed:contentSummary.spaceConsumed, date:modificationTime)
			
	}
	
	
	public static CheckPoint create(ContentSummary contentSummary){
		return create(contentSummary, new Date())			
	}
	
	public boolean equals(ContentSummary contentSummary){
	
		return (contentSummary) &&
			   contentSummary.directoryCount == directoryCount &&
		       contentSummary.fileCount == fileCount &&
			   contentSummary.spaceConsumed == spaceConsumed
			
	}	
	
}
