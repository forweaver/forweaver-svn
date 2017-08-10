package com.forweaver.util;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import com.forweaver.domain.Project;
import com.forweaver.domain.vc.VCBlame;
import com.forweaver.domain.vc.VCCommitLog;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCSimpleCommitLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;

public class SVNUtil implements VCUtil{
	private String svnPath;
	private String path;

	public SVNUtil(){
		this.svnPath = "/Users/macbook/project/svn/"; //svn의 로컬주소(프로젝트 디폴드 주소) 설정//
	}
	
	public String getSvnPath() {
		return svnPath;
	}

	public void setSvnPath(String svnPath) {
		this.svnPath = svnPath;
	}
	
	/** 프로젝트 초기화 메서드
	 * @param pro
	 */
	public void Init(Project pro) {
		try {
			this.path = svnPath + pro.getName();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public boolean createRepository() {
		try {
			SVNURL tgtURL = SVNRepositoryFactory.createLocalRepository( new File( this.path ), true , false );
			
			System.out.println("repo URL: " + tgtURL);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean deleteRepository() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDirectory(String commitID, String filePath) {
		// TODO Auto-generated method stub
		return false;
	}

	public VCFileInfo getFileInfo(String commitID, String filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public VCSimpleCommitLog getVCCommit(String refName) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCommitListCount(String refName) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<VCSimpleFileInfo> getGitFileInfoList(String commitID, String filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getGitFileList(String commitID) {
		// TODO Auto-generated method stub
		return null;
	}

	public VCCommitLog getCommitLog(String commitID) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VCSimpleCommitLog> getCommitLogList(String branchName, int page, int number) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getBranchList() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getSimpleBranchAndTagNameList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void getProjectZip(String commitName, String format, HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}

	public int[][] getDayAndHour() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VCBlame> getBlame(String filePath, String commitID) {
		// TODO Auto-generated method stub
		return null;
	}

}
