package edu.cmu.pdl.metadatabench.master.namespace;

import java.util.Random;

import edu.cmu.pdl.metadatabench.cluster.INamespaceMapDAO;
import edu.cmu.pdl.metadatabench.cluster.IOperationDispatcher;

public class UniformCreationStrategy extends AbstractDirectoryCreationStrategy {

	private Random randomId;
	private int masters;
	
	public UniformCreationStrategy(INamespaceMapDAO dao, IOperationDispatcher dispatcher, int masters){
		this(dao, dispatcher, "", masters);
	}
	
	public UniformCreationStrategy(INamespaceMapDAO dao, IOperationDispatcher dispatcher, String workingDirectory, int masters) {
		super(dao, dispatcher, workingDirectory);
		randomId = new Random();
		this.masters = masters;
	}

	@Override
	public long selectDirectory(int i) {
		int from = i;
		if(i > (masters)){
			from = i - ((i-1) % masters);
		}
		int key = randomId.nextInt(from-1) + 1;
		return (long) key;
	}

}
