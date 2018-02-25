package com.ibm.app.test.utils.rtc;

/**
 * RTC client
 * Rational Team Concert, Version: 6.0, http://jazz.net
 * @author ruifengm
 * @since 2015-Dec-01
 */

//import java.io.File;
//import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.app.test.utils.RTCClientConstants;
import com.ibm.app.test.utils.rtc.responses.RTCClientMethodResponse;
import com.ibm.app.test.utils.rtc.workitem.RTCDefect;
import com.ibm.app.test.utils.rtc.workitem.initialization.RTCDefectInit;
import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.client.ILinkManager;
import com.ibm.team.links.common.IItemReference;
import com.ibm.team.links.common.ILink;
import com.ibm.team.links.common.ILinkCollection;
import com.ibm.team.links.common.ILinkQueryPage;
//import com.ibm.team.links.common.factory.ILinkFactory;
import com.ibm.team.links.common.factory.IReferenceFactory;
import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IDevelopmentLineHandle;
import com.ibm.team.process.common.IIteration;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.Location;
//import com.ibm.team.repository.common.IItemHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IAuditableClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.IWorkItemWorkingCopyManager;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IAttachment;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IAttributeHandle;
import com.ibm.team.workitem.common.model.ICategory;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IComment;
import com.ibm.team.workitem.common.model.IComments;
import com.ibm.team.workitem.common.model.IEnumeration;
import com.ibm.team.workitem.common.model.ILiteral;
import com.ibm.team.workitem.common.model.IState;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.model.WorkItemLinkTypes;
import com.ibm.team.workitem.common.workflow.IWorkflowAction;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;


public class RTCClient {
	public static final String className = RTCClient.class.getName();
	public static final Logger logger = Logger.getLogger(className);
	
	// RTCLog-in credentials & customer project info
	private String repositoryURI; 
	private String userId;
	private String password;
	private String projectAreaName; 
	private String developmentLineId;
	private ArrayList<String> subscriberList; 
	
	// RTC operational entities
	private ITeamRepository teamRepository;
	private IProjectArea projectArea; 
	private IProcessClientService processClient;
	private IAuditableClient auditableClient;
	private IWorkItemClient workItemClient;

	
	/**
	 * Constructor. It initializes the RTC client by given URL, user id and password
	 */
	public RTCClient(String repositoryURI, String userId, String password,
			String projectAreaName, String developmentLineId, ArrayList<String> subscriberList) {
		super();
		this.repositoryURI = repositoryURI;
		this.userId = userId;
		this.password = password;
		this.projectAreaName = projectAreaName;
		this.developmentLineId = developmentLineId;
		this.subscriberList = subscriberList;
		
		// RTC start up
 		String classPaths = System.getProperty("java.class.path");
		for (String path: classPaths.split(";")) System.out.println(path);
		TeamPlatform.startup();
		
		this.teamRepository = TeamPlatform.getTeamRepositoryService().getTeamRepository(this.repositoryURI);
		this.processClient = (IProcessClientService) teamRepository.getClientLibrary(IProcessClientService.class);
		this.auditableClient = (IAuditableClient) teamRepository.getClientLibrary(IAuditableClient.class);
		this.workItemClient = (IWorkItemClient) teamRepository.getClientLibrary(IWorkItemClient.class);

	}
	
	public ArrayList<String> getSubscriberList() {
		return subscriberList;
	}

	public void setSubscriberList(ArrayList<String> subscriberList) {
		this.subscriberList = subscriberList;
	}

	/**
	 * RTC log in
	 * @throws TeamRepositoryException
	 */
	public void login() throws TeamRepositoryException, Exception {
		String sourceMethod = "login";
		logger.entering(className, sourceMethod);
		logger.fine(">>>>> Logging into the RTC respository " + this.repositoryURI + "..."); 
		teamRepository.registerLoginHandler(new RTCLoginHandler(userId, password) ); 
		teamRepository.login(null);
		if (teamRepository.loggedIn()){
			logger.fine(">>>>> RTC log in successful!"); 
		}
		else throw new Exception("RTC log in failed!"); 
		logger.exiting(className, sourceMethod);
	}
	
	/**
	 * RTC log in check
	 * @throws TeamRepositoryException
	 */
	public boolean loggedIn() throws TeamRepositoryException, Exception {
		String sourceMethod = "loggedInCheck";
		logger.entering(className, sourceMethod);
		logger.fine(">>>>> RTClient logged in? " + teamRepository.loggedIn());
		logger.exiting(className, sourceMethod);
		return teamRepository.loggedIn();
	}
	
	/**
	 * Set up the RTC client. 
	 * E.g. locate the project area
	 * @throws TeamRepositoryException
	 * @throws Exception
	 */
	public void setUp() throws TeamRepositoryException, Exception {
		String sourceMethod = "setUp";
		logger.entering(className, sourceMethod);
		// find project area
		URI projURI = URI.create(projectAreaName.replace(" ", "%20"));
		logger.fine(">>>>> Looking for project area " + projectAreaName + "...");
		projectArea = (IProjectArea) processClient.findProcessArea(projURI, null, null);
		// System.out.println(projURI.toString());
		if (projectArea == null) {
			throw new Exception("Project area '" + projectAreaName + "' not found!"); 
		} 
		else logger.fine(">>>>> Found project area " + projectArea.getName() + "!");
		logger.exiting(className, sourceMethod);
	}
	
	/**
	 * RTC log out
	 * @throws TeamRepositoryException
	 */
	public void logout() throws TeamRepositoryException, Exception {
		String sourceMethod = "logout";
		logger.entering(className, sourceMethod);
		logger.fine(">>>>> Log out from the RTC respository " + this.repositoryURI + "..."); 
		teamRepository.logout();
		if (!teamRepository.loggedIn()){
			logger.fine(">>>>> RTC log out successful!"); 
		}
		else throw new Exception("RTC log out failed!"); 
		logger.exiting(className, sourceMethod);
	}
	
	/**
	 * RTC shut down
	 */
	public void shutdown() {
		String sourceMethod = "shutdown";
		logger.entering(className, sourceMethod);
		logger.fine(">>>>> Shutting down the RTC team platform..."); 
		TeamPlatform.shutdown();
		logger.exiting(className, sourceMethod);
	}
	
	/**
	 * Create a defect
	 * @param RTCDefect
	 * @return RTCClientMethodResponse
	 * @throws TeamRepositoryException
	 * @throws Exception
	 */
	public RTCClientMethodResponse createDefect(RTCDefect defect) throws TeamRepositoryException, Exception{
		String sourceMethod = "createDefect";
		logger.entering(className, sourceMethod);
		
		// find work item type
		IWorkItemType workItemType = workItemClient.findWorkItemType(projectArea, RTCClientConstants.RTC_WORK_ITEM_DEFECT, null);
		if (workItemType == null) {
			throw new Exception("Work item type '" + RTCClientConstants.RTC_WORK_ITEM_DEFECT + "' not found!"); 
		}
		logger.fine(">>>>> Found work item type in RTC: " + workItemType.getDisplayName());
		
		// find filed against
		List<String> categoryPathList = Arrays.asList(defect.getFiledAgainst().split("/"));
		ICategoryHandle filedAgainst = workItemClient.findCategoryByNamePath(projectArea, categoryPathList, null);
		ICategory category = null;
		if (filedAgainst == null) {
			logger.fine("Filed against category '" + defect.getFiledAgainst() + "' not found! Proceed without setting it...");
		} 
		else {		
			//CategoriesManager catMag = CategoriesManager.createInstance(auditableClient, projectArea, null);
			//RTCLog.logVerbose(">>>>> Found filed against category in RTC: " + catMag.findNode(filedAgainst).getName());
			category = (ICategory)teamRepository.itemManager().fetchCompleteItem(filedAgainst, 0, null);
			logger.fine(">>>>> Found filed against category in RTC: " + category.getName());
		}

		
		// add subscribers 
		ArrayList<IContributor> subscribers = new ArrayList<IContributor>();
		IContributor subscriber = null; 
		for (String email: defect.getSubscriberList()) { 
			try {
				subscriber = teamRepository.contributorManager().fetchContributorByUserId(email, null);
			} catch (TeamRepositoryException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
				throw new Exception("Unable to fetch user info of subscriber " + email + "!");
			}
			subscribers.add(subscriber);
			logger.fine(">>>>> Found subscriber in RTC: " + subscriber.getName() + "/" + subscriber.getEmailAddress());
		}
		
		
		// find owner and creator 
		IContributor owner = null; 
		IContributor creator = teamRepository.loggedInContributor(); 
		logger.fine(">>>>> Found item creator in RTC: " + creator.getName() + "/" + creator.getEmailAddress());
		try {
			owner = teamRepository.contributorManager().fetchContributorByUserId(defect.getOwnedBy(), null);
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
			throw new Exception("Unable to fetch info of owner " + defect.getOwnedBy() + "!");
		}
		logger.fine(">>>>> Found item owner in RTC: " + owner.getName() + "/" + owner.getEmailAddress());
		
		// find current iteration(sprint)
		IDevelopmentLineHandle[] developmentLineHandles = projectArea.getDevelopmentLines();
		IIterationHandle plannedForSprint = null; 
		for (IDevelopmentLineHandle lineHandle: developmentLineHandles){
			IDevelopmentLine devLine = (IDevelopmentLine) teamRepository.itemManager().fetchCompleteItem(lineHandle, IItemManager.REFRESH, null); // always fetch from server, do not use cache
			// IDevelopmentLine devLine = auditableClient.resolveAuditable(lineHandle, ItemProfile.DEVELOPMENT_LINE_DEFAULT, null);
//			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> Dev Line: " + devLine.getId());
//			IIteration i = (IIteration)teamRepository.itemManager().fetchCompleteItem(devLine.getCurrentIteration(), 0, null);
//			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> Dev Line: " + i.getName());
			if(devLine.getId().endsWith(developmentLineId)) {
				plannedForSprint = devLine.getCurrentIteration(); 
				break;
			}
		}
		if (plannedForSprint == null) {
			throw new Exception("Unable to find current iteration(sprint)!"); 
		}
		IIteration iteration = (IIteration)teamRepository.itemManager().fetchCompleteItem(plannedForSprint, IItemManager.REFRESH, null);
		logger.fine(">>>>> Found current sprint in RTC: " + iteration.getName());
		
		// get attachments 
		ArrayList<IAttachment> attachmentList = new ArrayList<IAttachment>();
		/*
		 * Disabled the feature to save RTC storage resource and reduce takt time. Jan-13-2016

		IAttachment attachment = null; 
		for (String filePath: defect.getAttachmentLinkSet()){
			try {
				File attachmentFile = new File(filePath); 
				FileInputStream fis = new FileInputStream(attachmentFile); 
				String[] attachmentFilePaths = attachmentFile.getParent().split("\\."); // split on a literal dot
				                                                                      // sample path: com.ibm.bpm.test.restapi.der.mhc.allHost.GETAllHostJUnit
				String attachmentName = attachmentFilePaths[attachmentFilePaths.length-1] +
						"_" + attachmentFile.getName(); //get the last segment and the file name
				attachment = workItemClient.createAttachment(projectArea, attachmentName, "", "text", "UTF-8", fis, null);
			} catch (TeamRepositoryException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
				throw new Exception("Unable to add attachment " + filePath + "!");
			}
			attachmentList.add(attachment);
			logger.fine(">>>>> Uploaded attachment in RTC: " + attachment.getName());
		}
	    */
		
		// set custom attributes 
		IAttribute defectType = workItemClient.findAttribute(projectArea, RTCClientConstants.RTC_DEFECT_TYPE, null);
		IAttribute fileAgainstComponent = workItemClient.findAttribute(projectArea, RTCClientConstants.RTC_DEFECT_FILED_AGAINST_COMP, null); 
	    IAttribute foundInActivity = workItemClient.findAttribute(projectArea, RTCClientConstants.RTC_DEFECT_FOUND_IN_ACTIVITY, null); 
		IAttribute userImpact = workItemClient.findAttribute(projectArea, RTCClientConstants.RTC_DEFECT_USER_IMPACT, null); 
		IAttribute severity = workItemClient.findAttribute(projectArea, RTCClientConstants.RTC_DEFECT_SEVERIY, null); 
		IAttribute priority = workItemClient.findAttribute(projectArea, RTCClientConstants.RTC_DEFECT_PRIORITY, null); 
		
		RTCDefectInit defectInit = new RTCDefectInit(defect, filedAgainst, owner, creator, 
				subscribers, attachmentList, plannedForSprint, priority, workItemClient, teamRepository, 
				defectType, fileAgainstComponent, foundInActivity, userImpact, severity);
		
		// create & save defect
		RTCClientMethodResponse response = new RTCClientMethodResponse(); 
		int defectID = -1; 
		
		logger.fine(">>>>> Saving created defect in RTC...");
		IWorkItemHandle handle = null;
		try {
			handle = defectInit.run(workItemType, null);
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
			throw new Exception("Unable to save the created defect!", e);
		}
		IWorkItem defectItem = auditableClient.resolveAuditable(handle, IWorkItem.FULL_PROFILE, null); 
		defectID = defectItem.getId();
		if (defectID != -1) logger.fine(">>>>> Defect sucessfully saved!");
		
		//logger.fine(">>>>> Set Due Date in RTC:" + defectItem.getDueDate().toString()); 
		//logger.fine(">>>>> Created Defect Summary in RTC:" + defectItem.getHTMLSummary().toString());
		//logger.fine(">>>>> Created Defect Description in RTC:\n" + defectItem.getHTMLDescription().toString());
		
		// get defect status
		IWorkflowInfo workFlowInfo = workItemClient.findWorkflowInfo(defectItem, null);
		String defectStatus = workFlowInfo.getStateName(defectItem.getState2());
		
		// get defect link
		String defectURL = Location.namedLocation(defectItem, teamRepository.getRepositoryURI()).toAbsoluteUri().toString(); 
		
		response.setItemNum(defectID);
		response.setItemSummary(defectItem.getHTMLSummary().toString());
		response.setItemStatus(defectStatus);
		response.setItemLink(defectURL);

		logger.fine(">>>>> Created defect Info:\n" + response.toString());
		logger.exiting(className, sourceMethod);
		return response;
	}
	
	/**
	 * Update a defect with given arguments
	 * @param defectId
	 * @param comment
	 * @param summary
	 * @param description
	 * @param severity
	 * @param attachmentLinks
	 * @return defectJson
	 * @return E.g. {"Defect Summary":"[TEST AUTO API]ReportDashboardRegressionJUnitSuite: pass rate 91.2%", "Defect Status":"New", "Defect ID":145357}
	 * @throws TeamRepositoryException
	 * @throws Exception
	 */
	public RTCClientMethodResponse updateDefect(int defectId, String comment, String summary, String description, 
			String severity, Set<String> attachmentLinks) throws TeamRepositoryException, Exception{
		String sourceMethod = "updateDefect";
		logger.entering(className, sourceMethod);
		
		// find work item by its number
		IWorkItem defectItem = workItemClient.findWorkItemById(defectId, IWorkItem.DEFAULT_PROFILE, null);
		// launch work item working copy manager and connect
		IWorkItemWorkingCopyManager wcm = workItemClient.getWorkItemWorkingCopyManager(); 
		wcm.connect(defectItem, IWorkItem.DEFAULT_PROFILE, null);
		// get work item working copy
		WorkItemWorkingCopy wc = wcm.getWorkingCopy(defectItem);
		
		// Update severity
		logger.fine(">>>>> Updating severity...");
		try {
			IAttribute severityAttr = workItemClient.findAttribute(projectArea, RTCClientConstants.RTC_DEFECT_SEVERIY, null);
			wc.getWorkItem().setValue(severityAttr, RTCClient.getLiteralEqualsString(workItemClient, severity, severityAttr));
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
			throw new Exception("Unable to update severity for defect " + defectId + ".");
		}
		logger.fine(">>>>> Severity updated.");
		
		// Add comment
		logger.fine(">>>>> Adding comment...");
		if (comment != null && !comment.isEmpty()){
			IComments commentList = wc.getWorkItem().getComments(); 
			IComment newComment = commentList.createComment(teamRepository.loggedInContributor(), XMLString.createFromPlainText(comment));
			commentList.append(newComment);
			wc.save(null);
		}
		logger.fine(">>>>> Comment added.");
		
		ILinkManager linkManager = (ILinkManager) teamRepository.getClientLibrary(ILinkManager.class);
		IItemReference workItemRef = IReferenceFactory.INSTANCE.createReferenceToItem(wc.getWorkItem().getItemHandle());
		logger.fine(">>>>> Deleting old attachments...");
		// Delete old attachments 
		try {
			ILinkQueryPage linkQueryPage = linkManager.findLinksBySource(WorkItemLinkTypes.ATTACHMENT, workItemRef, null);
			ILinkCollection currentAttLinks = linkQueryPage.getAllLinksFromHereOn();
			for (ILink link: currentAttLinks){
				//IItemHandle itemHandle = (IItemHandle) link.getTargetRef().resolve();
				//IAttachment currentAtt = (IAttachment) teamRepository.itemManager().fetchCompleteItem(itemHandle, 0, null);
				// remove link
				linkManager.deleteLink(link, null);
				// delete attachment (needs permission)
				// workItemClient.deleteAttachment(currentAtt, null);
			}
		} catch (TeamRepositoryException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
			throw new Exception("Unable to delete old attachments for defect " + defectId + ".");
		}
		logger.fine(">>>> Old attachments deleted.");
		
		/*
		 * Disabled the feature to save RTC storage resource and reduce takt time. Jan-13-2016
		 *
		logger.fine(">>>>> Add new attachments...");
		// Get and add new attachments
		IAttachment attachment = null; 
		for (String filePath: attachmentLinks){
			try {
				// create attachment
				File attachmentFile = new File(filePath); 
				FileInputStream fis = new FileInputStream(attachmentFile); 
				String[] attachmentFilePaths = attachmentFile.getParent().split("\\."); // split on a literal dot
				                                                                      // sample path: com.ibm.bpm.test.restapi.der.mhc.allHost.GETAllHostJUnit
				String attachmentName = attachmentFilePaths[attachmentFilePaths.length-1] +
						"_" + attachmentFile.getName(); //get the last segment and the file name
				attachment = workItemClient.createAttachment(projectArea, attachmentName, "", "text", "UTF-8", fis, null);
				// add attachment link to work item
				IItemReference attachmentRef = IReferenceFactory.INSTANCE.createReferenceToItem(attachment.getItemHandle());
				ILink link = ILinkFactory.INSTANCE.createLink(WorkItemLinkTypes.ATTACHMENT, workItemRef, attachmentRef);
				linkManager.saveLink(link, null);
			} catch (TeamRepositoryException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.logp(Level.SEVERE, className, sourceMethod, sw.toString()); 
				throw new Exception("Unable to add attachment " + filePath + "!");
			}
			logger.fine(">>>>> Added attachment " + attachment.getName() + " for defect " + defectId + ".");
		}
		logger.fine(">>>>> New attachments added.");
		*
		*/
		
		
		logger.fine(">>>>> Updating defect summary & description...");
		// Update defect summary & description 
		wc.getWorkItem().setHTMLSummary(XMLString.createFromPlainText(summary));
		wc.getWorkItem().setHTMLDescription(XMLString.createFromPlainText(description));
		logger.fine(">>>>> Defect summary & description updated.");
		
		// Save defect
		wc.save(null);
		
		// get defect status
		IWorkflowInfo workFlowInfo = workItemClient.findWorkflowInfo(wc.getWorkItem(), null);
		String defectStatus = workFlowInfo.getStateName(defectItem.getState2());
	
		//logger.fine(">>>>> Updated Defect Summary in RTC:" + wc.getWorkItem().getHTMLSummary().toString());
		//logger.fine(">>>>> Updated Defect Description in RTC:\n" + wc.getWorkItem().getHTMLDescription().toString());
		
		// get defect link
		String defectURL = Location.namedLocation(defectItem, teamRepository.getRepositoryURI()).toAbsoluteUri().toString(); 
		
		RTCClientMethodResponse response = new RTCClientMethodResponse();
		response.setItemNum(defectId);
		response.setItemSummary(wc.getWorkItem().getHTMLSummary().toString());
		response.setItemStatus(defectStatus);
		response.setItemLink(defectURL);
		
		wcm.disconnect(defectItem);
		
		logger.fine(">>>>> Updated defect Info:\n" + response.toString());
		logger.exiting(className, sourceMethod);
		return response;
	}
	
	/**
	 * Close a defect by the 'Resolve --> Verfiy' steps with comment if given
	 * @param defectId
	 * @param comment
	 * @return RTCClientMethodResponse
	 * @throws TeamRepositoryException 
	 */
	public RTCClientMethodResponse closeDefect(int defectId, String comment) throws TeamRepositoryException {
		String sourceMethod = "closeDefect";
		logger.entering(className, sourceMethod);
		
		// find work item by its number
		IWorkItem defectItem = workItemClient.findWorkItemById(defectId, IWorkItem.DEFAULT_PROFILE, null);
		// launch work item working copy manager and connect
		IWorkItemWorkingCopyManager wcm = workItemClient.getWorkItemWorkingCopyManager(); 
		wcm.connect(defectItem, IWorkItem.DEFAULT_PROFILE, null);
		// get work item working copy
		WorkItemWorkingCopy wc = wcm.getWorkingCopy(defectItem);
		
		// Add comment
		logger.fine(">>>>> Adding comment...");
		if (comment != null && !comment.isEmpty()){
			IComments commentList = wc.getWorkItem().getComments(); 
			IComment newComment = commentList.createComment(teamRepository.loggedInContributor(), XMLString.createFromPlainText(comment));
			commentList.append(newComment);
			wc.save(null);
		}
		logger.fine(">>>>> Comment added.");
		
		
		String actionId = null; 
		// Resolve
		IWorkflowInfo workFlowInfo = workItemClient.findWorkflowInfo(defectItem, null);
		logger.fine(">>>>> Current Work Item State: " + workFlowInfo.getStateName(wc.getWorkItem().getState2()));
		// logger.fine(">>>>> Current Work Item State: " + workFlowInfo.getStateName(defectItem.getState2()));
		logger.fine(">>>>> Resolving defect...");
		if (workFlowInfo != null){
			// check if already resolved
			if (workFlowInfo.getStateGroup(defectItem.getState2()) == IWorkflowInfo.CLOSED_STATES) {
				logger.warning("<<<<< Defect " + defectId + " is already resloved.");
			}
			// apply Resolve action
			else if (workFlowInfo.getStateGroup(defectItem.getState2()) != IWorkflowInfo.CLOSED_STATES) {
				// get the Resolve action identifier
				Identifier<IWorkflowAction> resolveActId = workFlowInfo.getResolveActionId();
				if (resolveActId != null){
					logger.fine(">>>>> Found resolve action from the work item client: " + resolveActId.getStringIdentifier());
					Identifier<IWorkflowAction> availableActions[] = workFlowInfo.getActionIds(defectItem.getState2());
					// find the Resolve action in the available actions
					for (int i=0; i<availableActions.length; i++){
						logger.fine(">>>>> Found availabe action for the work item Defect: " + availableActions[i].getStringIdentifier());
						if (resolveActId.equals(availableActions[i])) {
							actionId = resolveActId.getStringIdentifier();
							break;
						}
					}
				} else {
					throw new TeamRepositoryException("Unable to find the Resolve action from the work item client!");
				}
				if (actionId == null){
					throw new TeamRepositoryException("Unable to perform " + actionId + " to the work item from its current state!");
				}
				else {
					wc.setWorkflowAction(actionId);
					wc.save(null);
				}
			}
			else {
				throw new TeamRepositoryException("Unknown work item state encounterd!");
			}
		}
		logger.fine(">>>>> Resolution done.");
		logger.fine(">>>>> Current Work Item State: " + workFlowInfo.getStateName(wc.getWorkItem().getState2()));
		
		// Verify
		logger.fine(">>>>> Verifying defect...");
		if (workFlowInfo != null){
			// check if already in resolved state
			if (workFlowInfo.getStateGroup(defectItem.getState2()) == IWorkflowInfo.CLOSED_STATES) {
				// check if already in verified state
				if (workFlowInfo.getStateName(defectItem.getState2()).equalsIgnoreCase(RTCClientConstants.RTC_DEFECT_STATE_VERIFIED)) {
					logger.warning("<<<<< Defect " + defectId + " is already verified.");
				}
				else {
					// apply Verify action
					Identifier<IWorkflowAction> availableActions[] = workFlowInfo.getActionIds(defectItem.getState2());
					// find the verify action in the available actions
					for (int i=0; i<availableActions.length; i++){
						logger.fine(">>>>> Found availabe action for the work item Defect: " + availableActions[i].getStringIdentifier());
						if (availableActions[i].getStringIdentifier().contains(RTCClientConstants.RTC_DEFECT_ACTION_VERIFY.toLowerCase())) {
							actionId = availableActions[i].getStringIdentifier(); 
							break;
						}
					}
					if (actionId == null){
						throw new TeamRepositoryException("Unable to perform " + actionId + " to the work item from its current state!");
					}
					else {
						wc.setWorkflowAction(actionId);
						wc.save(null);
					}
				}
			}
			else {
				throw new TeamRepositoryException("The defect is not in resolved state. It can't be verified.");
			}
		}
		logger.fine(">>>>> Verification done.");
		logger.fine(">>>>> Current Work Item State: " + workFlowInfo.getStateName(defectItem.getState2()));
		wcm.disconnect(defectItem); // this is very important! Otherwise RTC tends to cache the previous copy if consecutive actions are applied to a work item.
		
		// get defect link
		String defectURL = Location.namedLocation(defectItem, teamRepository.getRepositoryURI()).toAbsoluteUri().toString(); 
		
		RTCClientMethodResponse response = new RTCClientMethodResponse(); 
		response.setItemNum(defectId);
		response.setItemStatus(workFlowInfo.getStateName(defectItem.getState2())); 
		response.setItemSummary(wc.getWorkItem().getHTMLSummary().getPlainText());
		response.setItemLink(defectURL);
		logger.exiting(className, sourceMethod);
		return response;
	}
	
	/**
	 * Get work item status 
	 * @param workItemNum
	 * @return RTCClientMethodResponse
	 * @throws TeamRepositoryException
	 * @throws Exception
	 */
	public RTCClientMethodResponse getWorkItemStatus(int workItemNum) throws Exception{
		String sourceMethod = "getWorkItemStatus";
		logger.entering(className, sourceMethod);
		
		// find work item by its number
		IWorkItem workItem = workItemClient.findWorkItemById(workItemNum, IWorkItem.DEFAULT_PROFILE, null);
		// IWorkItem workItem = (IWorkItem) teamRepository.itemManager().fetchCompleteItem((IWorkItemHandle)temp.getItemHandle(), IItemManager.REFRESH, null);
		
		// get work item status
		IWorkflowInfo workFlowInfo = workItemClient.findWorkflowInfo(workItem, null);
		String status = workFlowInfo.getStateName(workItem.getState2());
		
		Identifier<IState>[] stateArr = workFlowInfo.getAllStateIds(); 
		for (Identifier<IState> state: stateArr) {
			logger.fine("#####Available state: " + workFlowInfo.getStateName(state));
		}
	
		logger.fine(">>>>> Found status of work item " + workItemNum + " in RTC:" + status);
		
		// get defect link
		String workItemURL = Location.namedLocation(workItem, teamRepository.getRepositoryURI()).toAbsoluteUri().toString(); 
		
		RTCClientMethodResponse response = new RTCClientMethodResponse();
		response.setItemNum(workItemNum);
		response.setItemStatus(status); 
		response.setItemSummary(workItem.getHTMLSummary().getPlainText());
		response.setItemLink(workItemURL); 
		
		logger.exiting(className, sourceMethod);
		return response;
	}
	
	/**
	 * Get literal ID of the corresponding attribute name given by client
	 * @param name
	 * @param attHanlde
	 * @return
	 * @throws TeamRepositoryException
	 */
	@SuppressWarnings("rawtypes")
	public static Identifier getLiteralEqualsString(IWorkItemClient workItemClient, String name, IAttributeHandle attHanlde) throws TeamRepositoryException {
		Identifier literalId = null; 
		IEnumeration enumeration = workItemClient.resolveEnumeration(attHanlde, null); 
		List literals = enumeration.getEnumerationLiterals(); 
		for(Iterator iter = literals.iterator(); iter.hasNext();) {
			ILiteral literal = (ILiteral) iter.next(); 
			if (literal.getName().equals(name)){
				literalId = literal.getIdentifier2();
				break; 
			}
		}
		return literalId;
	}
}
