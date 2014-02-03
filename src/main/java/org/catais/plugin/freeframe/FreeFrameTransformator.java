package org.catais.plugin.freeframe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.logging.LogWriter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.prep.PreparedPoint;
import com.vividsolutions.jts.geom.util.AffineTransformationBuilder;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

import org.catais.plugin.freeframe.IOUtils;

public class FreeFrameTransformator {
	
	private final String sourceFrame;
	private final String targetFrame;
	private final String triangularTransformationNetwork;
	private int SRID;
	
	private SimpleFeatureCollection featColl = null;
	private SimpleFeatureSource featSource = null;
	private SpatialIndex spatialIndex = null;

	public FreeFrameTransformator(String sourceFrame, String targetFrame, String triangularTransformationNetwork) throws IOException {
		this.sourceFrame = sourceFrame;
		this.targetFrame = targetFrame;
		this.triangularTransformationNetwork = triangularTransformationNetwork;
		
		createFeatureCollection();
		buildSpatialIndex();
		
		if (targetFrame.equalsIgnoreCase("LV95")) {
			SRID = 2056;
		} else {
			SRID = 21781;
		}
	}
	
	/**
	 * Transforms the specified source geometry in the source reference frame
     * in the target reference frame. Geometrycollections are not 
     * supported. If a coordinate of a geometry is not within the 
     * triangular network the original coordinate is returned.
	 * 
	 * @param sourceGeometry
	 * @return The geometry in the target reference frame.
	 * @throws KettleStepException
	 */
	public Geometry transform(Geometry sourceGeometry) throws KettleStepException {
		Geometry targetGeometry = null;
		
		if (sourceGeometry instanceof MultiPolygon) {
			int num = sourceGeometry.getNumGeometries();
			Polygon[] polys = new Polygon[num];
			for(int j=0; j<num; j++) {
				polys[j] = transformPolygon((Polygon) sourceGeometry.getGeometryN(j));
			}    
			targetGeometry = (Geometry) new GeometryFactory().createMultiPolygon(polys);
			
		} else if (sourceGeometry instanceof Polygon) {
			targetGeometry = (Geometry) transformPolygon((Polygon) sourceGeometry);
			
		} else if (sourceGeometry instanceof MultiLineString) {
			int num = sourceGeometry.getNumGeometries();
			LineString[] lines = new LineString[num];
			for(int j=0; j<num; j++) {
				lines[j] = transformLineString((LineString) sourceGeometry.getGeometryN(j));
			}    
			targetGeometry = (Geometry) new GeometryFactory().createMultiLineString(lines);
			
		} else if (sourceGeometry instanceof LineString) {
			targetGeometry = (Geometry) transformLineString((LineString) sourceGeometry);
			
		} else if (sourceGeometry instanceof MultiPoint) {
			int num = sourceGeometry.getNumGeometries();
			Point[] points  = new Point[num];
			for(int j=0; j<num; j++) {
				Coordinate coord = transformCoordinate(((Point) sourceGeometry.getGeometryN(j)).getCoordinate());
				points[j] = new GeometryFactory().createPoint(coord);
			} 
			targetGeometry = (Geometry) new GeometryFactory().createMultiPoint(points);
		
		} else if (sourceGeometry instanceof Point) {
			Coordinate coord = transformCoordinate(((Point) sourceGeometry).getCoordinate());
			targetGeometry = (Geometry) new GeometryFactory().createPoint(coord);
			
		} else {
			LogWriter.getInstance().logError("FreeFrameTransformator", "Geometry type not found: " + sourceGeometry.getGeometryType());
			targetGeometry = (Geometry) sourceGeometry.clone();
		}
		targetGeometry.setSRID(SRID);
		
		return targetGeometry;
	}
	
    private Polygon transformPolygon(Polygon p) {
    	LineString shell = (LineString) p.getExteriorRing();
    	LineString shellTransformed = transformLineString(shell);
    	
    	LinearRing[] rings = new LinearRing[p.getNumInteriorRing()];
    	int num = p.getNumInteriorRing();
    	for(int i=0; i<num; i++) {
    		LineString line = transformLineString(p.getInteriorRingN(i));	
    		rings[i] = new LinearRing(line.getCoordinateSequence(), new GeometryFactory()); 
    	}    	    	
    	return new Polygon(new LinearRing(shellTransformed.getCoordinateSequence(), new GeometryFactory()), rings, new GeometryFactory());
    }
    
    private LineString transformLineString(LineString l) {
    	Coordinate[] coords = l.getCoordinates();
    	int num = coords.length;

    	Coordinate[] coordsTransformed = new Coordinate[num];
    	for(int i=0; i<num; i++) {
    		coordsTransformed[i] = transformCoordinate(coords[i]);
    	}
    	CoordinateArraySequence sequence = new CoordinateArraySequence(coordsTransformed);
    	return new LineString(sequence, new GeometryFactory());
    }
	
    /**
     * Transforms the specified source coordinate in the source reference frame
     * in the target reference frame. If the coordinate is not within the 
     * triangular network the original coordinate is returned.
     * 
     * @param coord
     * @return The coordinate in the target reference frame.
     */
	private Coordinate transformCoordinate(Coordinate coord) {
    	Coordinate coordTransformed = new Coordinate();
    	Point point = new GeometryFactory().createPoint(coord);
    	PreparedPoint ppoint = new PreparedPoint(point.getInteriorPoint());

    	for (final Object o : spatialIndex.query(point.getEnvelopeInternal())) {
    		SimpleFeature f = (SimpleFeature) o;
    		MultiPolygon poly1 = (MultiPolygon) f.getDefaultGeometry();
    		if (ppoint.intersects(poly1)) {
    			String dstWkt = (String) f.getAttribute("dstwkt");
    			
    			try {
        			Polygon poly2 = (Polygon) new WKTReader2().read(dstWkt);
        			
        			Coordinate[] t1 = poly1.getCoordinates();
        			Coordinate[] t2 = poly2.getCoordinates();
        			
    				AffineTransformationBuilder builder = new AffineTransformationBuilder(t1[0], t1[1], t1[2], t2[0], t2[1], t2[2]);
    				builder.getTransformation().transform(coord, coordTransformed);
    				LogWriter.getInstance().logDebug("FreeFrameTransformator", coordTransformed.toString());
    				coordTransformed.z = coord.z;
    				return coordTransformed;
    				
//    				Set decimal format in dialog?
//    				DecimalFormat decimalForm = new DecimalFormat("#.###");
//    				Coordinate coordTransRound = new Coordinate(Double.valueOf(decimalForm.format(coordTransformed.x)), Double.valueOf(decimalForm.format(coordTransformed.y)));
//    				return coordTransRound;
        			
    			} catch (com.vividsolutions.jts.io.ParseException pe) {
    				pe.printStackTrace();
    				return coord;
    			} 			
    		}
    	}
    	return coord;
	}
    
	private void buildSpatialIndex() {
		spatialIndex = new STRtree();
		
		SimpleFeatureIterator iterator = featColl.features();
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				MultiPolygon poly = (MultiPolygon) feature.getDefaultGeometry();
				spatialIndex.insert(poly.getEnvelopeInternal(), feature);
			}
		}
		finally {
			iterator.close();
		}
		LogWriter.getInstance().logBasic("FreeFrameTransformator", "SpatialIndex built.");
	}
	
	/**
	 * Featurecollections of the triangular transformation
	 * network shapefiles are created here. Add new ttn here!
	 * 
	 * @throws IOException
	 */
	private void createFeatureCollection() throws IOException {
		File tempDir = IOUtils.createTempDirectory("freeframe");
		
		String prefix = null;
		if (triangularTransformationNetwork.equalsIgnoreCase("CHENyx06")) {
			prefix = "chenyx06";
		} else if (triangularTransformationNetwork.equalsIgnoreCase("BEENyx15")) {
			prefix = "beenyx15";
		} else {
			prefix = "chenyx06";
		}
		
		if (sourceFrame.equalsIgnoreCase("LV03")) {
			prefix = prefix.concat("lv03");
		} else {
			prefix = prefix.concat("lv95");
		}
		
		LogWriter.getInstance().logDebug("FreeFrameTransformator", "ttn prefix: " + prefix);

		InputStream is =  null;
		File ttn = null;
		
		is = FreeFrameTransformator.class.getResourceAsStream(prefix.concat(".dbf"));
		ttn = new File(tempDir, prefix.concat(".dbf"));
		IOUtils.copy(is, ttn);
		
		is = FreeFrameTransformator.class.getResourceAsStream(prefix.concat(".shp"));
		ttn = new File(tempDir, prefix.concat(".shp"));
		IOUtils.copy(is, ttn);

		is = FreeFrameTransformator.class.getResourceAsStream(prefix.concat(".shx"));
		ttn = new File(tempDir, prefix.concat(".shx"));
		IOUtils.copy(is, ttn);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> srcParams = new HashMap<String, Serializable>();
        srcParams.put("url", ttn.toURI().toURL());
        srcParams.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(srcParams);
        featSource = dataStore.getFeatureSource();
        featColl = featSource.getFeatures();
        
		LogWriter.getInstance().logDebug("FreeFrameTransformator", "Size of ttn feature collection: " + featColl.size());
	}
	
	
}
