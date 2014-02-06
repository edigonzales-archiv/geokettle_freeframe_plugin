package org.catais.plugin.freeframe;

import java.io.IOException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import org.catais.plugin.freeframe.FreeFrameTransformator;

import com.vividsolutions.jts.geom.Geometry;

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
		
		String sourceFrame = meta.getSourceFrame();
		String targetFrame = meta.getTargetFrame();
		String triangularTransformationNetwork = meta.getTriangularTransformationNetwork();

		Object[] r = getRow(); // get row, blocks when needed!
		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;

			RowMetaInterface outputRowMeta = getInputRowMeta().clone();
        	meta.getFields(outputRowMeta, getStepname(), null, null, this);
            data.setOutputRowMeta(outputRowMeta);
            
			try {
				transformator = new FreeFrameTransformator(sourceFrame, targetFrame, triangularTransformationNetwork);
			} catch (IOException e) {
				log.logError("FreeFrameStep", e.getMessage());
				setErrors(1);
				setOutputDone();
				return false;
			}
			logBasic("freeframe step initialized successfully");
		}

		if (sourceFrame.equalsIgnoreCase(targetFrame)) {
			logDebug("no transformation necessary (sourceFrame = targetFrame)");
			putRow(data.getOutputRowMeta(), r);
			return true;
		} 

		try {			
			Object[] outputRow = transformSpatialReferenceSystem(data.getOutputRowMeta(), r);
			putRow(data.getOutputRowMeta(), outputRow);
		} catch (KettleStepException ke) {
			log.logError("FreeFrameStep", ke.getSuperMessage());
			setErrors(1);
			setOutputDone();
			return false;
		}
		return true;
	}

	private synchronized Object[] transformSpatialReferenceSystem(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
		final int LENGTH = row.length;
		Object[] result = new Object[LENGTH];
		
		for (int i=0; i < LENGTH; i++) {
			if (row[i] != null) {
				ValueMetaInterface vm = rowMeta.getValueMeta(i);
				if (vm.getName().equals(meta.getFieldName()) && vm.isGeometry()) {
					result[i] = transformator.transform( (Geometry) row[i] );
				} else {
					result[i] = row[i];
				}
			} else {
				result[i] = null;
			}
		}
		return result;
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
