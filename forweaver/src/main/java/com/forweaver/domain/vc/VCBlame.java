package com.forweaver.domain.vc;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import org.eclipse.jgit.revwalk.RevCommit;

/** 버전 관리의 blame 정보를 담기 위한 클래스
 *
 */
public class VCBlame  implements Serializable {

	static final long serialVersionUID = 12224423434L;
	
	private String commitID; //revStr//
	private String userName; //authorStr//
	private String userEmail; //authorStr//
	private String commitTime; //dateStr//
	
	public VCBlame(RevCommit rc) {
		super();
		this.commitID = rc.getName().substring(0,9);
		this.userName = rc.getAuthorIdent().getName();
		this.userEmail = rc.getAuthorIdent().getEmailAddress();
		this.commitTime = new SimpleDateFormat("yy-MM-dd").format(rc.getAuthorIdent().getWhen());
	}
	//SVN Test//
	public VCBlame(String commitID, String userName, String userEmail, String commitTime) {
		super();
		this.commitID = commitID;
		this.userName = userName;
		this.userEmail = userEmail;
		this.commitTime = commitTime;
	} 
	////////////
	public String getCommitID() {
		return commitID;
	}
	public void setCommitID(String commitID) {
		this.commitID = commitID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getCommitTime() {
		return commitTime;
	}
	public void setCommitTime(String commitTime) {
		this.commitTime = commitTime;
	}
	

	
}
