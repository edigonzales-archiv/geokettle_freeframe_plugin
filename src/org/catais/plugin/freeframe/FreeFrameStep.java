package org.catais.plugin.freeframe;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import org.catais.plugin.freeframe.FreeFrameTransformator;

public class FreeFrameStep extends BaseStep implements StepInterface {

	private FreeFrameStepData data;
	private FreeFrameStepMeta meta;
	private FreeFrameTransformator transformator;
	
	public FreeFrameStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (FreeFrameStepMeta) smi;
		data = (FreeFrameStepData) sdi;

		Object[] r = getRow(); // get row, blocks when needed!
		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;

			data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			String sourceFrame = meta.getSourceFrame();
			String targetFrame = meta.getTargetFrame();
			// INDEX HIER!!
			transformator = new FreeFrameTransformator(sourceFrame, targetFrame);
			

			logBasic("template step initialized successfully");
		}

		Object[] outputRow = RowDataUtil.addValueData(r, data.outputRowMeta.size() - 1, "dummy value");

		putRow(data.outputRowMeta, outputRow); // copy row to possible alternate rowset(s)
		
		
		// my stuff
		try {
			Object[] myOutputRow = transformSpatialReferenceSystem(data.outputRowMeta, r);
		} catch (KettleStepException ke) {
			log.logError("GeoKettle", ke.getSuperMessage());
			setErrors(1);
			setOutputDone();
			return false;
			
		}
		

		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}

		return true;
	}

	private synchronized Object[] transformSpatialReferenceSystem(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
		transformator.transform();
		
		return row;
	}
	
	
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (FreeFrameStepMeta) smi;
		data = (FreeFrameStepData) sdi;

		return super.init(smi, sdi);
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (FreeFrameStepMeta) smi;
		data = (FreeFrameStepData) sdi;

		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run() {
		logBasic("Starting to run...");
		try {
			while (processRow(meta, data) && !isStopped())
				;
		} catch (Exception e) {
			logError("Unexpected error : " + e.toString());
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		} finally {
			dispose(meta, data);
			logBasic("Finished, processing " + getLinesRead() + " rows");
			markStop();
		}
	}

}
