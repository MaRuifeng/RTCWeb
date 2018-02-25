package com.ibm.app.test.utils.rtc.workitem.initialization;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.app.test.utils.rtc.RTCClient;
import com.ibm.app.test.utils.rtc.RTCWorkItemInit;
import com.ibm.app.test.utils.rtc.workitem.RTCDefect;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.common.model.IAttachment;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;

/**
 * Customized RTC defect initialization 
 * @author ruifengm
 * @since 2015-Dec-01
 */

public class RTCDefectInit extends RTCWorkItemInit {
	public static final String className = RTCDefectInit.class.getName();
	public static final Logger logger = Logger.getLogger(className);
	
	// defect attributes 
	private IAttribute defectType; 
	private IAttribute fileAgainstComponent; 
	private IAttribute foundInActivity; 
	private IAttribute userImpact; 
	private IAttribute severity; 
	
	// defect content
	private RTCDefect defect;
	
	
	// constructor
	public RTCDefectInit(RTCDefect defect,
			ICategoryHandle filedAgainst, IContributor owner,
			IContributor creator, ArrayList<IContributor> subscribers,
			ArrayList<IAttachment> attachmentLinkList,
			IIterationHandle plannedForSprint,
			IAttribute priority, IWorkItemClient workItemClient, ITeamRepository teamRepository,
			IAttribute defectType, IAttribute fileAgainstComponent, IAttribute foundInActivity,
			IAttribute userImpact, IAttribute severity) {
		super(defect.getSummary(), defect.getDescription(), filedAgainst, owner, creator, subscribers,
				attachmentLinkList, plannedForSprint, defect.getDueDate(), priority, defect.getComment(),
				workItemClient, teamRepository);
		this.defectType = defectType;
		this.fileAgainstComponent = fileAgainstComponent;
		this.foundInActivity = foundInActivity;
		this.userImpact = userImpact;
		this.severity = severity;
		this.defect = defect;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void workItemUniqueExecute(IWorkItem workItem,
			IProgressMonitor monitor) throws TeamRepositoryException {
		String sourceMethod = "workItemUniqueExecute";
		logger.entering(className, sourceMethod);
		// defect-related attributes initialization
		workItem.setValue(this.defectType, RTCClient.getLiteralEqualsString(this.workItemClient, defect.getType(), this.defectType));
		if (this.fileAgainstComponent != null) workItem.setValue(this.fileAgainstComponent, RTCClient.getLiteralEqualsString(this.workItemClient, defect.getFileAgainstComponent(), this.fileAgainstComponent));
		workItem.setValue(this.foundInActivity, RTCClient.getLiteralEqualsString(this.workItemClient, defect.getFoundInActivity(), this.foundInActivity));
		workItem.setValue(this.userImpact, RTCClient.getLiteralEqualsString(this.workItemClient, defect.getUserImpact(), this.userImpact));
		workItem.setValue(this.severity, RTCClient.getLiteralEqualsString(this.workItemClient, defect.getSeverity(), this.severity));
		
		logger.fine(">>>>> Set Defect Type in RTC: " + getStringEqualsLiteral((Identifier)workItem.getValue(this.defectType), this.defectType));
		if (this.fileAgainstComponent != null) logger.fine(">>>>> Set File Against Component in RTC: " + getStringEqualsLiteral((Identifier)workItem.getValue(this.fileAgainstComponent), this.fileAgainstComponent));
		logger.fine(">>>>> Set Found In Activity in RTC: " + getStringEqualsLiteral((Identifier)workItem.getValue(this.foundInActivity), this.foundInActivity));
		logger.fine(">>>>> Set User Impact in RTC: " + getStringEqualsLiteral((Identifier)workItem.getValue(this.userImpact), this.userImpact));
		logger.fine(">>>>> Set Severity in RTC: " + getStringEqualsLiteral((Identifier)workItem.getValue(this.severity), this.severity));

		logger.exiting(className, sourceMethod);
	}
	
	
}
