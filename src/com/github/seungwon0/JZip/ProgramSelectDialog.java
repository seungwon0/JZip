/**
 * ProgramSelectionDialog : 프로그램 선택 대화상자
 */
package com.github.seungwon0.JZip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 프로그램 선택 대화상자
 * 
 * @author Seungwon Jeong
 * 
 */
public class ProgramSelectDialog {

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,10"

	private Label label = null;

	private Text text = null;

	private Composite composite = null;

	private Button buttonOk = null;
	private Button buttonCancel = null;

	private String command;

	private Label icon = null;

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		sShell.setImage(JZip.jzipImage);
		sShell.setText("사용할 프로그램 명령어를 입력하세요.");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 10;
		gridLayout.verticalSpacing = 10;
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(318, 114));

		GridData gridData;

		icon = new Label(sShell, SWT.NONE);
		icon.setImage(sShell.getDisplay().getSystemImage(SWT.ICON_QUESTION));
		gridData = new GridData();
		gridData.verticalSpan = 2;
		gridData.verticalAlignment = GridData.BEGINNING;
		gridData.horizontalAlignment = GridData.CENTER;
		icon.setLayoutData(gridData);

		label = new Label(sShell, SWT.WRAP);
		label.setText(" 파일을 여는 데 사용할 프로그램 명령어를 입력하세요.");
		gridData = new GridData();
		gridData.heightHint = -1;
		gridData.widthHint = 250;
		label.setLayoutData(gridData);

		text = new Text(sShell, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.widthHint = 200;
		gridData.verticalAlignment = GridData.CENTER;
		text.setLayoutData(gridData);
		text.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetDefaultSelected(
					org.eclipse.swt.events.SelectionEvent e) {
				// 엔터 키를 입력한 경우

				command = text.getText().trim();

				sShell.dispose();
			}
		});
		text.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					command = null;

					sShell.dispose();
				}
			}
		});
		text.addTraverseListener(new org.eclipse.swt.events.TraverseListener() {
			public void keyTraversed(org.eclipse.swt.events.TraverseEvent e) {
				e.doit = false;
			}
		});

		createComposite();

		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				command = null;
			}
		});
	}

	/**
	 * This method initializes composite
	 * 
	 */
	private void createComposite() {
		composite = new Composite(sShell, SWT.NONE);
		composite.setLayout(new RowLayout());
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		gridData.horizontalSpan = 2;
		gridData.verticalAlignment = GridData.CENTER;
		composite.setLayoutData(gridData);

		// 확인
		buttonOk = new Button(composite, SWT.NONE);
		buttonOk.setText("확인(&O)");
		buttonOk.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				command = text.getText().trim();

				sShell.dispose();
			}
		});

		// 취소
		buttonCancel = new Button(composite, SWT.NONE);
		buttonCancel.setText("취소(&C)");
		buttonCancel
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						command = null;

						sShell.dispose();
					}
				});
	}

	/**
	 * {@link ProgramSelectDialog}의 생성자
	 * 
	 * @param parent
	 *            부모 {@link Shell}
	 * @param fileName
	 *            파일명
	 */
	public ProgramSelectDialog(Shell parent, String fileName) {
		command = null;

		createSShell();

		sShell.setParent(parent);

		label.setText(fileName + label.getText());

		sShell.pack();

		final Point parentLocation = parent.getLocation();
		final Point parentSize = parent.getSize();
		final Point size = sShell.getSize();

		final int x = parentLocation.x + (parentSize.x - size.x) / 2;
		final int y = parentLocation.y + (parentSize.y - size.y) / 2;

		if (x >= parentLocation.x && y >= parentLocation.y)
			sShell.setLocation(x, y);
		else
			sShell.setLocation(parentLocation);
	}

	/**
	 * {@link ProgramSelectDialog}를 화면에 표시하는 메소드
	 * 
	 * @return 프로그램 명령
	 */
	public String open() {
		text.setFocus();

		sShell.open();
		Display display = sShell.getDisplay();
		while (!sShell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();

		return command;
	}
}
