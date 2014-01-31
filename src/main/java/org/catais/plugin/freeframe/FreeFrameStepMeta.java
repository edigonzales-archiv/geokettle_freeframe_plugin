package org.catais.plugin.freeframe;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.*;
import org.pentaho.di.core.database.DatabaseMeta; 
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.row.*;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.*;
import org.pentaho.di.trans.*;
import org.pentaho.di.trans.step.*;
import org.w3c.dom.Node;

public class FreeFrameStepMeta extends BaseStepMeta implements StepMetaInterface {

	private String outputField;
	
	/** On which field is the transformation applied? **/
	private String fieldName;
	
	/** Source reference frame **/
	private String sourceFrame;
	
	/** Target reference frame **/
	private String targetFrame;
	
	/** Triangular Transformation Network **/
	private String triangularTransformationNetwork;


	public FreeFrameStepMeta() {
		super(); 
		fieldName = "";
		sourceFrame = "";
		targetFrame = "";
		triangularTransformationNetwork = "";
	}

	public String getOutputField() {
		return outputField;
	}

	public void setOutputField(String outputField) {
		this.outputField = outputField;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
		
	public String getSourceFrame() {
		return sourceFrame;
	}
	
	public void setSourceFrame(String sourceFrame) {
		this.sourceFrame = sourceFrame;
	}
	
	public String getTargetFrame() {
		return targetFrame;
	}
	
	public void setTargetFrame(String targetFrame) {
		this.targetFrame = targetFrame;
	}
	
	public String getTriangularTransformationNetwork() {
		return triangularTransformationNetwork;
	}

	public void setTriangularTransformationNetwork(String triangularTransformationNetwork) {
		this.triangularTransformationNetwork = triangularTransformationNetwork;
	}
	
	public String getXML() throws KettleValueException {
		String retval = "";
		retval += "		<outputfield>" + getOutputField() + "</outputfield>" + Const.CR;
		retval += "		<fieldname>" + getFieldName() + "</fieldname>" + Const.CR;
		retval += "		<source_frame>" + getSourceFrame() + "</source_frame>" + Const.CR;
		retval += "		<target_frame>" + getTargetFrame() + "</target_frame>" + Const.CR;
		retval += "		<triangular_transformation_network>" + getTriangularTransformationNetwork() + "</triangular_transformation_network>" + Const.CR;
		return retval;
	}

	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {

		// append the outputField to the output
		ValueMetaInterface v1 = new ValueMeta();
		v1.setName(outputField);
		v1.setType(ValueMeta.TYPE_STRING);
		v1.setTrimType(ValueMeta.TRIM_TYPE_BOTH);
		v1.setOrigin(origin);

		r.addValueMeta(v1);
		
		// change output SRS
		int idx = r.indexOfValue(fieldName);
		if (idx >= 0) {
			ValueMetaInterface v = r.getValueMeta(idx);
			
			String targetEPSG;
			if (targetFrame.trim().equalsIgnoreCase("LV95")) {
				targetEPSG = "EPSG:2056";
			} else {
				targetEPSG = "EPSG:21781";
			}
			v.setGeometrySRS(SRS.createFromEPSG(targetEPSG));
			v.setOrigin(origin);
		}	
	}

	public Object clone() {
		Object retval = super.clone();
		return retval;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {

		try {
			setOutputField(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "outputfield")));
			setFieldName(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "fieldname")),""));			
			setSourceFrame(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "source_frame")),""));
			setTargetFrame(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "target_frame")),""));
			setTriangularTransformationNetwork(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "triangular_transformation_network")),""));
		} catch (Exception e) {
			throw new KettleXMLException("Template Plugin Unable to read step info from XML node", e);
		}

	}

	public void setDefault() {
		outputField = "template_outfield";
//		fieldName = "";
//		direction = "";
//		sourceFrame = "";
//		targetFrame = "";
//		triangularTransformationNetwork = "";
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		CheckResult cr;

		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
			remarks.add(cr);
		}	
    	
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new FreeFrameStepDialog(shell, meta, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new FreeFrameStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	public StepDataInterface getStepData() {
		return new FreeFrameStepData();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try
		{
			outputField  = rep.getStepAttributeString(id_step, "outputfield"); //$NON-NLS-1$
			fieldName = rep.getStepAttributeString(id_step, "fieldname"); //$NON-NLS-1$
			sourceFrame = rep.getStepAttributeString(id_step, "source_frame"); //$NON-NLS-1$
			targetFrame = rep.getStepAttributeString(id_step, "target_frame"); //$NON-NLS-1$
			triangularTransformationNetwork = rep.getStepAttributeString(id_step, "triangular_transformation_network"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("FreeFrameStep.Exception.UnexpectedErrorInReadingStepInfo"), e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "outputfield", outputField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fieldname", fieldName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "source_frame", sourceFrame); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "target_frame", targetFrame); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "triangular_transformation_network", triangularTransformationNetwork); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("FreeFrameStep.Exception.UnableToSaveStepInfoToRepository")+id_step, e); 
		}
	}
}
