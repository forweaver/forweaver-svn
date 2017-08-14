package com.forweaver.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.forweaver.domain.git.statistics.GitParentStatistics;
import com.forweaver.domain.vc.VCCommitLog;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCSimpleCommitLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;
import com.forweaver.util.GitInfo;
import com.forweaver.util.SVNUtil;

public class SVNService implements VCService{
	@Autowired
	SVNUtil svnUtil;
	
	public VCFileInfo getFileInfo(String parentDirctoryName, String repositoryName, String commitID, String filePath) {
		System.out.println("*****************************");
		System.out.println("parentDirctoryName: " + parentDirctoryName);
		System.out.println("repositoryName: " + repositoryName);
		System.out.println("commitID: " + commitID);
		System.out.println("filePath: " + filePath);
		
		String repopath = svnUtil.RepoInit(parentDirctoryName, repositoryName);
		System.out.println("repopath: " + repopath);
		
		//저장소 정보를 출력//
		
		System.out.println("*****************************");
		
		return null;
	}

	public VCFileInfo getFileInfoWithBlame(String parentDirctoryName, String repositoryName, String commitID,
			String filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getBranchList(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean existCommit(String parentDirctoryName, String repositoryName, String commit) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getCommitListCount(String parentDirctoryName, String repositoryName, String commit) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<VCSimpleFileInfo> getVCSimpleFileInfoList(String parentDirctoryName, String repositoryName,
			String commitID, String filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<VCSimpleCommitLog> getVCCommitLogList(String parentDirctoryName, String repositoryName,
			String branchName, int page, int number) {
		// TODO Auto-generated method stub
		return null;
	}

	public VCCommitLog getVCCommitLog(String parentDirctoryName, String repositoryName, String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void getProjectZip(String parentDirctoryName, String repositoryName, String commitName, String format,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		
	}

	public GitParentStatistics loadStatistics(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[][] loadDayAndHour(String parentDirctoryName, String repositoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	public GitInfo getVCInfo(String parentDirctoryName, String repositoryName, String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReadme(String creatorName, String projectName, String commit,
			List<VCSimpleFileInfo> gitFileInfoList) {
		// TODO Auto-generated method stub
		return null;
	}

}
