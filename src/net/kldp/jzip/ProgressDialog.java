/**
 * ProgressDialog : 진행 상황 표시 대화상자 
 */
package net.kldp.jzip;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Seungwon Jeong
 * 
 */
public class ProgressDialog {

	public static enum ProgressMode {
		ARCHIVE, EXTRACT,
	}

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="0,0"

	private ProgressBar progressBar = null;

	private Label label = null;

	public ProgressDialog(Shell parent, ProgressMode mode, int maximum) {
		createSShell();

		sShell.setParent(parent);

		switch (mode) {
		case ARCHIVE:
			sShell.setText("압축하는 중...");
			label.setText("압축하는 중입니다.");
			break;

		case EXTRACT:
			sShell.setText("압축을 푸는 중...");
			label.setText("압축을 푸는 중입니다.");
			break;
		}

		progressBar.setMaximum(maximum);

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

	public void close() {
		sShell.dispose();
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.heightHint = -1;
		gridData.widthHint = 300;
		gridData.verticalAlignment = GridData.CENTER;

		GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 10;
		gridLayout.verticalSpacing = 10;

		sShell = new Shell(SWT.NO_TRIM | SWT.APPLICATION_MODAL);
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(310, 54));

		label = new Label(sShell, SWT.NONE);

		progressBar = new ProgressBar(sShell, SWT.NONE);
		progressBar.setLayoutData(gridData);
	}

	public void open() {
		sShell.open();
	}

	public void update(int selection) {
		progressBar.setSelection(selection);
	}
}
