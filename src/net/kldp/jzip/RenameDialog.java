/**
 * RenameDialog : 이름 바꾸기 대화상자
 */
package net.kldp.jzip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 이름 바꾸기 대화상자 클래스
 * 
 * @author Seungwon Jeong
 * 
 */
public class RenameDialog {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="98,32"

	private Label label = null;

	private Text text = null;

	private String fileName = null;

	private Composite composite = null;

	private Button buttonOk = null;
	private Button buttonCancel = null;

	private Label icon = null;

	/**
	 * RenameDialog 클래스의 생성자
	 * 
	 * @param parent 부모 {@link Shell}
	 * @param fileName 이름을 바꿀 항목명
	 */
	public RenameDialog(Shell parent, String fileName) {
		this.fileName = fileName;
		
		createSShell();

		text.setText(fileName);

		sShell.setParent(parent);
		
		sShell.pack();
		
		final Point parentLocation = parent.getLocation();
		final Point parentSize = parent.getSize();
		final Point size = sShell.getSize();
		
		int x = parentLocation.x + (parentSize.x - size.x) / 2;
		int y = parentLocation.y + (parentSize.y - size.y) / 2;
		
		if (x >= parentLocation.x && y >= parentLocation.y)
			sShell.setLocation(x, y);
		else
			sShell.setLocation(parentLocation);
	}

	/**
	 * This method initializes composite	
	 *
	 */
	private void createComposite() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		gridData.horizontalSpan = 2;
		gridData.verticalAlignment = GridData.CENTER;
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		
		composite = new Composite(sShell, SWT.NONE);
		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);
		
		// 확인
		buttonOk = new Button(composite, SWT.NONE);
		buttonOk.setText("확인(&O)");
		buttonOk.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				fileName = text.getText().trim();
				
				sShell.dispose();
			}
		});
		
		// 취소
		buttonCancel = new Button(composite, SWT.NONE);
		buttonCancel.setText("취소(&C)");
		buttonCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				fileName = null;
				
				sShell.dispose();
			}
		});
	}

	/**
	 * This method initializes sShell
	 * 
	 */
	private void createSShell() {
		GridData gridData = new GridData();
		
		GridData gridData1 = new GridData();
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.widthHint = 200;
		gridData1.grabExcessHorizontalSpace = false;
		gridData1.horizontalAlignment = GridData.CENTER;
		
		GridData gridData2 = new GridData();
		gridData2.verticalSpan = 2;
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.verticalSpacing = 10;
		gridLayout.horizontalSpacing = 10;
		gridLayout.makeColumnsEqualWidth = false;
		
		sShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		sShell.setImage(JZip.jzipImage);
		sShell.setText("이름 바꾸기");
		sShell.setSize(new Point(278, 104));
		sShell.setLayout(gridLayout);
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			@Override
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				fileName = null;
			}
		});
		
		icon = new Label(sShell, SWT.NONE);
		icon.setImage(sShell.getDisplay().getSystemImage(SWT.ICON_QUESTION));
		icon.setLayoutData(gridData2);
		
		label = new Label(sShell, SWT.NONE);
		label.setText("새로운 이름 :");
		label.setLayoutData(gridData);
		
		text = new Text(sShell, SWT.BORDER);
		text.setLayoutData(gridData1);
		text.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			@Override
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					fileName = null;

					sShell.dispose();
				}
			}
		});
		text.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
				// 엔터 키를 입력한 경우
				
				fileName = text.getText().trim();
				
				sShell.dispose();
			}
		});
		
		text.addTraverseListener(new org.eclipse.swt.events.TraverseListener() {
			public void keyTraversed(org.eclipse.swt.events.TraverseEvent e) {
				e.doit = false;
			}
		});
		
		createComposite();
	}

	/**
	 * {@link RenameDialog}를 여는 메소드
	 * 
	 * @return 새로운 이름
	 */
	public String open() {
		text.selectAll();
		text.setFocus();
		
		sShell.open();
		Display display = sShell.getDisplay();
		while (!sShell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();

		return fileName;
	}
}
