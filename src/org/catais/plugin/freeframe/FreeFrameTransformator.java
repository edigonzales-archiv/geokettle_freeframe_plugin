package org.catais.plugin.freeframe;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.logging.LogWriter;

import com.vividsolutions.jts.geom.Geometry;

public class FreeFrameTransformator {
	
	private final String sourceFrame;
	private final String targetFrame;

	public FreeFrameTransformator(String sourceFrame, String targetFrame) {
		this.sourceFrame = sourceFrame;
		this.targetFrame = targetFrame;
	}
	
	public void transform() throws KettleStepException {
		
		LogWriter.getInstance().logBasic("GeoKettle", "****************************333Test Test Test Test ");
		
	}
	
	
}
