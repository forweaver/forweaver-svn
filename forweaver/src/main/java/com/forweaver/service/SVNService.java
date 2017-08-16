package com.forweaver.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import com.forweaver.domain.Weaver;
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
	@Autowired
	WeaverService weaverService;
	
	public VCFileInfo getFileInfo(String parentDirctoryName, String repositoryName, String commitID, String filePath) {
		System.out.println("*****************************");
		System.out.println("parentDirctoryName: " + parentDirctoryName);
		System.out.println("repositoryName: " + repositoryName);
		System.out.println("commitID: " + commitID);
		System.out.println("filePath: " + filePath);
	
		//사용자 정보 출력(세션)//
		Weaver weaver = weaverService.getCurrentWeaver();
		System.out.println("==================");
		System.out.println("* Session id: " + weaver.getUsername());
		System.out.println("* Session password: " + weaver.getPassword());
		System.out.println("==================");
		
		//프로젝트 초기화//
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		
		try {
			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
	        repository.setAuthenticationManager(authManager);
	        
	        System.out.println("Auth Check Success...");*/
			
			String repoUUID = svnUtil.getRepository().getRepositoryUUID(true).toString();
			String reporevesion = ""+svnUtil.getRepository().getLatestRevision();
			String repoRoot = svnUtil.getRepository().getRepositoryRoot(true).toString();
			String repoURL = svnUtil.getRepository().getLocation().toString();
			
			System.out.println("repoUUID: " + repoUUID);
			System.out.println("reporevesion: " + reporevesion);
			System.out.println("repoRoot: " + repoRoot);
			System.out.println("repoURL: " + repoURL);
			
			//commitID가 Long으로 들어온다는 가정//
			commitID = "-1";
			//svnUtil.isDirectory(commitID, filePath);
			//저장소 리스트를 출력//
			VCFileInfo gitFileInfo = svnUtil.getFileInfo(commitID, filePath);
			
			return gitFileInfo;
			
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		System.out.println("*****************************");
		System.out.println("parentDirctoryName: " + parentDirctoryName);
		System.out.println("repositoryName: " + repositoryName);
		System.out.println("commitID: " + commitID);
		System.out.println("filePath: " + filePath);
	
		//사용자 정보 출력(세션)//
		Weaver weaver = weaverService.getCurrentWeaver();
		System.out.println("==================");
		System.out.println("* Session id: " + weaver.getUsername());
		System.out.println("* Session password: " + weaver.getPassword());
		System.out.println("==================");
		
		//프로젝트 초기화//
		svnUtil.RepoInt(parentDirctoryName, repositoryName);
		
		//파일의 내용을 불러온다.//
		List<VCSimpleFileInfo> svnFileInfoList = svnUtil.getVCFileInfoList(commitID,filePath);
		
		return svnFileInfoList;
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
			List<VCSimpleFileInfo> svnFileInfoList) {
		String readme = "";
		if(svnFileInfoList != null) 
			for(VCSimpleFileInfo svnSimpleFileInfo:svnFileInfoList)// 파일들을 검색해서 리드미 파일을 찾아냄
				if(svnSimpleFileInfo.getName().toUpperCase().contains("README.md"))
					readme = getFileInfo(
							creatorName, 
							projectName, 
							commit, 
							"/"+svnSimpleFileInfo.getName()).getContent();
		
		System.out.println("readme info: " + readme);
		return readme;
	}

}
