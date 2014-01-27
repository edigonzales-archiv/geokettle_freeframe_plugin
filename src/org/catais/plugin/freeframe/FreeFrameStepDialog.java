package org.catais.plugin.freeframe;

import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

public class FreeFrameStepDialog extends BaseStepDialog implements StepDialogInterface {
	
	private FreeFrameStepMeta input;

	// output field name
	private Label wlValName, wlDirection, wlGeomName, wlSourceFrame, wlTargetFrame, wlTriangularTransformationNetwork;
	private Text wValName;
	private FormData fdlValName, fdValName, fdlGeomName, fdGeomField, fdlDirection, fdDirection, fdlSourceFrame, fdlTargetFrame, fdSourceFrame, fdTargetFrame, fdlTriangularTransformationNetwork, fdTriangularTransformationNetwork;
	private CCombo wGeomField,wDirection, wSourceFrame, wTargetFrame, wTriangularTransformationNetwork;
	
	public FreeFrameStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (FreeFrameStepMeta) in;
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("Template.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// output dummy value
		wlValName = new Label(shell, SWT.RIGHT);
		wlValName.setText(Messages.getString("Template.FieldName.Label")); 
		props.setLook(wlValName);
		fdlValName = new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right = new FormAttachment(middle, -margin);
		fdlValName.top = new FormAttachment(wStepname, margin);
		wlValName.setLayoutData(fdlValName);

		wValName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wValName);
		wValName.addModifyListener(lsMod);
		fdValName = new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right = new FormAttachment(100, 0);
		fdValName.top = new FormAttachment(wStepname, margin);
		wValName.setLayoutData(fdValName);
		
		// transformation direction/type
		wlDirection = new Label(shell, SWT.RIGHT);
		wlDirection.setText(Messages.getString("FreeFrameDialog.TransformationDirectionType.Label")); 
		props.setLook(wlDirection);
		fdlDirection = new FormData();
		fdlDirection.left = new FormAttachment(0, 0);
		fdlDirection.right = new FormAttachment(middle, -margin);
		fdlDirection.top = new FormAttachment(wValName, margin);
		wlDirection.setLayoutData(fdlDirection);

		wDirection = new CCombo(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		props.setLook(wDirection);
		wDirection.addModifyListener(lsMod);
		fdDirection = new FormData();
		fdDirection.left = new FormAttachment(middle, 0);
		fdDirection.right = new FormAttachment(100, 0);
		fdDirection.top = new FormAttachment(wValName, margin);
		wDirection.setLayoutData(fdDirection);
		fillDirectionTypesList(wDirection);	
		
		// geometry field
		wlGeomName = new Label(shell, SWT.RIGHT);
		wlGeomName.setText(Messages.getString("FreeFrameDialog.GeomFieldName.Label")); 
		props.setLook(wlGeomName);
		fdlGeomName = new FormData();
		fdlGeomName.left = new FormAttachment(0, 0);
		fdlGeomName.right = new FormAttachment(middle, -margin);
		fdlGeomName.top = new FormAttachment(wDirection, margin);
		wlGeomName.setLayoutData(fdlGeomName);

		wGeomField = new CCombo(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		props.setLook(wGeomField);
		wGeomField.addModifyListener(lsMod);
		fdGeomField = new FormData();
		fdGeomField.left = new FormAttachment(middle, 0);
		fdGeomField.right = new FormAttachment(100, 0);
		fdGeomField.top = new FormAttachment(wDirection, margin);
		wGeomField.setLayoutData(fdGeomField);
		fillGeometryFieldsList(wGeomField);
		
		// source reference frame
		wlSourceFrame = new Label(shell, SWT.RIGHT);
		wlSourceFrame.setText(Messages.getString("FreeFrameDialog.SourceReferenceFrame.Label")); 
		props.setLook(wlSourceFrame);
		fdlSourceFrame = new FormData();
		fdlSourceFrame.left = new FormAttachment(0, 0);
		fdlSourceFrame.right = new FormAttachment(middle, -margin);
		fdlSourceFrame.top = new FormAttachment(wGeomField, margin);
		wlSourceFrame.setLayoutData(fdlSourceFrame);

		wSourceFrame = new CCombo(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		props.setLook(wSourceFrame);
		wSourceFrame.addModifyListener(lsMod);
		fdSourceFrame = new FormData();
		fdSourceFrame.left = new FormAttachment(middle, 0);
		fdSourceFrame.right = new FormAttachment(100, 0);
		fdSourceFrame.top = new FormAttachment(wGeomField, margin);
		wSourceFrame.setLayoutData(fdSourceFrame);
		fillReferenceFrameList(wSourceFrame);

		// target reference frame
		wlTargetFrame = new Label(shell, SWT.RIGHT);
		wlTargetFrame.setText(Messages.getString("FreeFrameDialog.TargetReferenceFrame.Label")); 
		props.setLook(wlTargetFrame);
		fdlTargetFrame = new FormData();
		fdlTargetFrame.left = new FormAttachment(0, 0);
		fdlTargetFrame.right = new FormAttachment(middle, -margin);
		fdlTargetFrame.top = new FormAttachment(wSourceFrame, margin);
		wlTargetFrame.setLayoutData(fdlTargetFrame);

		wTargetFrame = new CCombo(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		props.setLook(wTargetFrame);
		wTargetFrame.addModifyListener(lsMod);
		fdTargetFrame = new FormData();
		fdTargetFrame.left = new FormAttachment(middle, 0);
		fdTargetFrame.right = new FormAttachment(100, 0);
		fdTargetFrame.top = new FormAttachment(wSourceFrame, margin);
		wTargetFrame.setLayoutData(fdTargetFrame);
		fillReferenceFrameList(wTargetFrame);

		// dataset (CHENyx06, BENyx15 etc.)
		wlTriangularTransformationNetwork = new Label(shell, SWT.RIGHT);
		wlTriangularTransformationNetwork.setText(Messages.getString("FreeFrameDialog.TriangularTransformationNetwork.Label")); 
		props.setLook(wlTriangularTransformationNetwork);
		fdlTriangularTransformationNetwork = new FormData();
		fdlTriangularTransformationNetwork.left = new FormAttachment(0, 0);
		fdlTriangularTransformationNetwork.right = new FormAttachment(middle, -margin);
		fdlTriangularTransformationNetwork.top = new FormAttachment(wTargetFrame, margin);
		wlTriangularTransformationNetwork.setLayoutData(fdlTriangularTransformationNetwork);
		
		wTriangularTransformationNetwork = new CCombo(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		props.setLook(wTriangularTransformationNetwork);
		wTriangularTransformationNetwork.addModifyListener(lsMod);
		fdTriangularTransformationNetwork = new FormData();
		fdTriangularTransformationNetwork.left = new FormAttachment(middle, 0);
		fdTriangularTransformationNetwork.right = new FormAttachment(100, 0);
		fdTriangularTransformationNetwork.top = new FormAttachment(wTargetFrame, margin);
		wTriangularTransformationNetwork.setLayoutData(fdTriangularTransformationNetwork);
		fillTriangularTransformationNetworkList(wTriangularTransformationNetwork);		
		      
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); 

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTriangularTransformationNetwork);

		
		// Add listeners
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wValName.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		
		// Set the shell size, based upon previous time...
		setSize();

		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Fills the combo-box with the available triangular 
	 * transformation networks.
	 * 
	 * @param combo The combo-box to fill.
	 */
	private void fillTriangularTransformationNetworkList(CCombo combo) {
		combo.setItems(new String[]{"CHENyx06", "BENyx15"});
	}
	
	/**
	 * Fills the combo-box with the available reference 
	 * frames.
	 * 
	 * @param combo The combo-box to fill.
	 */
	private void fillReferenceFrameList(CCombo combo) {
		combo.setItems(new String[]{"LV03", "LV95"});
	}
	
	/**
	 * Fills the combo-box with the available transformation 
	 * directions (and types).
	 * 
	 * @param combo The combo-box to fill.
	 */
	private void fillDirectionTypesList(CCombo combo) {
		combo.setItems(new String[]{"LV03 -> LV95 (CHENyx06)", "LV95 -> LV93 (CHENyx06)"});
	}
	
	/**
	 * Fills the combo-box with the available geometry-fields that allow
	 * a spatial reference system transformation.
	 * 
	 * @param combo The combo-box to fill.
	 */
	private void fillGeometryFieldsList(CCombo combo) {
		RowMetaInterface inputfields = null;
		try {
			inputfields = transMeta.getPrevStepFields(stepname);
		} catch (KettleException e) {
			inputfields = new RowMeta();
			new ErrorDialog(shell,"Error", "Could not find the fields", e);
		}

		String[] fieldNames			= inputfields.getFieldNames();
		String[] fieldNamesAndTypes = inputfields.getFieldNamesAndTypes(0);
		TreeSet<String> geomFields  = new TreeSet<String>();
		for (int i=0; i < fieldNames.length; i++) {
			if (fieldNamesAndTypes[i].toLowerCase().contains("geometry")) {
				geomFields.add(fieldNames[i]);
			}
		}
		combo.setItems(geomFields.toArray(new String[]{}));

		// set the default selection from loaded repo/xml
//		int existingSelection = combo.indexOf(fieldname);
//		if (existingSelection > -1)
//			combo.select(existingSelection);
	}	
	
	// Read data and place it in the dialog
	public void getData() {
		wStepname.selectAll();
		wValName.setText(input.getOutputField());
		wDirection.setText(input.getDirection());
		wGeomField.setText(input.getFieldName());
		wSourceFrame.setText(input.getSourceFrame());
		wTargetFrame.setText(input.getTargetFrame());
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}
	
	// let the plugin know about the entered data
	private void ok() {
		stepname = wStepname.getText(); // return value
		input.setOutputField(wValName.getText());
		input.setDirection(wDirection.getText());
		input.setFieldName(wGeomField.getText());
		input.setSourceFrame(wSourceFrame.getText());
		input.setTargetFrame(wTargetFrame.getText());
		dispose();
	}
}
