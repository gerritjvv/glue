package org.glue.trigger.persist.db

import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Query
import javax.persistence.Table

import org.glue.trigger.persist.CheckPoint

/**
 * 
 * Defines the checkpoints for each trigger path
 *
 */
@Entity
@Table(name='trigger_checkpoint')
@org.hibernate.annotations.Table(
appliesTo='trigger_checkpoint',
indexes =  [  @org.hibernate.annotations.Index(name='TriggersCheckPointEntityUnitName', columnNames=['unit_name']),
]
)
@NamedQueries(value=[
	@NamedQuery(name='TriggersCheckPointEntity.byUnitName',
	query='from TriggersCheckPointEntity f where f.id.unitName = :unitName'),
	@NamedQuery(name='TriggersCheckPointEntity.byUnitNameAndPath',
	query='from TriggersCheckPointEntity f where f.id.unitName = :unitName AND f.id.path = :path')
]
)
@Typed
class TriggersCheckPointEntity {
	
	
	@EmbeddedId
	TriggersCheckPointEntityId id
	
	@Column(nullable = false)
	Date checkpoint
	
	Long spaceConsumed = 0L
	Long fileCount = 0L
	Long directoryCount = 0L
	
	String getUnitName(){
		id?.unitName
	}
	String getPath(){
		id?.path
	}
	
	CheckPoint asCheckPoint(){
		
		CheckPoint chk = new CheckPoint()
		chk.setDate( checkpoint )
		chk.setSpaceConsumed( (spaceConsumed == null) ? 0L : spaceConsumed.longValue() )
		chk.setFileCount( (fileCount == null) ? 0L : fileCount.longValue() );
		chk.setDirectoryCount( (directoryCount == null) ? 0L : directoryCount.longValue() );
		
		return chk;
	}
	
	void setCheckPoint(Date chk){
		checkpoint = chk
	}
	
	void setCheckPoint(CheckPoint chk){
		fill(chk)
	}
	
	void fill(CheckPoint chk){
			checkpoint=chk.date
			spaceConsumed= chk.spaceConsumed
			fileCount = chk.fileCount
			directoryCount = chk.directoryCount
	}
	/**
	 * Utility method that gets the checkpoint by unit name
	 * @param em
	 * @param unitName
	 * @return
	 */
	static Collection<TriggersCheckPointEntity> getByUnitName(EntityManager em, String unitName){
		Query query = em.createNamedQuery('TriggersCheckPointEntity.byUnitName').setParameter('unitName', unitName)
		def results = query.getResultList()
		
		
		return (results && results.size() > 0) ? results : null
		
	}
	
	/**
	* Utility method that gets the checkpoint by unit name and path
	* @param em
	* @param unitName
	* @return
	*/
   static TriggersCheckPointEntity getByUnitNameAndPath(EntityManager em, String unitName, String path){
	   Query query = em.createNamedQuery('TriggersCheckPointEntity.byUnitNameAndPath').setParameter('unitName', unitName)
	   .setParameter('path', path)
	   
	   def results = query.getResultList()
	   
	   return (results && results.size() > 0) ? results[0] : null
	   
   }
   
   /**
   * Utility method that removes the checkpoint by unit name and path
   * @param em
   * @param unitName
   * @param path
   * @return
   */
  static void removeByNameAndPath(EntityManager em, String unitName, String path){
	  Query query = em.createNamedQuery('TriggersCheckPointEntity.byUnitNameAndPath').setParameter('unitName', unitName)
	  .setParameter('path', path)
	  
	  def results = query.getResultList()
	  if(results && results.size() > 0){
		  em.remove(results[0])
	  }
	  
  }
	
}
