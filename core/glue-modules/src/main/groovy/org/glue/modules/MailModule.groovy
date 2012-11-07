package org.glue.modules
;

import groovy.util.ConfigObject;
import java.io.PrintWriter;
import javax.mail.*
import javax.mail.internet.*

import org.glue.unit.om.GlueContext;
import org.glue.unit.om.GlueModule;
import org.glue.unit.om.GlueProcess;
import org.glue.unit.om.GlueUnit;

@Typed(TypePolicy.DYNAMIC)
class MailModule implements GlueModule {

	def smtp_port = 25
	def smtp_host= '1.1.1.1';
	def from_email ='glue@yourcompany.com'
	def recipientList=[];
	Boolean debug=false;
	String processFailMessage="Process failed";
	String processStartMessage="Process started";
	String processFinishMessage="Process finished";
	String unitFailMessage="Unit failed";
	String unitStartMessage="Unit started";
	String unitFinishMessage="Unit finished";
	String uiUrl="http://localhost/glue";

	void destroy(){
	}
	
	@Override
	public Map getInfo()
	{
	return 	['smtp_port' : smtp_port,
	'smtp_host': smtp_host,
	'from_email':from_email,
	'recipientList' : recipientList,
	'debug': debug,
	'processFailMessage':processFailMessage,
	'processStartMessage': processStartMessage,
	'processFinishMessag': processFinishMessage,
	'unitFailMessage': unitFailMessage,
	'unitStartMessage':unitStartMessage,
	'unitFinishMessage': unitFinishMessage,
	'uiUrl': uiUrl]
	}
	
	Closure getUnitUrl ={ uiUrl, unitId ->
		return "$uiUrl/$unitId";
	}
	Closure getProcessUrl ={ uiUrl, unitId, processName ->
		return "$uiUrl/$unitId/$processName";
	}

	void onProcessKill(GlueProcess process, GlueContext context){
		
	}
	
	@Override
	public Boolean canProcessRun(GlueProcess process, GlueContext context) {
		return true
	}

	@Override
	public String getName() {
		return 'mail';
	}

	@Typed(TypePolicy.MIXED)
	@Override
	public void init(ConfigObject config) {
		if(config.smtp_port) this.smtp_port=config.smtp_port
		if(config.smtp_host) this.smtp_host=config.smtp_host
		if(config.from_email) this.from_email=config.from_email
		if(config.recipientList) this.recipientList=config.recipientList.toString().split(', *')
		if(config.debug) this.debug=config.debug
		if(config.uiUrl) this.uiUrl=config.uiUrl
		if(config.getProcessUrl) this.getProcessUrl=config.getProcessUrl;
		if(config.getUnitUrl) this.getUnitUrl=config.getUnitUrl;
		println "Recipients: ${this.recipientList.size()}"
		this.recipientList.each { println it}
		
	}

	@Override
	public void onUnitFail(GlueUnit unit, GlueContext context) {
	}

	@Typed(TypePolicy.DYNAMIC)
	@Override
	public void onUnitFinish(GlueUnit unit, GlueContext context) {
	}

	@Typed(TypePolicy.DYNAMIC)
	@Override
	public void onUnitStart(GlueUnit unit, GlueContext context) {
		
	}
	
	@Override
	public void configure(String unitId, ConfigObject config) {
	}
	@Typed(TypePolicy.DYNAMIC)
	@Override
	public void onProcessFail(GlueProcess process, GlueContext context,	Throwable t) {
		if(!context.unitId){
			throw new RuntimeException("The GlueContext must have a unitId associated")
		}

		GlueUnit unit=context.unit;
		//only notify if the notifyOnFail boolean is set to true
		if(unit.notifyOnFail){
			this.mail( recipientList,
					"[glue] Process ${process.getName()} of ${unit.getName()} (${context.unitId}) failed: reason: ${t.getMessage()}",
					"Process ${process.getName()} of ${unit.getName()} (${context.unitId}) failed: \nreason: \n${t.getMessage()}\n$processFailMessage\n${this.getProcessUrl(uiUrl,context.unitId,process.getName())}\n${this.getStackTrace(t)}");
		}
	}
	@Override
	public void onProcessFinish(GlueProcess process, GlueContext context) {

	}
	@Override
	public void onProcessStart(GlueProcess process, GlueContext context) {

	}



	public void mail(String[] recipients, String subject, String body) {


		def props = new Properties()
		props.put('mail.smtp.host', this.smtp_host)
		props.put('mail.smtp.port', this.smtp_port)
		Session session = Session.getDefaultInstance(props, null)
		if(this.debug)
			session.setDebug true;

		// Construct the message
		MimeMessage msg = new MimeMessage(session)
		InternetAddress[] recipientAddresses = new InternetAddress[recipients.length];
		recipients.eachWithIndex { String email, int i ->
			recipientAddresses[i]=new InternetAddress(email)
		}
		println recipientAddresses;
		
		msg.setFrom(new InternetAddress(this.from_email.toString()))
		msg.sentDate = new Date()
		msg.subject = subject
		msg.setRecipients(Message.RecipientType.TO, recipientAddresses)
		//msg.setHeader('Organization', 'mycompany.org')
		msg.setContent(body,'text/plain')

		// Send the message
		Transport.send(msg)
	}

	public String getStackTrace(Throwable t) {
		OutputStream s = new ByteArrayOutputStream();
		PrintWriter ps = new PrintWriter(s);
		t.printStackTrace(ps);
		ps.close();
		return s.toString();
	}
}
