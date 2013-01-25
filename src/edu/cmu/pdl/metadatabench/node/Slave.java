package edu.cmu.pdl.metadatabench.node;

import com.hazelcast.core.HazelcastInstance;

import edu.cmu.pdl.metadatabench.cluster.HazelcastMapDAO;

public class Slave{

	private static final int THREADS = 100; // TODO: param
	private static HazelcastInstance hazelcast;
	private static OperationExecutor executor;
	private static OperationHandler handler;
	
	public static void start(HazelcastInstance hazelcast) {
		executor = new OperationExecutor(new HDFSClient(), THREADS);
		handler = new OperationHandler(executor, new HazelcastMapDAO(hazelcast));
//		IMap<Integer,String> dirMap = hazelcast.getMap("directories");
//		dirMap.addLocalEntryListener(new DirectoryEntryListener(executor));
//
//		IMap<Integer,String> fileMap = hazelcast.getMap("files");
//		fileMap.addLocalEntryListener(new FileEntryListener(executor));
	}
	
	public static OperationHandler getOperationHandler(){
		return handler;
	}
	
	public static HazelcastInstance getHazelcastInstance(){
		return hazelcast;
	}

}