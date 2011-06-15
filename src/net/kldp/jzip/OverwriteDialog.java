/**
 * OverwriteDialog : 파일 덮어쓰기 대화상자 
 */
package net.kldp.jzip;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * 파일 덮어쓰기 대화상자
 * 
 * @author jeongseungwon
 * 
 */
public class OverwriteDialog {

	public static enum Overwrite {
		CANCEL, YES, NO, ALL_YES, ALL_NO
	}

	private Shell sShell = null;

	private Label label = null;

	private Overwrite overwrite;

	private Composite composite = null;

	private Button buttonYes = null;

	private Button buttonNo = null;

	private Button buttonAllYes = null;

	private Button buttonAllNo = null;

	private Label icon = null;

	/**
	 * {@link OverwriteDialog} 생성자
	 * 
	 * @param parent
	 *            부모 {@link Shell}
	 * @param fileName
	 *            확인할 파일명
	 */
	public OverwriteDialog(Shell parent, String fileName) {
		overwrite = Overwrite.NO;

		createSShell();

		sShell.setParent(parent);

		label.setText(fileName + label.getText());

		sShell.pack();

		final Point parentLocation = parent.getLocation();
		final Point parentSize = parent.getSize();
		final Point size = sShell.getSize();

		int x = parentLocation.x + (parentSize.x - size.x) / 2;
		int y = parentLocation.y + (parentSize.y - size.y) / 2;

		if (x >= parentLocation.x && y >= parentLocation.y) {
			sShell.setLocation(x, y);
		} else {
			sShell.setLocation(parentLocation);
		}
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.CENTER;
		gridData.heightHint = -1;
		gridData.widthHint = 360;
		gridData.horizontalAlignment = GridData.BEGINNING;

		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.CENTER;
		gridData1.verticalAlignment = GridData.CENTER;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.verticalSpacing = 10;
		gridLayout.horizontalSpacing = 10;
		gridLayout.makeColumnsEqualWidth = false;

		sShell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		sShell.setLayout(gridLayout);
		sShell.setImage(JZip.jzipImage);
		sShell.setText("덮어쓸까요?");

		icon = new Label(sShell, SWT.NONE);
		icon.setImage(sShell.getDisplay().getSystemImage(SWT.ICON_QUESTION));
		icon.setLayoutData(gridData1);

		label = new Label(sShell, SWT.HORIZONTAL | SWT.WRAP);
		label.setText(" 파일 또는 디렉토리가 이미 존재합니다.\n\n덮어쓸까요?");
		label.setLayoutData(gridData);

		createComposite();

		sShell.setSize(new Point(428, 100));

		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				overwrite = Overwrite.CANCEL;
			}
		});
	}

	/**
	 * 파일 덮어쓰기 확인 대화상자를 화면에 표시하는 메소드
	 * 
	 * @return 사용자의 응답
	 */
	public Overwrite open() {
		buttonYes.setFocus();

		sShell.open();
		Display display = sShell.getDisplay();
		while (!sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return overwrite;
	}

	/**
	 * This method initializes composite
	 * 
	 */
	private void createComposite() {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.END;

		composite = new Composite(sShell, SWT.NONE);
		composite.setLayout(new RowLayout());
		composite.setLayoutData(gridData);

		// 예
		buttonYes = new Button(composite, SWT.NONE);
		buttonYes.setText("예(&Y)");
		buttonYes
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						overwrite = Overwrite.YES;

						sShell.dispose();
					}
				});
		buttonYes.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					overwrite = Overwrite.CANCEL;

					sShell.dispose();
				} else if (e.keyCode == SWT.ARROW_RIGHT) {
					buttonNo.setFocus();
				}
			}
		});

		// 아니오
		buttonNo = new Button(composite, SWT.NONE);
		buttonNo.setText("아니오(&N)");
		buttonNo.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					overwrite = Overwrite.CANCEL;

					sShell.dispose();
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					buttonYes.setFocus();
				} else if (e.keyCode == SWT.ARROW_RIGHT) {
					buttonAllYes.setFocus();
				}
			}
		});
		buttonNo
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						overwrite = Overwrite.NO;

						sShell.dispose();
					}
				});

		// 모두 예
		buttonAllYes = new Button(composite, SWT.NONE);
		buttonAllYes.setText("모두 예(&A)");
		buttonAllYes
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						overwrite = Overwrite.ALL_YES;

						sShell.dispose();
					}
				});
		buttonAllYes.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					overwrite = Overwrite.CANCEL;

					sShell.dispose();
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					buttonNo.setFocus();
				} else if (e.keyCode == SWT.ARROW_RIGHT) {
					buttonAllNo.setFocus();
				}
			}
		});

		// 모두 아니오
		buttonAllNo = new Button(composite, SWT.NONE);
		buttonAllNo.setText("모두 아니오(&Q)");
		buttonAllNo
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						overwrite = Overwrite.ALL_NO;

						sShell.dispose();
					}
				});
		buttonAllNo.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					overwrite = Overwrite.CANCEL;

					sShell.dispose();
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					buttonAllYes.setFocus();
				}
			}
		});
	}
}
