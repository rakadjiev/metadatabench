package edu.cmu.pdl.metadatabench.generator;

import com.hazelcast.core.AtomicNumber;


public abstract class AbstractDirectoryCreationStrategy {

	protected static char PATH_SEPARATOR = '/';
	private static String DIR_NAME_PREFIX = PATH_SEPARATOR + "dir";
	
	private String workingDirectory;
	protected AtomicNumber numberOfDirs;
	protected INamespaceMapDAO dao;
	
	public AbstractDirectoryCreationStrategy(INamespaceMapDAO dao, String workingDirectory){
		this.workingDirectory = workingDirectory;
		while(this.workingDirectory.endsWith("/")){
			this.workingDirectory = this.workingDirectory.substring(0, this.workingDirectory.length() - 1);
		}
		numberOfDirs = ((HazelcastMapDAO)dao).getHazelcastInstance().getAtomicNumber("numberOfDirs");
		this.dao = dao;
	}
	
	abstract public String selectDirectory(int i);
	
	public void createNextDirectory(int i){
		String parentPath = selectDirectory(i);
//		long dirs = numberOfDirs.incrementAndGet();
//		String name = parentPath + DIR_NAME_PREFIX + dirs;
//		dao.createDir(dirs, name);
		String name = parentPath + DIR_NAME_PREFIX + i;
		dao.createDir(i, name);
	}
	
	public void createRoot(){
		if(numberOfDirs.compareAndSet(0,1)){
			System.out.println("I am the first, so I'll create the root directories.");
			String rootPath = workingDirectory + DIR_NAME_PREFIX + 1;
			dao.createDir(1, rootPath);
			numberOfDirs.incrementAndGet();
			String firstDirPath = rootPath + DIR_NAME_PREFIX + 2;
			dao.createDir(2, firstDirPath);
			
		} else {
			System.out.println("I'll wait until the root directories are created.");
			while(numberOfDirs.get() < 2){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
