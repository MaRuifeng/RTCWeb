package com.ibm.app.test.utils.rtc;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.client.ILinkManager;
import com.ibm.team.links.common.IItemReference;
import com.ibm.team.links.common.ILink;
import com.ibm.team.links.common.factory.ILinkFactory;
import com.ibm.team.links.common.factory.IReferenceFactory;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IAttachment;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IAttributeHandle;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IComment;
import com.ibm.team.workitem.common.model.IComments;
import com.ibm.team.workitem.common.model.IEnumeration;
import com.ibm.team.workitem.common.model.ILiteral;
import com.ibm.team.workitem.common.model.ISubscriptions;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.model.WorkItemLinkTypes;

/**
 * RTC work item initialization
 * @author ruifengm
 * @since 2015-Dec-01
 */
public abstract class RTCWorkItemInit extends WorkItemOperation {
	
	// work item common attributes 
	protected String summary; 
	protected String description; 
	protected ICategoryHandle filedAgainst; 
	protected IContributor owner; 
	protected IContributor creator; 
	protected ArrayList<IContributor> subscribers; 
	protected ArrayList<IAttachment> attachmentList;
	protected IIterationHandle plannedForSprint; 
	protected Timestamp dueDate; 
	protected IAttribute priority;
	protected String comment;
	
	// work item handlers
	protected ITeamRepository teamRepository; 
	protected IWorkItemClient workItemClient; 

	// constructor
	public RTCWorkItemInit(String summary, String description,
			ICategoryHandle filedAgainst, IContributor owner,
			IContributor creator, ArrayList<IContributor> subscribers,
			ArrayList<IAttachment> attachmentList,
			IIterationHandle plannedForSprint, Timestamp dueDate,
			IAttribute priority, String comment, IWorkItemClient workItemClient, ITeamRepository teamRepository) {
		super("RTC Work Item Initialization");
		this.summary = summary;
		this.description = description;
		this.filedAgainst = filedAgainst;
		this.owner = owner;
		this.creator = creator;
		this.subscribers = subscribers;
		this.attachmentList = attachmentList;
		this.plannedForSprint = plannedForSprint;
		this.dueDate = dueDate;
		this.priority = priority;
		this.workItemClient = workItemClient;
		this.teamRepository = teamRepository;
		this.comment = comment;
	}
	
	/**
	 * Execute work item initialization in RTC 
	 * @author ruifengm
	 * @param WorkItemWorkingCopy, IProgressMonitor
	 */
	@Override
	protected void execute(WorkItemWorkingCopy workingCopy, IProgressMonitor monitor) throws TeamRepositoryException {
		IWorkItem workItem = workingCopy.getWorkItem(); 
		workItem.setHTMLSummary(XMLString.createFromPlainText(this.summary)); 
		workItem.setHTMLDescription(XMLString.createFromPlainText(this.description));
		workItem.setCategory(this.filedAgainst); 
		workItem.setTarget(this.plannedForSprint);
		workItem.setCreator(this.creator);
		workItem.setOwner(this.owner);
		workItem.setDueDate(this.dueDate);
		for (IAttachment att: this.attachmentList){
			att = (IAttachment) att.getWorkingCopy(); 
			att = workItemClient.saveAttachment(att, monitor);
			linkAttachmentToWorkItem(att, workItem, teamRepository, monitor);
		}
		ISubscriptions subs = workItem.getSubscriptions(); 
		for (IContributor sub: this.subscribers){
			subs.add(sub);
		}
		if (this.comment != null && !this.comment.isEmpty()){
			IComments commentList = workItem.getComments(); 
			IComment newComment = commentList.createComment(teamRepository.loggedInContributor(), XMLString.createFromPlainText(comment));
			commentList.append(newComment);
			workingCopy.save(null);
		}
		
		// additional attribute initialization based on work item types
		workItemUniqueExecute(workItem, monitor);
	}
	
	/**
	 * Link attachment reference to work item 
	 * @param attachment
	 * @param workItem
	 * @param teamRepository
	 * @param monitor
	 * @throws TeamRepositoryException
	 */
	protected void linkAttachmentToWorkItem(IAttachment attachment, IWorkItem workItem, ITeamRepository teamRepository, IProgressMonitor monitor) 
			throws TeamRepositoryException {
		ILinkManager linkManager = (ILinkManager) teamRepository.getClientLibrary(ILinkManager.class);
		IItemReference attachmentRef = IReferenceFactory.INSTANCE.createReferenceToItem(attachment.getItemHandle());
		IItemReference workItemRef = IReferenceFactory.INSTANCE.createReferenceToItem(workItem.getItemHandle());
		ILink link = ILinkFactory.INSTANCE.createLink(WorkItemLinkTypes.ATTACHMENT, workItemRef, attachmentRef);
		linkManager.saveLink(link, monitor);
	}
	
	/**
	 * Get string value of the literal by given ID
	 * @param name
	 * @param attHanlde
	 * @return
	 * @throws TeamRepositoryException
	 */
	@SuppressWarnings("rawtypes")
	protected String getStringEqualsLiteral(Identifier literalId, IAttributeHandle attHanlde) throws TeamRepositoryException {
		String name = null;
		IEnumeration enumeration = this.workItemClient.resolveEnumeration(attHanlde, null); 
		List literals = enumeration.getEnumerationLiterals(); 
		for(Iterator iter = literals.iterator(); iter.hasNext();) {
			ILiteral literal = (ILiteral) iter.next(); 
			if (literal.getIdentifier2().equals(literalId)){
				name = literal.getName();
				break; 
			}
		}
		return name;
	}
	
	/**
	 * Full initialization with additional attributes depending on work item types
	 * @param workingCopy
	 * @param monitor
	 */
	protected abstract void workItemUniqueExecute(IWorkItem workItem, IProgressMonitor monitor) throws TeamRepositoryException;
}
