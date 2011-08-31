/**
 * ActionSelectionDialog : 동작 선택 대화상자
 */
package com.github.seungwon0.JZip;

import java.io.File;

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

/**
 * 동작 선택 대화상자
 * 
 * @author Seungwon Jeong
 * 
 */
public class ActionSelectDialog {

	public static enum Action {
		CANCEL, ADD, OPEN,
	}

	private Shell sShell = null;

	private Label label = null;

	private Composite composite = null;

	private Button buttonCancel = null;
	private Button buttonAdd = null;
	private Button buttonOpen = null;

	private Action select;

	private Label icon = null;

	public ActionSelectDialog(Shell parent, String fileName) {
		select = Action.CANCEL;

		createSShell();

		sShell.setParent(parent);

		// Show only file name, not full path
		label.setText(new File(fileName).getName()
				+ " 파일을 현재 열려있는 압축 파일에 더할까요?\n아니면 새로운 압축 파일로 열까요?");

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

		// 취소
		buttonCancel = new Button(composite, SWT.NONE);
		buttonCancel.setText("취소(&C)");
		buttonCancel
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					@Override
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						select = Action.CANCEL;

						sShell.dispose();
					}
				});
		buttonCancel.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			@Override
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					// ESC 키를 누른 경우

					select = Action.CANCEL;

					sShell.dispose();
				} else if (e.keyCode == SWT.ARROW_RIGHT) {
					buttonAdd.setFocus();
				}
			}
		});

		// 추가
		buttonAdd = new Button(composite, SWT.NONE);
		buttonAdd.setText("추가(&A)");
		buttonAdd
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					@Override
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						select = Action.ADD;

						sShell.dispose();
					}
				});
		buttonAdd.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			@Override
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					// ESC 키를 누른 경우

					select = Action.CANCEL;

					sShell.dispose();
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					buttonCancel.setFocus();
				} else if (e.keyCode == SWT.ARROW_RIGHT) {
					buttonOpen.setFocus();
				}
			}
		});

		// 열기
		buttonOpen = new Button(composite, SWT.NONE);
		buttonOpen.setText("열기(&O)");
		buttonOpen
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					@Override
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						select = Action.OPEN;

						sShell.dispose();
					}
				});
		buttonOpen.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			@Override
			public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					// ESC 키를 누른 경우

					select = Action.CANCEL;

					sShell.dispose();
				} else if (e.keyCode == SWT.ARROW_LEFT) {
					buttonAdd.setFocus();
				}
			}
		});
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		sShell.setImage(JZip.jzipImage);
		sShell.setText("이 파일을 어떻게 처리할까요?");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.verticalSpacing = 10;
		gridLayout.horizontalSpacing = 10;
		gridLayout.makeColumnsEqualWidth = false;
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(360, 80));

		GridData gridData;

		icon = new Label(sShell, SWT.NONE);
		icon.setImage(sShell.getDisplay().getSystemImage(SWT.ICON_QUESTION));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.verticalAlignment = GridData.CENTER;
		icon.setLayoutData(gridData);

		label = new Label(sShell, SWT.HORIZONTAL | SWT.WRAP);
		gridData = new GridData();
		gridData.widthHint = 340;
		gridData.heightHint = -1;
		label.setLayoutData(gridData);

		createComposite();

		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			@Override
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				select = Action.CANCEL;
			}
		});
	}

	/**
	 * {@link ActionSelectDialog}를 화면에 표시하는 메소드
	 * 
	 * @return 선택된 동작
	 */
	public Action open() {
		buttonCancel.setFocus();

		sShell.open();
		Display display = sShell.getDisplay();
		while (!sShell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();

		return select;
	}
}
