package edu.cmu.pdl.metadatabench.master;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import com.hazelcast.core.HazelcastInstance;

import edu.cmu.pdl.metadatabench.cluster.HazelcastDispatcher;
import edu.cmu.pdl.metadatabench.cluster.HazelcastMapDAO;
import edu.cmu.pdl.metadatabench.cluster.INamespaceMapDAO;
import edu.cmu.pdl.metadatabench.cluster.IOperationDispatcher;
import edu.cmu.pdl.metadatabench.cluster.MeasurementsCollect;
import edu.cmu.pdl.metadatabench.cluster.MeasurementsReset;
import edu.cmu.pdl.metadatabench.cluster.ProgressReset;
import edu.cmu.pdl.metadatabench.master.namespace.AbstractDirectoryCreationStrategy;
import edu.cmu.pdl.metadatabench.master.namespace.AbstractFileCreationStrategy;
import edu.cmu.pdl.metadatabench.master.namespace.BarabasiAlbertDirectoryCreationStrategy;
import edu.cmu.pdl.metadatabench.master.namespace.NamespaceGenerator;
import edu.cmu.pdl.metadatabench.master.namespace.ZipfianFileCreationStrategy;
import edu.cmu.pdl.metadatabench.master.workload.WorkloadGenerator;
import edu.cmu.pdl.metadatabench.measurement.MeasurementDataCollection;
import edu.cmu.pdl.metadatabench.measurement.MeasurementDataForNode;
import edu.cmu.pdl.metadatabench.measurement.TextMeasurementsExporterExt;

public class Master {

	public static void start(HazelcastInstance hazelcast, int id, int masters, int numberOfDirs, int numberOfFiles, int numberOfOperations){
		INamespaceMapDAO dao = new HazelcastMapDAO(hazelcast);
		IOperationDispatcher dispatcher = new HazelcastDispatcher(hazelcast);
		
		AbstractDirectoryCreationStrategy dirCreator = new BarabasiAlbertDirectoryCreationStrategy(dao, dispatcher, "/workDir", masters);
		AbstractFileCreationStrategy fileCreator = new ZipfianFileCreationStrategy(dispatcher, numberOfDirs);
		NamespaceGenerator nsGen = new NamespaceGenerator(dirCreator, fileCreator, id, masters);
		
		if(numberOfDirs > 0){
			System.out.println("Dir creation started");
			long start = System.currentTimeMillis();
			nsGen.generateDirs(numberOfDirs);
			long end = System.currentTimeMillis();
			System.out.println(numberOfDirs + " dirs generated in: " + (end-start)/1000.0);
			try {
				ProgressBarrier.awaitOperationCompletion(numberOfDirs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double creationTime = (System.currentTimeMillis()-start)/1000.0;
			System.out.println("Dir creation done in: " + creationTime + " s");
			System.out.println("Throughput: " + (numberOfDirs/creationTime) + " ops/s");
			ProgressBarrier.reset();
			dispatcher.dispatch(new ProgressReset());
		}
		
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(numberOfFiles > 0){
			System.out.println("File creation started");
			long start = System.currentTimeMillis();
			nsGen.generateFiles(numberOfFiles);
			long end = System.currentTimeMillis();
			System.out.println(numberOfFiles + " files generated in: " + (end-start)/1000.0);
			try {
				ProgressBarrier.awaitOperationCompletion(numberOfFiles);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double creationTime = (System.currentTimeMillis()-start)/1000.0;
			System.out.println("File creation done in: " + creationTime + " s");
			System.out.println("Throughput: " + (numberOfFiles/creationTime) + " ops/s");
			ProgressBarrier.reset();
			dispatcher.dispatch(new ProgressReset());
			collectAndExportMeasurements(dispatcher);
		}

		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(numberOfOperations > 0){
			System.out.println("Workload generation started");
			long start = System.currentTimeMillis();
			WorkloadGenerator wlGen = new WorkloadGenerator(dispatcher, numberOfOperations, numberOfDirs, numberOfFiles);
			wlGen.generate();
			long end = System.currentTimeMillis();
			System.out.println(numberOfOperations + " operations generated in: " + (end-start)/1000.0);
			try {
				ProgressBarrier.awaitOperationCompletion(numberOfOperations);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double creationTime = (System.currentTimeMillis()-start)/1000.0;
			System.out.println("Workload done in: " + creationTime + " s");
			System.out.println("Throughput: " + (numberOfOperations/creationTime) + " ops/s");
			ProgressBarrier.reset();
			dispatcher.dispatch(new ProgressReset());
			collectAndExportMeasurements(dispatcher);
		}
	}
	
	private static void collectAndExportMeasurements(IOperationDispatcher dispatcher){
		MeasurementDataCollection measurements = MeasurementDataCollection.getInstance();
		try {
			Collection<MeasurementDataForNode> measurementDataCollection = dispatcher.dispatch(new MeasurementsCollect());
			for(MeasurementDataForNode measurementData : measurementDataCollection){
				measurements.addMeasurementData(measurementData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			TextMeasurementsExporterExt exporter = new TextMeasurementsExporterExt(baos);
			measurements.exportMeasurements(exporter);
//			measurements.exportMeasurementsPerNode(exporter);
			exporter.close();
			System.out.println(baos.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		dispatcher.dispatch(new MeasurementsReset());
		measurements.reset();
	}

}
