package com.forweaver.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

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
	
	@Autowired
	AnnotationHandler annotationhandler;

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
		System.out.println("--<Directory Check>--");
		System.out.println("==> filepath: " + filePath);
		System.out.println("==> commitID: " + commitID);
		
		//해당 filepath만 검증//
		SVNDirEntry dirEntry=null;
		
		try {
		    dirEntry=this.repository.info(filePath,Long.parseLong(commitID));
		    
		    if(dirEntry.getKind().toString().equals("dir")){
            	System.out.println("[true directory]");
            	System.out.println("---------------------");
            	return true;
            } else{
            	System.out.println("[false directory]");
            	System.out.println("---------------------");
            	return false;
            }
		} catch (  SVNException e) {
			e.printStackTrace();
			
			return false;
		}
	}

	/** 프로젝트의 파일 정보를 가져옴
	 * @param commitID
	 * @param filePath
	 * @return
	 */
	@SuppressWarnings("finally")
	public VCFileInfo getFileInfo(String commitID, String filePath) {
		System.out.println("--<File Info>--");
		System.out.println("==> filepath: " + filePath);
		System.out.println("==> commit: " + commitID);
		
		//파일내용, 커밋로그 2개를 call//
		List<VCSimpleCommitLog> commitLogList = new ArrayList<VCSimpleCommitLog>();
		
		//저장소의 로그기록을 가져온다.//
		Collection logEntries = null;

		int selectCommitIndex = 0;
		int endRevesion = Integer.parseInt(commitID); //HEAD (the latest) revision
		
		try {
			logEntries = this.repository.log(new String[] { filePath }, null, selectCommitIndex, endRevesion, true, true);
			
			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				
				commitLogList.add(new VCSimpleCommitLog(
						""+logEntry.getRevision(),
						logEntry.getMessage(),
						logEntry.getAuthor(),
						"not email svn",
						logEntry.getDate()));
			}
			
			//해당 파일에 대해서 로그가 일치하는 부분에서 종료//
			for(;selectCommitIndex<commitLogList.size();selectCommitIndex++)
				if(commitLogList.get(selectCommitIndex).getCommitLogID().equals(endRevesion))
					break;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		} finally{	
			//파일의 내용을 불러온다.(String, byte[])//
			String stringContent = doPrintFileStringcontent(filePath);
			byte[] byteContent = doPrintFileBytecontent(filePath);
			
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
		System.out.println("--<file content>--");
		System.out.println("==> filename: " + filename);
		
		String filecontent = "";
		
		SVNRepository repository = null;
		
		try {
			repository = this.repository;
			
			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
			repository.setAuthenticationManager(authManager);*/
			
			SVNNodeKind nodeKind = repository.checkPath(filename, -1);

			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.FILE) {
				SVNProperties fileProperties = new SVNProperties();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
				repository.getFile(filename, -1, fileProperties, baos);
				
				String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);
				boolean isTextType = SVNProperty.isTextMimeType(mimeType);

				Iterator iterator = fileProperties.nameSet().iterator();
				
				while (iterator.hasNext()) {
					String propertyName = (String) iterator.next();
					String propertyValue = fileProperties.getStringValue(propertyName);
				}

				if (isTextType) {
					System.out.println("==> "+filename +" File contents:");
					
					filecontent = baos.toString();
	
					System.out.println(filecontent);
					System.out.println("------------------");
					return filecontent;
				} else {
					System.out.println(filename + " Not a text file.");
					System.out.println("------------------");
					return filecontent;
				}
			} else if (nodeKind == SVNNodeKind.DIR) {
				System.out.println(filename + " is Directory");
				System.out.println("------------------");
				return filecontent;
			}
			
			
		} catch (SVNException e) {
			e.printStackTrace();
			System.out.println("------------------");
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
		System.out.println("--<file content>--");
		System.out.println("==> filename: " + filename);
		
		byte[] content = null;
		
		SVNRepository repository = null;
		
		try {
			repository = this.repository;
			
			/*//인증정보를 설정//
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userid, userpassword);
			repository.setAuthenticationManager(authManager);*/
			
			SVNNodeKind nodeKind = repository.checkPath(filename, -1);

			if (nodeKind == SVNNodeKind.NONE || nodeKind == SVNNodeKind.FILE) {
				SVNProperties fileProperties = new SVNProperties();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
				repository.getFile(filename, -1, fileProperties, baos);
				
				String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);
				boolean isTextType = SVNProperty.isTextMimeType(mimeType);

				Iterator iterator = fileProperties.nameSet().iterator();
				
				while (iterator.hasNext()) {
					String propertyName = (String) iterator.next();
					String propertyValue = fileProperties.getStringValue(propertyName);
				}

				if (isTextType) {
					System.out.println("==> "+filename +" File contents:");
					content = baos.toByteArray();
	
					System.out.println(content);
					
					return content;
				} else {
					System.out.println(filename + " Not a text file.");
					System.out.println("------------------");
					return content;
				}
			} else if (nodeKind == SVNNodeKind.DIR) {
				System.out.println(filename + " is Directory");
				System.out.println("------------------");
				return content;
			}
			
			
		} catch (SVNException e) {
			e.printStackTrace();
			System.out.println("------------------");
			return content;
		}
		
		return content;
	}

	public VCSimpleCommitLog getVCCommit(String refName) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCommitListCount(String commitID) {
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
		
		System.out.println("==> fileinfo list path: " + filePath);
		
		try {
			svnFileInfoList = listEntries(repository, filePath, commitID); //파일내의 정보를 불러온다.//
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return svnFileInfoList;
	}
	
	//파일 리스트 정보를 가져온다.//
	private List<VCSimpleFileInfo> listEntries(SVNRepository repository, String path, String commitID) throws SVNException {
		List<VCSimpleFileInfo> svnFileInfoList = new ArrayList<VCSimpleFileInfo>();
		
		System.out.println("--<listEntries>--");
		System.out.println("path: " + path);
		
        try
        {
        	Collection entries = repository.getDir(path, -1, null, (Collection) null);
        	
            Iterator iterator = entries.iterator();
            
        	while (iterator.hasNext()) {
                SVNDirEntry entry = (SVNDirEntry) iterator.next();
                
                //디렉터리 출력 형식에 맞게 가져온다.//
                VCSimpleFileInfo svnFileInfo = new VCSimpleFileInfo(
                		entry.getName(), path+"/"+entry.getName(),
						isDirectory(commitID,path),
						""+entry.getRevision(), entry.getCommitMessage().toString(),
						entry.getDate(),
						entry.getAuthor(),
						"svn not email");
                
                svnFileInfoList.add(svnFileInfo);
            }
        	
        	System.out.println("------------------");
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
	
			System.out.println("==> Diff result:");
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
		String diffresult = null;
		SVNRepository repository = null;
		
		try {
			repository = this.getRepository(); //저장소를 불러온다.//
			
			SVNURL svnURL = repository.getRepositoryRoot(false);

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
		System.out.println("===== Blame set...");
		System.out.println("filePath: " + filePath);
		System.out.println("commitID: " + commitID);
		
		long startRevesion = 0;
		long endRevesion = Long.parseLong(commitID);
		
		System.out.println("start Revesion: " + startRevesion);
		System.out.println("end Revesion: " + endRevesion);
		
		List<VCBlame> gitBlames = new ArrayList<VCBlame>();
		List<Map<String, Object>>blameinfolist = new ArrayList<Map<String, Object>>();
		
		SVNRepository repository = null;
		//블렘을 수행하는 핸들러 호출//
		try {	
			//Get LogClient//
			SVNClientManager clientManager = SVNClientManager.newInstance();
			SVNLogClient logClient = clientManager.getLogClient();
			
			boolean includeMergedRevisions = false;
			
			annotationhandler.setInit(includeMergedRevisions, true, logClient.getOptions());
			
			repository = this.getRepository(); //저장소를 불러온다.//
			
			SVNURL svnURL = repository.getRepositoryRoot(false).appendPath(filePath, false);
			
			System.out.println("repo address: " + repository.getRepositoryRoot(false).getPath());
			
			logClient.doAnnotate(svnURL, SVNRevision.UNDEFINED, SVNRevision.create(startRevesion), SVNRevision.create(endRevesion), annotationhandler);
		  
			blameinfolist = annotationhandler.getResult();
			
			System.out.println("blame info size: " + blameinfolist.size());
			
			for(int i=0; i<blameinfolist.size(); i++){
				gitBlames.add(new VCBlame(
						blameinfolist.get(i).get("commitID").toString(),
						blameinfolist.get(i).get("userName").toString(),
						blameinfolist.get(i).get("userEmail").toString(),
						blameinfolist.get(i).get("commitTime").toString()));
			}
			
			return gitBlames;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}

	/** 파일을 수정 시 commit까지 바로 진행
	 * @param name
	 * @param email
	 * @param branchName
	 * @param message
	 * @param zip
	 */
	public void updateFile(String name,String email,String branchName,String message,String path,String code){
		System.out.println("-> name: " + name);
		System.out.println("-> email: " + email);
		System.out.println("-> message: " + message);
		System.out.println("-> path: " + path);
		System.out.println("-> code: " + code);
		
		//SVN modify commit//
		SVNRepository repository = null;
		
		try {
			repository = this.getRepository(); //저장소를 불러온다.//
			
			byte[] oldcontents = code.getBytes();
			byte[] updatecontents = code.getBytes();
			
			SVNNodeKind nodeKind = repository.checkPath("", -1);
			System.out.println("-> status: " + nodeKind);
			
			long latestRevision = repository.getLatestRevision();
	        System.out.println("Repository latest revision (before committing): " + latestRevision);
	        
	        ISVNEditor editor = repository.getCommitEditor(message, null);
	        
	        System.out.println("midify path: " + path);
	        SVNCommitInfo commitInfo = modifyFile(editor, path, path, oldcontents, updatecontents);
	        System.out.println("The file was changed: " + commitInfo);
	        
	        //수정되었는지 확인//
	        if(commitInfo != null){
	        	System.out.println("==> edit result: success...");
	        } else if(commitInfo == null){
	        	System.out.println("==> edit result: fail...");
	        }
		} catch(SVNException e){
			e.printStackTrace();
		}
	}
	
	private static SVNCommitInfo modifyFile(ISVNEditor editor, String dirPath, String filePath, byte[] oldData, byte[] newData) throws SVNException {
        editor.openRoot(-1);
        editor.openDir(dirPath, -1);
        editor.openFile(filePath, -1);
        editor.applyTextDelta(filePath, null);
        
        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(oldData), 0, new ByteArrayInputStream(newData), editor, true);

        editor.closeFile(filePath, checksum);
        editor.closeDir();

        return editor.closeEdit();
    }
}