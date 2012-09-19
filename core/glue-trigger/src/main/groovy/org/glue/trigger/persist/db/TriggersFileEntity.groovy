package org.glue.trigger.persist.db

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityManager
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.OneToMany
import javax.persistence.Query
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.UniqueConstraint

/**
 * 
 * Defines a single file that was found by the hdfs trigger.
 * <p/>
 * Unique constraints: unique_name, path<br/>
 * Indexes:<br/>
 * unitNameStatus: unit_name, status<br/>
 *
 */
@Typed
@Entity
@Table(name='trigger_files'
	, uniqueConstraints=[
	@UniqueConstraint(columnNames=['unit_name', 'path']),
 ]
)
@org.hibernate.annotations.Table(
appliesTo='trigger_files',
indexes =  [  @org.hibernate.annotations.Index(name='TriggersFileEntityUnitNameStatus', columnNames=['unit_name', 'status']),
	@org.hibernate.annotations.Index(name='TriggersFileEntityUnitName', columnNames=['unit_name']) ,
	@org.hibernate.annotations.Index(name='TriggersFileEntityUnitNameDate', columnNames=['unit_name', 'date']) ,
	@org.hibernate.annotations.Index(name='TriggersFileEntityDate', columnNames=['date']) ,
	@org.hibernate.annotations.Index(name='TriggersFileEntityUnitNamePath', columnNames=['unit_name', 'path']) ]

)
@NamedQueries(value=[
	@NamedQuery(name='TriggersFileEntity.byUnitNameAndStatus',
	query='from TriggersFileEntity f where f.unitName = :unitName AND f.status = :status ORDER BY f.unitName, f.status'),
	@NamedQuery(name='TriggersFileEntity.byUnitName',
	query='from TriggersFileEntity f where f.unitName = :unitName ORDER BY f.unitName'),
	@NamedQuery(name='TriggersFileEntity.byUnitNameAndDateRange',
	query='from TriggersFileEntity f where f.unitName = :unitName AND f.date <= :dateEnd AND f.date >= :dateStart ORDER BY f.date'),
	@NamedQuery(name='TriggersFileEntity.byUnitDateRange',
	query='from TriggersFileEntity f where f.date <= :dateEnd AND f.date >= :dateStart ORDER BY f.date'),
	@NamedQuery(name='TriggersFileEntity.byUnitNameAndPath',
	query='from TriggersFileEntity f where f.unitName = :unitName AND f.path = :path'),
]
)
class TriggersFileEntity {

	enum STATUS {
		PROCESSED, READY
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id

	@Column(name='unit_name', nullable = false)
	String unitName

	@Enumerated(value = EnumType.STRING)
	@Column(nullable=false)
	STATUS status

	@Column(nullable=false, length=600)
	String path


	@Column(name="ts", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable= false)
	@Temporal(TemporalType.TIMESTAMP)
	Date timeStamp;

	/**
	 * This is the date extracted from the path (not done in this class).<br/>
	 * Should represent the date for the file in the repository.
	 */
	@Column(nullable=false)
	Date date

	/**
	 * A set of running unit execution ids that have or are running using this file.
	 */
	@OneToMany(cascade=CascadeType.ALL)
	List<TriggerFileUnitStatus> unitStatus
	
	public TriggersFileEntity(TriggersFileEntity file){
		id = file.id
		unitName = file.unitName
		status = file.status
		path = file.path
		timeStamp = file.timeStamp
		date = file.date
		unitStatus = file.unitStatus
	}
	
	public TriggersFileEntity(){
		date = new Date()
	}

	public void addFileUnitStatus(String unitId, STATUS status){
		if(!unitStatus){
			unitStatus = new ArrayList<TriggerFileUnitStatus>();
		}
		unitStatus << new TriggerFileUnitStatus(unitId:unitId, status:status); 
	}
	
	/**
	 * Gets a unit status coll that is not lazy fetch
	 * @return
	 */
	public List<TriggerFileUnitStatus> getNonLazyUnitStatusColl(){
		new ArrayList<TriggerFileUnitStatus>(unitStatus)
	}
	
	/**
	 * Returns the results of a query from filtering by date range. The date range is inclusive
	 * @param em EntityManager
	 * @param dateStart Date
	 * @param dateEnd Date
	 * @return Set of TriggersFileEntity
	 */
	static Set<TriggersFileEntity> getByDateRange(EntityManager em, Date dateStart, Date dateEnd){

		Query query = em.createNamedQuery('TriggersFileEntity.byUnitDateRange')
				.setParameter('dateStart', dateStart)
				.setParameter('dateEnd', dateEnd)


		def results = query.getResultList() as Set<TriggersFileEntity>

		return (results && results.size() > 0) ? results : null
	}

	/**
	 * Returns the results of a query from filtering by name and date, the date range is inclusive.
	 * @param em EntityManager
	 * @param unitName
	 * @param dateStart Date
	 * @param dateEnd Date
	 * @return Set of TriggersFileEntity
	 */
	static Set<TriggersFileEntity> getByNameAndDateRange(EntityManager em, String unitName, Date dateStart, Date dateEnd){
		Query query = em.createNamedQuery('TriggersFileEntity.byUnitNameAndDateRange').setParameter('unitName', unitName)
				.setParameter('dateStart', dateStart)
				.setParameter('dateEnd', dateEnd)

		def results = query.getResultList() as Set<TriggersFileEntity> 

		return (results && results.size() > 0) ? results : null
	}

	/**
	 * Returns the results of a query from filtering by name
	 * @param em EntityManager
	 * @param unitName
	 * @return Set of TriggersFileEntity
	 */
	static Set<TriggersFileEntity> getByName(EntityManager em, String unitName){
		Query query = em.createNamedQuery('TriggersFileEntity.byUnitName').setParameter('unitName', unitName)
		def results = query.getResultList() as Set<TriggersFileEntity>

		return (results && results.size() > 0) ? results : null
	}

	/**
	 * Returns the results of a query from filtering by name and status
	 * @param em EntityManager
	 * @param unitName
	 * @param status
	 * @return Set of TriggersFileEntity
	 */
	static Set<TriggersFileEntity> getByNameAndStatus(EntityManager em, String unitName, STATUS status){
		Query query = em.createNamedQuery('TriggersFileEntity.byUnitNameAndStatus').setParameter('unitName', unitName).setParameter('status', status)
		def results = query.getResultList() as Set<TriggersFileEntity>

		return (results && results.size() > 0) ? results : null
	}


	/**
	 * Returns a TriggersFileEntity found by unitName, and path
	 * @param em EntityManager
	 * @param unitName
	 * @param status
	 * @return TriggersFileEntity
	 */
	static TriggersFileEntity getByNameAndPath(EntityManager em, String unitName, String path){
		Query query = em.createNamedQuery('TriggersFileEntity.byUnitNameAndPath').setParameter('unitName', unitName).setParameter('path', path)
		def results = query.getResultList() as Set<TriggersFileEntity>

		return (results && results.size() > 0) ? results.iterator().next() : null
	}
}
