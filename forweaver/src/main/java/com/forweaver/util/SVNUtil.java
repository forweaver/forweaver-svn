package com.forweaver.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.gitective.core.BlobUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.forweaver.domain.Project;
import com.forweaver.domain.vc.VCBlame;
import com.forweaver.domain.vc.VCCommitLog;
import com.forweaver.domain.vc.VCFileInfo;
import com.forweaver.domain.vc.VCSimpleCommitLog;
import com.forweaver.domain.vc.VCSimpleFileInfo;

public class SVNUtil implements VCUtil{
	private String svnPath;
	private String svnreporootPath;
	private String path;
	private SVNRepository repository;

	public SVNUtil(){
		this.svnPath = "/Users/macbook/project/svn/"; //svn의 로컬주소(프로젝트 디폴드 주소) 설정//
		this.svnreporootPath = "file:///Users/macbook/project/svn/"; //svn의 저장소 주소//
	}
	
	public String getSvnPath() {
		return svnPath;
	}

	public String getSvnrepoPath() {
		return svnreporootPath;
	}

	public SVNRepository getRepository() {
		return repository;
	}

	/** 프로젝트 초기화 메서드
	 * @param pro
	 */
	public void Init(Project pro) {
		try {
			this.path = svnPath + pro.getName();
			System.out.println("path: " + this.path);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	/** 프로젝트 저장소 정보 설정(Repository Load) 메서드
	 * 
	 * @param parentDirctoryName
	 * @param repositoryName
	 * @return
	 */
	public void RepoInt(String parentDirctoryName, String repositoryName) {
		try {
			SVNRepository repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded(this.svnreporootPath+"/"+parentDirctoryName+"/"+repositoryName));
			this.repository = repository;
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
		try {
			FileUtils.deleteDirectory(new File(this.path)); //파일제거//
		}catch(Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	/** 파일주소와 커밋아이디(Revesion)를 바탕으로 디렉토리인지 검사함.
	 * @param commitID
	 * @param filePath
	 * @return
	 */
	public boolean isDirectory(String commitID, String filePath) {
		System.out.println("*****************************");
		System.out.println("commitID: " + commitID);
		System.out.println("filePath: " + filePath);
		System.out.println("path: " + this.path);
		
		try
        {
        	Collection entries = this.repository.getDir(filePath, Long.parseLong(commitID), null, (Collection) null);
        	
            Iterator iterator = entries.iterator();
            
            int repptreecount = 0;
            
        	while (iterator.hasNext()) {
                SVNDirEntry entry = (SVNDirEntry) iterator.next();
                
                System.out.println("entry kind: " + entry.getKind().toString());
                System.out.println("entry name: " + entry.getName().toString());
                System.out.println("entry author: " + entry.getAuthor().toString());
                System.out.println("entry revesion: " + entry.getRevision());
                System.out.println("entry date: " + entry.getDate().toString());
                System.out.println("entry lock: " + entry.getLock());
                System.out.println("entry relative path: " + entry.getRelativePath().toString());
                
                //디렉터리인지 파일인지 구분//
                if(entry.getKind().toString().equals("dir")){
                	System.out.println("Directory? [YES]");
                	
                	return true;
                } else{
                	System.out.println("Directory? [NO]");
                	
                	return false;
                }
            }
        } catch(SVNException e){
        	e.printStackTrace();
        	
        	return false;
        }
		
		System.out.println("*****************************");
		
		return false;
	}

	/** 프로젝트의 파일 정보를 가져옴
	 * @param commitID
	 * @param filePath
	 * @return
	 */
	@SuppressWarnings("finally")
	public VCFileInfo getFileInfo(String commitID, String filePath) {
		System.out.println("*****************************");
		System.out.println("commitID: " + commitID);
		System.out.println("filePath: " + filePath);
		System.out.println("path: " + this.path);
		
		//파일내용, 커밋로그 2가를 call//
		List<VCSimpleCommitLog> commitLogList = new ArrayList<VCSimpleCommitLog>();
		
		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = 0;
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision
		
		try {
			logEntries = this.repository.log(new String[] { filePath }, null, selectCommitIndex, endRevesion, true, true);
			
			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				
				//repotreelist_commitmessage.add(logEntry.getMessage());
				commitLogList.add(new VCSimpleCommitLog(
						""+logEntry.getRevision(),
						logEntry.getMessage(),
						logEntry.getAuthor(),
						"not email svn",
						logEntry.getDate()));
			}
			
			for(;selectCommitIndex<commitLogList.size();selectCommitIndex++)
				if(commitLogList.get(selectCommitIndex).getCommitLogID().equals(endRevesion))
					break;
			
			
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		} finally{	
			System.out.println("<<Log Info>>");
			System.out.println("log count: " + commitLogList.size());
			
			//revesion 0은 생략//
			for(int i=1; i<commitLogList.size(); i++){
				System.out.println("["+i+"] revesion: " + commitLogList.get(i).getCommitLogID());
				System.out.println("["+i+"] message: " + commitLogList.get(i).getShortMassage());
				System.out.println("["+i+"] author: " + commitLogList.get(i).getCommiterName());
				System.out.println("["+i+"] email: " + commitLogList.get(i).getCommiterEmail());
				System.out.println("["+i+"] date: " + commitLogList.get(i).getCommitDate());
				System.out.println("--");
			}
			
			System.out.println("<><><><><><><><><>");
			
			//파일의 내용을 불러온다.(String, byte[])//
			String stringContent = doPrintFileStringcontent(filePath);
			byte[] byteContent = doPrintFileBytecontent(filePath);
			
			System.out.println("*****************************");
			
			return new VCFileInfo(filePath, stringContent, byteContent,
					commitLogList, selectCommitIndex,isDirectory(commitID,filePath));
		}
	}
	
	/** 파일내용 String으로 출력
	 * 
	 * @param filename
	 * @return
	 */
	public String doPrintFileStringcontent(String filename){
		String filecontent = "";
		
		System.out.println("file content view");
		
		System.out.println("file name: " + filename);
		System.out.println("local path: " + this.path);
		
		SVNRepository repository = null;
		
		try {
			repository = this.repository;
			
			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
			repository.setAuthenticationManager(authManager);*/
			
			SVNNodeKind nodeKind = repository.checkPath(filename, -1);

			System.out.println("repo check ok...");
			System.out.println("nodeKind: " + nodeKind);
			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.FILE) {
				SVNProperties fileProperties = new SVNProperties();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
				repository.getFile(filename, -1, fileProperties, baos);
				
				System.out.println("file view ok...");
				
				String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);
				boolean isTextType = SVNProperty.isTextMimeType(mimeType);

				Iterator iterator = fileProperties.nameSet().iterator();
				
				while (iterator.hasNext()) {
					String propertyName = (String) iterator.next();
					String propertyValue = fileProperties.getStringValue(propertyName);
					System.out.println("File property: " + propertyName + "=" + propertyValue);
				}

				if (isTextType) {
					System.out.println("File contents:");
					filecontent = baos.toString();
	
					System.out.println(filecontent);
					
					return filecontent;
				} else {
					System.out.println("Not a text file.");
					
					return filecontent;
				}
			} else if (nodeKind == SVNNodeKind.DIR) {
				System.out.println("is Directory");
				
				return filecontent;
			}
			
			
		} catch (SVNException e) {
			e.printStackTrace();
			
			return filecontent;
		}
		
		return filecontent;
	}
	
	/** 파일내용 byte[]로 출력
	 * 
	 * @param filename
	 * @return
	 */
	public byte[] doPrintFileBytecontent(String filename){
		byte[] content = null;
		System.out.println("file content view");
		
		System.out.println("file name: " + filename);
		System.out.println("local path: " + this.path);
		
		SVNRepository repository = null;
		
		try {
			repository = this.repository;
			
			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
			repository.setAuthenticationManager(authManager);*/
			
			SVNNodeKind nodeKind = repository.checkPath(filename, -1);

			System.out.println("repo check ok...");
			System.out.println("nodeKind: " + nodeKind);
			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.FILE) {
				SVNProperties fileProperties = new SVNProperties();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
				repository.getFile(filename, -1, fileProperties, baos);
				
				System.out.println("file view ok...");
				
				String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);
				boolean isTextType = SVNProperty.isTextMimeType(mimeType);

				Iterator iterator = fileProperties.nameSet().iterator();
				
				while (iterator.hasNext()) {
					String propertyName = (String) iterator.next();
					String propertyValue = fileProperties.getStringValue(propertyName);
					System.out.println("File property: " + propertyName + "=" + propertyValue);
				}

				if (isTextType) {
					System.out.println("File contents:");
					content = baos.toByteArray();
	
					System.out.println(content);
					
					return content;
				} else {
					System.out.println("Not a text file.");
					
					return content;
				}
			} else if (nodeKind == SVNNodeKind.DIR) {
				System.out.println("is Directory");
				
				return content;
			}
			
			
		} catch (SVNException e) {
			e.printStackTrace();
			
			return content;
		}
		
		return content;
	}

	public VCSimpleCommitLog getVCCommit(String refName) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCommitListCount(String commitID) {
		System.out.println("*****************************");
		System.out.println("commitID: " + commitID);
		System.out.println("path: " + this.path);
		
		int logcount = 0;
		
		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = 0;
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision
				
		try {
			logEntries = this.repository.log(new String[] { "" }, null, selectCommitIndex, endRevesion, true, true);
					
			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				logcount++;
			}
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("==> log count: " + logcount);
		
		return logcount;
	}

	// 디렉터리일 경우 정보 리스트
	public List<VCSimpleFileInfo> getVCFileInfoList(String commitID, String filePath) {
		List<VCSimpleFileInfo> svnFileInfoList = new ArrayList<VCSimpleFileInfo>();
	
		SVNRepository repository = this.repository;
		
		try {
			svnFileInfoList = listEntries(repository, filePath, commitID); //파일내의 정보를 불러온다.//
			
			System.out.println("list size: " + svnFileInfoList.size());
			
			for(int i=0; i<svnFileInfoList.size(); i++){
				System.out.println("info: " + svnFileInfoList.get(i).getName());
			}
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return svnFileInfoList;
	}
	
	//파일 리스트 정보를 가져온다.//
	public List<VCSimpleFileInfo> listEntries(SVNRepository repository, String path, String commitID) throws SVNException {
		List<VCSimpleFileInfo> svnFileInfoList = new ArrayList<VCSimpleFileInfo>();
		
        try
        {
        	Collection entries = repository.getDir(path, -1, null, (Collection) null);
        	
            Iterator iterator = entries.iterator();
            
        	while (iterator.hasNext()) {
                SVNDirEntry entry = (SVNDirEntry) iterator.next();
                
                //디렉터리 출력 형식에 맞게 가져온다.//
                VCSimpleFileInfo svnFileInfo = new VCSimpleFileInfo(
                		entry.getName(), path,
						isDirectory(commitID,path),
						""+entry.getRevision(), entry.getCommitMessage().toString(),
						entry.getDate(),
						entry.getAuthor(),
						"svn not email");
                
                svnFileInfoList.add(svnFileInfo);
            }
        	
        	return svnFileInfoList;
        } catch(SVNException e){
        	e.printStackTrace();
        }
        
        return svnFileInfoList;
    }

	public List<String> getVCFileList(String commitID) {
		// TODO Auto-generated method stub
		return null;
	}

	public VCCommitLog getCommitLog(String commitID) {
		VCCommitLog svnCommitLog = null;
		String diffStr = new String();
		
		String revesion = "";
		String commitMessage = "";
		String fullMessage = "";
		String commiterName = "";
		String commiterEmail = "svn no email";
		Date commitdate = null;
		
		//diff와 로그정보를 가져온다.//
		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = Integer.parseInt(commitID);
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision
		
		try{
			logEntries = this.repository.log(new String[] { "" }, null, selectCommitIndex, endRevesion, true, true);
			
			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				
				revesion = ""+logEntry.getRevision();
				commitMessage = logEntry.getMessage();
				fullMessage = logEntry.getMessage();
				commiterName = logEntry.getAuthor();
				commitdate = logEntry.getDate();
			}
			
			//diff정보를 가져온다.(선택된 커밋과 하나 이전 커밋과의 비교)//
			diffStr = doDiff(selectCommitIndex-1, selectCommitIndex);
	
			System.out.println("--Diff result--");
			System.out.println(diffStr);
			
			//로그객체를 생성//
			svnCommitLog = new VCCommitLog(revesion,
					commitMessage, fullMessage, commiterName, commiterEmail,
					diffStr, null,commitdate);
			
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return svnCommitLog;
	}
	
	/** SVN Diff 수행
	 * 
	 * @param revesionone
	 * @param revesiontwo
	 * @return
	 */
	public String doDiff(long revesionone, long revesiontwo){
		System.out.println("revesionone: " + revesionone + " / revesiontwo: " + revesiontwo);
		String diffresult = null;
		SVNRepository repository = null;
		
		try {
			repository = this.getRepository(); //저장소를 불러온다.//
			
			System.out.println("==> repo root URL: " + repository.getRepositoryRoot(false)); //저장소의 루트 경로를 불러온다.//
			
			SVNURL svnURL = repository.getRepositoryRoot(false);
			
			System.out.println("repo setting succcess...");

			// Get diffClient.
		    SVNClientManager clientManager = SVNClientManager.newInstance();
		    SVNDiffClient diffClient = clientManager.getDiffClient();
		    
		    // Using diffClient, write the changes by commitId into
		    // byteArrayOutputStream, as unified format.
		    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		    diffClient.doDiff(svnURL, null, SVNRevision.create(revesionone), SVNRevision.create(revesiontwo), SVNDepth.INFINITY, true, byteArrayOutputStream);
		    //diffClient.doDiff(new File(repourl), SVNRevision.UNDEFINED, SVNRevision.create(revesionone), SVNRevision.create(revesiontwo), true, true, byteArrayOutputStream);
		    diffresult = byteArrayOutputStream.toString();
		    
	        return diffresult;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		return diffresult;
	}

	public List<VCSimpleCommitLog> getCommitLogList(String commitID, int page, int number) {
		//파일내용, 커밋로그 2가를 call//
		List<VCSimpleCommitLog> svncommitLogList = new ArrayList<VCSimpleCommitLog>();
		//page카운트 변수//
		int pageCount = 0;
				
		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = 0;
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision
		
		try{
			//페이징 처리//
			//1페이지인 경우는 그대로 간다.//
			if(page == 1){
				System.out.println("==> page : " + page);
				System.out.println("==> number: " + number);
				
				logEntries = this.repository.log(new String[] { "" }, null, selectCommitIndex, endRevesion, true, true);
				
				for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
					//System.out.println("pageCount: " + pageCount);
					
					if(pageCount > number){
						break;
					}
					
					SVNLogEntry logEntry = (SVNLogEntry) entries.next();
							
					//repotreelist_commitmessage.add(logEntry.getMessage());
					svncommitLogList.add(new VCSimpleCommitLog(
							""+logEntry.getRevision(),
							logEntry.getMessage(),
							logEntry.getAuthor(),
							"not email svn",
							logEntry.getDate()));
					
					
					pageCount++;
				}
			} else if(page > 1){
				//1페이지 이상부터는 루프의 범위가 달라진다.//
				int startNumber = number+1;
				int endNumber = page * number;
			
				System.out.println("==> page : " + startNumber);
				System.out.println("==> number: " + endNumber);
				
				logEntries = this.repository.log(new String[] { "" }, null, startNumber, endRevesion, true, true);
				
				for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
					//해당 범위에 들어왔을 때 로그를 추출//
					if(pageCount > startNumber){
						//System.out.println("pageCount: " + pageCount);
						
						SVNLogEntry logEntry = (SVNLogEntry) entries.next();
						
						//repotreelist_commitmessage.add(logEntry.getMessage());
						svncommitLogList.add(new VCSimpleCommitLog(
								""+logEntry.getRevision(),
								logEntry.getMessage(),
								logEntry.getAuthor(),
								"not email svn",
								logEntry.getDate()));
					} if(pageCount > endNumber){
						System.out.println("break log extract");
						break;
					}
					
					pageCount++;
				}
			}
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		pageCount = 0;
		
		//정렬순서를 내림차순으로 변경//
		Descending descending = new Descending();
		Collections.sort(svncommitLogList, descending);

		return svncommitLogList;
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
