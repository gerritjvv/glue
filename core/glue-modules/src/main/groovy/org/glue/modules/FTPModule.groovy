package org.glue.modules;

import org.apache.commons.io.IOUtils
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.vfs2.FileContent
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.Selectors
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.auth.StaticUserAuthenticator
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder
import org.glue.unit.exceptions.ModuleConfigurationException
import org.glue.unit.om.GlueContext
import org.glue.unit.om.GlueModule
import org.glue.unit.om.GlueProcess
import org.glue.unit.om.GlueUnit

/**
 *
 * 
 */
@Typed(TypePolicy.DYNAMIC)
class FTPModule implements GlueModule{


	private ConfigObject config=null;

	Map<String, ConfigObject> serverConfigurations= [:]
	ConfigObject defaultConfiguration=null

	
	void destroy(){
		shutdown()
	}

	void onProcessKill(GlueProcess process, GlueContext context){
	}

	@Override
	public Map getInfo() {
		return config;
	}

	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true
	}

	public void shutdown(){
	}

	@Override
	public String getName() {
		return 'ftp';
	}
	
	public boolean exists(server = null, pathName){
		return withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj -> fsobj.exists() } )
	}
	
	
	public boolean mkdir(server = null, pathName){
		return withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj -> fsobj.createFolder(); return true } )
	}
	
	public boolean rmdir(server = null, pathName){
		return withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj -> return fsobj.delete(); } )
	}
	
	
	public boolean put(String server = null, String pathName, InputStream local){
		
		return withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj -> 
			FileContent content = fsobj.getContent()
			OutputStream out = content.getOutputStream()
			try{
			IOUtils.copy(local, out)
			}finally{
			   IOUtils.closeQuietly(out)
			}
			
			return true 
		})
		
	}
	
	public boolean put(String server = null, String pathName, String remoteFile){
			return withFTP(server, remoteFile, { FileSystemManager fsman, FileObject fsobj -> 
				fsobj.copyFrom(fsman.resolveFile(pathName), Selectors.SELECT_SELF); true; 
			} )
	}
	
	public boolean get(String server = null, String pathName, String localPath){
		
		return withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj ->
		     FileObject localFile = fsman.resolveFile(localPath)
			 try{
				 localFile.copyFrom(fsobj, Selectors.SELECT_SELF); 
			 }finally{
			   localFile.close()
			 }
			 return true
		} )
		
	}
	
	public void withInputStream(String server = null, String pathName, Runnable closure){
		withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj -> closure.run(fsobj.content.inputStream) } )
	}

	public boolean rename(server = null, from, to){
		return withFTP(server, from, { FileSystemManager fsman, FileObject fsobj -> 
		
			     FileObject toobj = fsobj.resolveFile(to)
				 try{
					 fsobj.moveTo(toobj)
				 }finally{
				   toobj.close()
				 }
				 
				 return true
		} )
	}

	public void withWriter(server = null, pathName, Runnable closure){
		
		withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj ->  
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fsobj.content.outputStream))
			try{
				closure.run(writer)
			}finally{
			  writer.close()
			}
		
			
		})
		
	}
	
	public void withOutputStream(server = null, pathName, Runnable closure){
		
		withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj ->  closure.run(fsobj.content.outputStream) } )
		
	}

	public String[] ls(server = null, pathName = '/'){
		def children = []
		
		withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj -> 
			fsobj.children.each { FileObject child -> children << child.name.pathDecoded} 
		} )
		
		return children as String[]
	}

	public boolean isFile(server, pathName){
		return withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj -> return fsobj.type == FileType.FILE	} )
	}

	public String getParent(server, pathName){
		return withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj -> return fsobj.parent.name.path	} )
	}
	
	public Map<String, Object> fileProperties(server = null, pathName = null){
		return withFTP(server, pathName, { FileSystemManager fsman, FileObject fsobj ->
				FileContent content = fsobj.content
				return [
					'size':content.getSize(),
					'lastModifiedTime':content.lastModifiedTime,
					'attributes':content.attributes
					]
		} )
	}
	
	/**
	 * 
	 * e.g.<br/>
	 * <pre>
	 * className='org.glue.modules.FTPModule'
	 isSingleton=true
	 config{
	 servers{
	 myserver{
	 host='logon url'
	 port=''
	 user='user'
	 pwd='password'
	 }
	 }
	 }
	 * </pre>
	 */
	@Override
	public void init(ConfigObject config) {
		this.config=config;
		if(!config.servers) {
			println "Can't find any clusters in config!"
		}

		config.servers.each { key, v ->

			print "loading server $key"
			if(v.isDefault || !defaultConfiguration) {
				defaultConfiguration=v;
			}

			serverConfigurations[key]=v


			if(!v.host){
				throw new ModuleConfigurationException("A host proprety must be defined for $key in FTPModule")
			}
			if(!v.user){
				throw new ModuleConfigurationException("A user proprety must be defined for $key in FTPModule")
			}
			if(!v.pwd){
				throw new ModuleConfigurationException("A pwd proprety must be defined for $key in FTPModule")
			}
			
		}

		if(defaultConfiguration == null){
			throw new ModuleConfigurationException("No default configuration was specified for the ftp module configuration $config")
		}
	}




	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
	}

	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
	}
	@Override
	public void configure(String unitId, ConfigObject config) {
	}

	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,	Throwable t) {
	}

	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {
	}

	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {
	}

	
	public Object withFTP(String server, String fsPath, Runnable closure){
		ConfigObject conf = (server)? serverConfigurations[server.toString()] : defaultConfiguration
		
		if(conf == null)
			throw new ModuleConfigurationException("No configuration found for server " + server + " in ftp module")
			
		String host = validate(conf.host as String, "host")
		String uid = validate(conf.user as String, "uid")
		String pwd = validate(conf.pwd as String, "pwd")
		
//		long timeout = (conf.timeout) ? timeout = Long.parseLong(conf.timeout as String) : 10000L
		
		FileSystemManager fsManager = VFS.getManager()
		StaticUserAuthenticator auth = new StaticUserAuthenticator(null, uid, pwd)
		FileSystemOptions opts = new FileSystemOptions()
		DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts,	auth);
		
		
		String url = "${host}/${fsPath}"
		Object ret = null
		FileObject fsObj = null;
		try{
		 fsObj = fsManager.resolveFile(url, opts)
		 ret = closure.run(fsManager, fsObj)
		}finally{
		  fsObj?.close()
		}	

		return ret
	}
	
	private static final String validate(String val, String name){
		if(!val){
			throw new ModuleConfigurationException("Property $name must be defined in the ftp module configuration")	
		}

		return val
	}
	
	
}
