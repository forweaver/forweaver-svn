package com.forweaver.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forweaver.domain.git.statistics.GitParentStatistics;
import com.forweaver.domain.vc.VCCommitLog;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCSimpleCommitLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;
import com.forweaver.util.GitInfo;
import com.forweaver.util.GitUtil;

@Service
public class GitService {

	@Autowired private GitUtil gitUtil;

	public VCFileInfo getFileInfo(String parentDirctoryName,String repositoryName,
			String commitID,String filePath){
		gitUtil.Init(parentDirctoryName,repositoryName);
		if(filePath.equals("/") || !filePath.startsWith("/"))
			return null;
		else
			filePath = filePath.substring(1);

		VCFileInfo gitFileInfo = gitUtil.getFileInfo(commitID, filePath);
		
		return gitFileInfo;
	}

	public VCFileInfo getFileInfoWithBlame(String parentDirctoryName,String repositoryName,
			String commitID,String filePath){
		gitUtil.Init(parentDirctoryName,repositoryName);
		if(filePath.equals("/") || !filePath.startsWith("/"))
			return null;
		else
			filePath = filePath.substring(1);

		VCFileInfo gitFileInfo = gitUtil.getFileInfo(commitID, filePath);
		if(!gitFileInfo.isDirectory())
			gitFileInfo.setBlames(gitUtil.getBlame(filePath, commitID));
		return gitFileInfo;
	}

	public List<String> getBranchList(String parentDirctoryName,
			String repositoryName){
		gitUtil.Init(parentDirctoryName,repositoryName);
		List<String> branchList = gitUtil.getSimpleBranchAndTagNameList();
		return branchList;
	}

	public boolean existCommit(String parentDirctoryName,
			String repositoryName,String commit){
		gitUtil.Init(parentDirctoryName,repositoryName);
		try{
			if(gitUtil.getVCCommit(commit) == null)
				return false;
			else
				return true;
		}catch(Exception e){
			return false;
		}
	}

	public int getCommitListCount(String parentDirctoryName,
			String repositoryName,String commit){
		gitUtil.Init(parentDirctoryName,repositoryName);
		return gitUtil.getCommitListCount(commit);
	}

	public List<VCSimpleFileInfo> getGitSimpleFileInfoList(String parentDirctoryName,
			String repositoryName,String commitID,String filePath) {
		gitUtil.Init(parentDirctoryName,repositoryName);

		if(filePath.equals("/") || !filePath.startsWith("/"))
			filePath = "";
		else
			filePath = filePath.substring(1);

		List<VCSimpleFileInfo> gitFileInfoList = gitUtil.getVCFileInfoList(commitID,filePath);
		return gitFileInfoList;
	}

	public List<VCSimpleCommitLog> getGitCommitLogList(String parentDirctoryName,
			String repositoryName,String branchName,int page,int number) {	
		gitUtil.Init(parentDirctoryName,repositoryName);
		List<VCSimpleCommitLog> gitCommitLogList = gitUtil.getCommitLogList(branchName,page,number);
		return gitCommitLogList;
	}


	public VCCommitLog getGitCommitLog(String parentDirctoryName,
			String repositoryName,String branchName) {
		gitUtil.Init(parentDirctoryName,repositoryName);
		VCCommitLog gitCommitLog = gitUtil.getCommitLog(branchName);
		return gitCommitLog;

	}


	public void getProjectZip(String parentDirctoryName,
			String repositoryName,String commitName,String format,HttpServletResponse response){
		gitUtil.Init(parentDirctoryName,repositoryName);
		gitUtil.getProjectZip(commitName,format,response);
	}


	public GitParentStatistics loadStatistics(String parentDirctoryName,
			String repositoryName){
		gitUtil.Init(parentDirctoryName,repositoryName);
		return gitUtil.getCommitStatistics();
	}

	public int[][] loadDayAndHour(String parentDirctoryName,
			String repositoryName){
		gitUtil.Init(parentDirctoryName,repositoryName);	
		return gitUtil.getDayAndHour();
	}

	public GitInfo getGitInfo(String parentDirctoryName,
			String repositoryName,String branchName){
		gitUtil.Init(parentDirctoryName,repositoryName);	
		return gitUtil.getGitInfo(branchName);
	}

	public String getReadme(String creatorName,String projectName,String commit,List<VCSimpleFileInfo> gitFileInfoList){
		String readme = "";
		if(gitFileInfoList != null) 
			for(VCSimpleFileInfo gitSimpleFileInfo:gitFileInfoList)// 파일들을 검색해서 리드미 파일을 찾아냄
				if(gitSimpleFileInfo.getName().toUpperCase().contains("README.MD"))
					readme = getFileInfo(
							creatorName, 
							projectName, 
							commit, 
							"/"+gitSimpleFileInfo.getName()).getContent();
		return readme;
	}
	

}
