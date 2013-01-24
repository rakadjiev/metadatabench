package edu.cmu.pdl.metadatabench.generator;

import java.util.Random;

public class UniformCreationStrategy extends AbstractDirectoryCreationStrategy {

	private Random randomId;
	private int masters;
	
	public UniformCreationStrategy(INamespaceMapDAO dao, int masters){
		this(dao, "", masters);
	}
	
	public UniformCreationStrategy(INamespaceMapDAO dao, String workingDirectory, int masters) {
		super(dao, workingDirectory);
		randomId = new Random();
		this.masters = masters;
	}

	@Override
	public String selectDirectory(int i) {
		int from = i;
		if(i > (masters)){
			from = i - ((i-1) % masters);
		}
		int key = randomId.nextInt(from-1) + 1;
		String dirPath = dao.getDir(key);
		return dirPath;
	}

}