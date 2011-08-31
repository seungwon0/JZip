/**
 * JZip
 * 
 * 이 프로그램은 Zip 파일 포맷을 지원하는 압축 프로그램입니다.
 * 
 * 이 프로그램은 SWT(Standard Widget Toolkit)와 Apache Ant를 사용합니다.
 * 그리고 Tango 아이콘 테마와 File Roller의 아이콘을 사용합니다.
 * 
 * 저작권 : GNU General Public License
 * 홈페이지 :  https://github.com/seungwon0/JZip
 * 개발자 : 정승원 (seungwon0@gmail.com)
 */
package com.github.seungwon0.JZip;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Stack;

import org.apache.tools.zip.ZipFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * JZip 프로그램의 메인 클래스
 * 
 * @author Seungwon Jeong
 * 
 */
public class JZip {

	public static final File tmpDir = new File(
			System.getProperty("java.io.tmpdir"), "JZip");

	public static Image jzipImage;

	/**
	 * 임시 디렉토리를 생성하는 메소드
	 */
	public static void createTmpDir() {
		if (tmpDir.isDirectory())
			return;

		if (tmpDir.isFile())
			tmpDir.delete();

		tmpDir.mkdirs();
	}

	/**
	 * 디렉토리를 삭제하는 메소드 (모든 하위 디렉토리와 파일까지 삭제함)
	 * 
	 * @param dirFile
	 *            삭제할 디렉토리 {@link File}
	 */
	public static void deleteDir(File dirFile) {
		for (File file : dirFile.listFiles())
			if (file.isDirectory())
				deleteDir(file);
			else
				file.delete();

		dirFile.delete();
	}

	/**
	 * JZip 프로그램의 main 메소드
	 * 
	 * @param args
	 *            열어야할 Zip 파일 이름 혹은 압축할 파일들
	 */
	public static void main(String[] args) {
		/*
		 * Before this is run, be sure to set up the launch configuration
		 * (Arguments->VM Arguments) for the correct SWT library path in order
		 * to run with the SWT dlls. The dlls are located in the SWT plugin jar.
		 * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
		 * installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
		 */
		Display display = Display.getDefault();

		JZip thisClass = new JZip();
		thisClass.createSShell();
		thisClass.setDnd(); // DnD 기능 설정
		thisClass.setContextMenu(); // 문맥 메뉴 설정

		thisClass.sShell.open();

		thisClass.sShell.layout();

		if (args.length == 1) {
			if (thisClass.canOpen(args[0]))
				thisClass.open(args[0]);
			else
				thisClass.createNewArchive(args);
		} else if (args.length >= 2) {
			thisClass.createNewArchive(args);
		}

		while (!thisClass.sShell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
		display.dispose();

		// 임시 디렉토리 삭제
		if (tmpDir.isDirectory())
			deleteDir(tmpDir);
	}

	private Shell sShell;

	private final String jzip;
	private final String defaultPath;

	private Menu menuBar;
	private Menu submenuFile;
	private Menu submenuEdit;
	private Menu submenuView;
	private Menu submenuHelp;
	private Menu submenuAlignment;
	private Menu submenuFormat;
	private Menu submenuOpenRecent;

	private MenuItem radioFile;
	private MenuItem radioDir;
	private MenuItem pushRefresh;
	private MenuItem pushClose;
	private MenuItem radioLong;
	private MenuItem radioShort;
	private MenuItem pushProperty;
	private MenuItem radioUtf8;
	private MenuItem pushSelectAll;
	private MenuItem pushDeselectAll;
	private MenuItem pushAddFile;
	private MenuItem pushAddDir;
	private MenuItem pushRename;
	private MenuItem pushDel;
	private MenuItem radioMs949;
	private MenuItem pushSave;
	private MenuItem pushExtract;
	private MenuItem radioMid;
	private MenuItem radioName;
	private MenuItem radioSize;
	private MenuItem radioType;
	private MenuItem radioTime;
	private MenuItem radioPath;
	private MenuItem checkReverse;
	private MenuItem checkTree;
	private MenuItem contextPushView;
	private MenuItem contextPushOpen;
	private MenuItem contextPushExtract;
	private MenuItem contextPushRename;
	private MenuItem contextPushDel;
	private MenuItem contextPushOpenDir;
	private MenuItem submenuItemOpenRecent;

	private Zip zip;

	private Table table;

	private TableColumn tableColumnName;
	private TableColumn tableColumnSize;
	private TableColumn tableColumnType;
	private TableColumn tableColumnTime;
	private TableColumn tableColumnPath;

	private ToolBar toolBar;

	private ToolItem toolItemExtract;
	private ToolItem toolItemAddFile;
	private ToolItem toolItemAddDir;

	private Composite dirComposite;
	private Composite contentsComposite;

	private Button buttonPrev;
	private Button buttonNext;
	private Button buttonUp;
	private Button buttonHome;

	private Label compositeSeparator;
	private Label labelPath;
	private Label statusSeparator;
	private Label statusLine;

	private Text text;

	private Tree tree;

	private Sash sash;

	private Stack<String> prevStack;
	private Stack<String> nextStack;

	/**
	 * {@link JZip} 클래스의 생성자
	 */
	public JZip() {
		jzip = "JZip 0.9.2";

		defaultPath = System.getProperty("user.home");
	}

	/**
	 * 압축 파일에 디렉토리를 더하는 메소드
	 */
	private void addDir() {
		if (!zip.canWrite()) {
			// Zip 파일에 대한 쓰기 권한이 없는 경우

			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("디렉토리 더하기 실패");
			messageBox.setMessage(zip.getFileName() + " 파일에 대한 쓰기 권한이 없습니다.");
			messageBox.open();

			return;
		}

		// 디렉토리 선택 대화상자
		DirectoryDialog directoryDialog = new DirectoryDialog(sShell, SWT.OPEN);
		directoryDialog.setText("압축 파일에 더할 디렉토리를 선택하세요.");
		directoryDialog.setMessage("압축 파일에 더할 디렉토리를 선택하세요.\n"
				+ "모든 하위 디렉토리와 디렉토리 안의 파일들도 다 더합니다.");
		directoryDialog.setFilterPath(defaultPath);
		addDir(directoryDialog.open());
	}

	/**
	 * 압축 파일에 디렉토리를 더하는 메소드
	 * 
	 * @param directoryName
	 *            더할 디렉토리명
	 */
	private void addDir(String directoryName) {
		if (directoryName == null)
			return;

		// 더할 디렉토리 File
		File directory = new File(directoryName);

		if (!directory.exists()) {
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("디렉토리 더하기 실패!");
			messageBox.setMessage(directory.getPath() + " 디렉토리가 존재하지 않습니다.");
			messageBox.open();

			return;
		}

		if (directory.isFile()) {
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("디렉토리 더하기 실패!");
			messageBox
					.setMessage(directory.getPath() + " 파일은 디렉토리가 아니라 파일입니다.");
			messageBox.open();

			return;
		}

		if (!directory.canRead()) {
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("디렉토리 더하기 실패!");
			messageBox.setMessage(directory.getPath()
					+ " 디렉토리에 대한 읽기 권한이 없습니다.");
			messageBox.open();

			return;
		}

		setStatusLine("압축 파일에 디렉토리를 더하는 중입니다.");

		zip.addDir(sShell, directory);

		updateContents();
	}

	/**
	 * 압축 파일에 파일을 더하는 메소드
	 */
	private void addFile() {
		if (!zip.canWrite()) {
			// 쓰기 권한이 없는 경우
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("파일 더하기 실패");
			messageBox.setMessage(zip.getFileName() + " 파일에 대한 쓰기 권한이 없습니다.");
			messageBox.open();

			return;
		}

		// 파일 선택 대화상자
		FileDialog dialog = new FileDialog(sShell, SWT.OPEN | SWT.MULTI);
		dialog.setText("압축 파일에 더할 파일을 선택하세요.");
		dialog.setFilterPath(defaultPath);

		if (dialog.open() != null) {
			final String parentName = dialog.getFilterPath();
			final String[] fileNames = dialog.getFileNames();

			String[] filePaths = new String[fileNames.length];
			for (int i = 0; i < filePaths.length; i++)
				filePaths[i] = new File(parentName, fileNames[i])
						.getAbsolutePath();

			setStatusLine("압축 파일에 파일을 더하는 중입니다.");

			zip.addFile(sShell, filePaths);

			updateContents();
		}
	}

	/**
	 * 열 수 있는 파일인지 확인하는 메소드
	 * 
	 * @param fileName
	 *            확인할 파일명
	 * @return 열 수 있는지 여부
	 */
	private boolean canOpen(String fileName) {
		if (fileName == null)
			return false;

		File file = new File(fileName);

		if (!file.exists())
			return false;

		if (file.isDirectory())
			return false;

		if (!file.canRead())
			return false;

		try {
			new ZipFile(file);

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 현재 열려있는 압축 파일을 닫는 메소드
	 */
	private void close() {
		zip = null;

		prevStack = null;
		nextStack = null;

		sShell.setText(jzip);

		updateContents();
	}

	/**
	 * contentsComposite를 생성하는 메소드
	 */
	private void createContentsComposite() {
		contentsComposite = new Composite(sShell, SWT.NONE);
		contentsComposite.setLayout(new FormLayout());
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		contentsComposite.setLayoutData(gridData);

		createTree();

		// 테이블
		table = new Table(contentsComposite, SWT.MULTI | SWT.VIRTUAL
				| SWT.BORDER | SWT.FULL_SELECTION);
		final Display display = sShell.getDisplay();
		table.setHeaderVisible(true);
		table.setVisible(false);
		table.setLinesVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				final TableItem item = (TableItem) e.item;
				final int index = table.indexOf(item);

				if (zip.isDirecotry(index)) {
					if (radioDir.getSelection()) {
						// 디렉토리로 보기인 경우
						prevStack.add(zip.getPath());
						nextStack.clear();
						zip.openDir(index);

						updateContents();
					}
				} else {
					zip.openFile(index);
				}
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateMenu();

				updateStatusLine();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (radioDir.getSelection() && zip != null) {
					if (e.stateMask == SWT.ALT && e.keyCode == SWT.ARROW_UP)
						goToParent();
					else if (e.keyCode == SWT.BS)
						goToHome();
					else if (e.stateMask == SWT.ALT && e.keyCode == SWT.HOME)
						goToHome();
					else if (e.stateMask == SWT.ALT
							&& e.keyCode == SWT.ARROW_LEFT)
						goPrev();
					else if (e.stateMask == SWT.ALT
							&& e.keyCode == SWT.ARROW_RIGHT)
						goNext();
				}
			}
		});
		table.addListener(SWT.SetData, new Listener() {
			private final Image folderImage = new Image(display, getClass()
					.getResourceAsStream("/icons/16x16/folder.png"));
			private final Image imageImage = new Image(display, getClass()
					.getResourceAsStream("/icons/16x16/image-x-generic.png"));
			private final Image audioImage = new Image(display, getClass()
					.getResourceAsStream("/icons/16x16/audio-x-generic.png"));
			private final Image videoImage = new Image(display, getClass()
					.getResourceAsStream("/icons/16x16/video-x-generic.png"));
			private final Image textImage = new Image(display, getClass()
					.getResourceAsStream("/icons/16x16/text-x-generic.png"));
			private final Image htmlImage = new Image(display, getClass()
					.getResourceAsStream("/icons/16x16/text-html.png"));
			private final Image archiveImage = new Image(display, getClass()
					.getResourceAsStream("/icons/16x16/package-x-generic.png"));

			public void handleEvent(Event event) {
				final TableItem item = (TableItem) event.item;
				final int index = table.indexOf(item);

				int dateFormat = DateFormat.MEDIUM;
				if (radioShort.getSelection())
					dateFormat = DateFormat.SHORT;
				else if (radioLong.getSelection())
					dateFormat = DateFormat.LONG;

				item.setText(zip.getStrings(index, dateFormat));

				final String type = item.getText(2);

				if (type.equals("디렉토리"))
					item.setImage(folderImage);
				else if (type.equals("그림 파일"))
					item.setImage(imageImage);
				else if (type.equals("음악 파일"))
					item.setImage(audioImage);
				else if (type.equals("동영상 파일"))
					item.setImage(videoImage);
				else if (type.equals("텍스트 파일"))
					item.setImage(textImage);
				else if (type.equals("HTML 파일"))
					item.setImage(htmlImage);
				else if (type.equals("압축 파일"))
					item.setImage(archiveImage);
				else
					item.setImage(textImage);
			}
		});

		// 이름
		tableColumnName = new TableColumn(table, SWT.NONE);
		tableColumnName.setWidth(260);
		tableColumnName.setText("이름");
		tableColumnName.setToolTipText("이름순으로 정렬합니다.");
		tableColumnName.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final boolean reverse = (table.getSortColumn() == tableColumnName && table
						.getSortDirection() == SWT.DOWN);

				radioName.setSelection(true);
				radioSize.setSelection(false);
				radioType.setSelection(false);
				radioTime.setSelection(false);
				radioPath.setSelection(false);

				checkReverse.setSelection(reverse);

				updateContents();

				table.setSortColumn(tableColumnName);
				table.setSortDirection(reverse ? SWT.UP : SWT.DOWN);
			}
		});

		// 크기
		tableColumnSize = new TableColumn(table, SWT.RIGHT);
		tableColumnSize.setWidth(80);
		tableColumnSize.setText("크기");
		tableColumnSize.setToolTipText("크기순으로 정렬합니다.");
		tableColumnSize.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final boolean reverse = (table.getSortColumn() == tableColumnSize && table
						.getSortDirection() == SWT.DOWN);

				radioName.setSelection(false);
				radioSize.setSelection(true);
				radioType.setSelection(false);
				radioTime.setSelection(false);
				radioPath.setSelection(false);

				checkReverse.setSelection(reverse);

				updateContents();

				table.setSortColumn(tableColumnSize);
				table.setSortDirection(reverse ? SWT.UP : SWT.DOWN);
			}
		});

		// 형식
		tableColumnType = new TableColumn(table, SWT.RIGHT);
		tableColumnType.setWidth(80);
		tableColumnType.setText("형식");
		tableColumnType.setToolTipText("형식순으로 정렬합니다.");
		tableColumnType.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final boolean reverse = (table.getSortColumn() == tableColumnType && table
						.getSortDirection() == SWT.DOWN);

				radioName.setSelection(false);
				radioSize.setSelection(false);
				radioType.setSelection(true);
				radioTime.setSelection(false);
				radioPath.setSelection(false);

				checkReverse.setSelection(reverse);

				updateContents();

				table.setSortColumn(tableColumnType);
				table.setSortDirection(reverse ? SWT.UP : SWT.DOWN);
			}
		});

		// 바뀐 시간
		tableColumnTime = new TableColumn(table, SWT.NONE);
		tableColumnTime.setWidth(200);
		tableColumnTime.setText("바뀐 시간");
		tableColumnTime.setToolTipText("바뀐 시간순으로 정렬합니다.");
		tableColumnTime.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final boolean reverse = (table.getSortColumn() == tableColumnTime && table
						.getSortDirection() == SWT.DOWN);

				radioName.setSelection(false);
				radioSize.setSelection(false);
				radioType.setSelection(false);
				radioTime.setSelection(true);
				radioPath.setSelection(false);

				checkReverse.setSelection(reverse);

				updateContents();

				table.setSortColumn(tableColumnTime);
				table.setSortDirection(reverse ? SWT.UP : SWT.DOWN);
			}
		});

		// 경로
		createTablecolumnPath();

		// 레이아웃 설정
		setContentsLayout();
	}

	/**
	 * dirComposite을 생성하는 메소드
	 */
	private void createDirComposite() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;

		GridData gridData1 = new GridData();
		gridData1.heightHint = 0;
		gridData1.verticalAlignment = GridData.FILL;
		gridData1.horizontalAlignment = GridData.BEGINNING;

		GridData gridData2 = new GridData();
		gridData2.widthHint = 300;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 7;

		dirComposite = new Composite(sShell, SWT.NONE);
		dirComposite.setLayout(gridLayout);
		dirComposite.setLayoutData(gridData);

		final Display display = sShell.getDisplay();

		// 뒤로
		buttonPrev = new Button(dirComposite, SWT.NONE);
		buttonPrev.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/go-previous.png")));
		buttonPrev.setText("뒤로");
		buttonPrev.setToolTipText("이전 디렉토리로 이동합니다. (Alt+Left)");
		buttonPrev.setEnabled(false);
		buttonPrev.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goPrev();
			}
		});

		// 앞으로
		buttonNext = new Button(dirComposite, SWT.NONE);
		buttonNext.setText("앞으로");
		buttonNext.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/go-next.png")));
		buttonNext.setToolTipText("다음 디렉토리로 이동합니다. (Alt+Right)");
		buttonNext.setEnabled(false);
		buttonNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				goNext();
			}
		});

		// 위로
		buttonUp = new Button(dirComposite, SWT.NONE);
		buttonUp.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/go-up.png")));
		buttonUp.setText("위로");
		buttonUp.setToolTipText("상위 디렉토리로 이동합니다. (Alt+Up, Back Space)");
		buttonUp.setEnabled(false);
		buttonUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				goToParent();
			}
		});

		// 홈으로
		buttonHome = new Button(dirComposite, SWT.NONE);
		buttonHome.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/go-home.png")));
		buttonHome.setText("홈으로");
		buttonHome.setToolTipText("홈으로 이동합니다. (Alt+HOME)");
		buttonHome.setEnabled(false);
		buttonHome.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				goToHome();
			}
		});

		compositeSeparator = new Label(dirComposite, SWT.SEPARATOR);
		compositeSeparator.setEnabled(false);
		compositeSeparator.setLayoutData(gridData1);

		// 위치
		labelPath = new Label(dirComposite, SWT.NONE);
		labelPath.setText("위치 : ");
		labelPath.setEnabled(false);

		text = new Text(dirComposite, SWT.BORDER);
		text.setToolTipText("압축 파일 내의 현재 위치");
		text.setEnabled(false);
		text.setLayoutData(gridData2);
		text.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				goTo(text.getText().trim());
			}
		});
		text.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				pushDel.setAccelerator(0);
			}

			public void focusLost(FocusEvent e) {
				pushDel.setAccelerator(SWT.DEL);
			}
		});
	}

	/**
	 * 메뉴를 생성하는 메소드
	 */
	private void createMenu() {
		// 메뉴 막대
		menuBar = new Menu(sShell, SWT.BAR);

		final Display display = sShell.getDisplay();

		// 압축 파일
		MenuItem submenuItemFile = new MenuItem(menuBar, SWT.CASCADE);
		submenuItemFile.setText("압축 파일(&A)");

		submenuFile = new Menu(submenuItemFile);
		submenuFile.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent e) {
				updateStatusLine();
			}

			public void menuShown(MenuEvent e) {
			}
		});

		submenuItemFile.setMenu(submenuFile);

		// 새로 만들기
		MenuItem pushNew = new MenuItem(submenuFile, SWT.PUSH);
		pushNew.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/document-new.png")));
		pushNew.setText("새로 만들기(&N)...\tCtrl+N");
		pushNew.setAccelerator(SWT.CTRL | 'N');
		pushNew.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				createNew();
			}
		});
		pushNew.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("새로운 압축 파일을 생성합니다.");
			}
		});

		// 열기
		MenuItem pushOpen = new MenuItem(submenuFile, SWT.PUSH);
		pushOpen.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/document-open.png")));
		pushOpen.setText("열기(&O)...\tCtrl+O");
		pushOpen.setAccelerator(SWT.CTRL | 'O');
		pushOpen.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				open();
			}
		});
		pushOpen.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("기존 압축 파일을 엽니다.");
			}
		});

		// 최근 파일 열기
		submenuItemOpenRecent = new MenuItem(submenuFile, SWT.CASCADE);
		submenuItemOpenRecent.setText("최근 파일 열기...");
		submenuItemOpenRecent.setEnabled(false);
		submenuOpenRecent = new Menu(submenuItemOpenRecent);
		submenuItemOpenRecent.setMenu(submenuOpenRecent);
		submenuItemOpenRecent.addArmListener(new ArmListener() {

			public void widgetArmed(ArmEvent e) {
				setStatusLine("최근에 연 압축 파일을 엽니다.");
			}

		});

		// 다른 이름으로 저장
		pushSave = new MenuItem(submenuFile, SWT.PUSH);
		pushSave.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/document-save-as.png")));
		pushSave.setText("다른 이름으로 저장(&S)...\tCtrl+S");
		pushSave.setAccelerator(SWT.CTRL | 'S');
		pushSave.setEnabled(false);
		pushSave.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				saveAs();
			}
		});
		pushSave.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 항목을 다른 이름으로 저장합니다.");
			}
		});

		// 압축 풀기
		pushExtract = new MenuItem(submenuFile, SWT.PUSH);
		pushExtract.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/extract-archive.png")));
		pushExtract.setText("압축 풀기(&E)...\tCtrl+E");
		pushExtract.setAccelerator(SWT.CTRL | 'E');
		pushExtract.setEnabled(false);
		pushExtract.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				extract();
			}
		});
		pushExtract.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 항목을 압축 해제합니다.");
			}
		});

		// 속성
		pushProperty = new MenuItem(submenuFile, SWT.PUSH);
		pushProperty.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/16x16/document-properties.png")));
		pushProperty.setText("속성(&P)...");
		pushProperty.setEnabled(false);
		pushProperty.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int dateFormat = DateFormat.MEDIUM;
				if (radioLong.getSelection()) {
					dateFormat = DateFormat.SHORT;
				} else if (radioShort.getSelection()) {
					dateFormat = DateFormat.LONG;
				}

				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(2);

				long length = zip.getLength();
				long originalLength = zip.getOriginalLength();
				double ratio = (double) originalLength / length;

				StringBuilder message = new StringBuilder(70);
				message.append("파일 이름 : " + zip.getFileName());
				message.append("\n파일 경로 : " + zip.getFileParentPath());
				message.append("\n바뀐 시간 : "
						+ Zip.getTimeString(zip.lastModified(), dateFormat));
				message.append("\n압축 크기 : " + Zip.getSizeString(length));
				message.append("\n실제 크기 : " + Zip.getSizeString(originalLength));
				message.append("\n압축 정도 : " + nf.format(ratio));
				message.append("\n항목 개수 : " + zip.getOriginalSize());

				MessageBox property = new MessageBox(sShell, SWT.OK
						| SWT.ICON_INFORMATION);
				property.setText(zip.getFileName() + " 속성");
				property.setMessage(message.toString());
				property.open();
			}
		});
		pushProperty.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("압축 파일의 속성을 보여줍니다.");
			}
		});

		// 닫기
		pushClose = new MenuItem(submenuFile, SWT.PUSH);
		pushClose.setText("닫기(&C)");
		pushClose.setEnabled(false);
		pushClose.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// 압축 파일 닫기
				close();
			}
		});
		pushClose.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("압축 파일을 닫습니다.");
			}
		});

		new MenuItem(submenuFile, SWT.SEPARATOR);

		// 프로그램 종료
		MenuItem pushQuit = new MenuItem(submenuFile, SWT.PUSH);
		pushQuit.setText("프로그램 종료(&Q)\tCtrl+Q");
		pushQuit.setAccelerator(SWT.CTRL | 'Q');
		pushQuit.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// 프로그램 종료
				sShell.dispose();
			}
		});
		pushQuit.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("프로그램을 종료합니다.");
			}
		});

		// 편집
		MenuItem submenuItemEdit = new MenuItem(menuBar, SWT.CASCADE);
		submenuItemEdit.setText("편집(&E)");

		submenuEdit = new Menu(submenuItemEdit);
		submenuEdit.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent e) {
				updateStatusLine();
			}

			public void menuShown(MenuEvent e) {
			}
		});

		submenuItemEdit.setMenu(submenuEdit);

		// 파일 더하기
		pushAddFile = new MenuItem(submenuEdit, SWT.PUSH);
		pushAddFile.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/add-files-to-archive.png")));
		pushAddFile.setText("파일 더하기(&F)...");
		pushAddFile.setEnabled(false);
		pushAddFile.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				addFile();
			}
		});
		pushAddFile.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("압축 파일에 새로운 파일을 더합니다.");
			}
		});

		// 디렉토리 더하기
		pushAddDir = new MenuItem(submenuEdit, SWT.PUSH);
		pushAddDir.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/add-folder-to-archive.png")));
		pushAddDir.setText("디렉토리 더하기(&D)...");
		pushAddDir.setEnabled(false);
		pushAddDir.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				addDir();
			}
		});
		pushAddDir.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("압축 파일에 새로운 디렉토리를 더합니다.");
			}
		});

		new MenuItem(submenuEdit, SWT.SEPARATOR);

		// 이름 바꾸기
		pushRename = new MenuItem(submenuEdit, SWT.PUSH);
		pushRename.setText("이름 바꾸기(&R)...\tF2");
		pushRename.setAccelerator(SWT.F2);
		pushRename.setEnabled(false);
		pushRename.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				rename();
			}
		});
		pushRename.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 항목의 이름을 변경합니다.");
			}
		});

		// 지우기
		pushDel = new MenuItem(submenuEdit, SWT.PUSH);
		pushDel.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/edit-delete.png")));
		pushDel.setText("지우기(&D)...\tDel");
		pushDel.setAccelerator(SWT.DEL);
		pushDel.setEnabled(false);
		pushDel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				delete();
			}
		});
		pushDel.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 항목을 삭제합니다.");
			}
		});

		new MenuItem(submenuEdit, SWT.SEPARATOR);

		// 모두 선택
		pushSelectAll = new MenuItem(submenuEdit, SWT.PUSH);
		pushSelectAll.setText("모두 선택(&A)\tCtrl+A");
		pushSelectAll.setEnabled(false);
		pushSelectAll.setAccelerator(SWT.CTRL | 'A');
		pushSelectAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				table.selectAll();

				updateMenu();

				updateStatusLine();
			}
		});
		pushSelectAll.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("모든 항목을 선택합니다.");
			}
		});

		// 모두 선택 해제
		pushDeselectAll = new MenuItem(submenuEdit, SWT.PUSH);
		pushDeselectAll.setText("모두 선택 해제(&U)");
		pushDeselectAll.setEnabled(false);
		pushDeselectAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				table.deselectAll();

				updateMenu();

				updateStatusLine();
			}
		});
		pushDeselectAll.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("모든 항목을 선택 해제합니다.");
			}
		});

		// 보기
		MenuItem submenuItemView = new MenuItem(menuBar, SWT.CASCADE);
		submenuItemView.setText("보기(&V)");

		submenuView = new Menu(submenuItemView);
		submenuView.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent e) {
				updateStatusLine();
			}

			public void menuShown(MenuEvent e) {
			}
		});

		submenuItemView.setMenu(submenuView);

		// 새로 고침
		pushRefresh = new MenuItem(submenuView, SWT.PUSH);
		pushRefresh.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/view-refresh.png")));
		pushRefresh.setText("새로 고침(&R)\tF5");
		pushRefresh.setEnabled(false);
		pushRefresh.setAccelerator(SWT.F5);
		pushRefresh.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				open(zip.getFilePath());
			}
		});
		pushRefresh.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("현재 열려있는 압축 파일을 다시 불러옵니다.");
			}
		});

		new MenuItem(submenuView, SWT.SEPARATOR);

		// MS949
		radioMs949 = new MenuItem(submenuView, SWT.RADIO);
		radioMs949.setText("&MS949 (윈도우)");
		radioMs949.setSelection(true);
		radioMs949.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioMs949.getSelection())
					open(zip.getFilePath());
			}
		});
		radioMs949.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("인코딩을 MS949(윈도우)로 변경합니다.");
			}
		});

		// UTF-8
		radioUtf8 = new MenuItem(submenuView, SWT.RADIO);
		radioUtf8.setText("&UTF-8 (리눅스)");
		radioUtf8.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioUtf8.getSelection())
					open(zip.getFilePath());
			}
		});
		radioUtf8.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("인코딩을 UTF-8(리눅스)로 변경합니다.");
			}
		});

		new MenuItem(submenuView, SWT.SEPARATOR);

		// 도구 모음
		final MenuItem checkToolBar = new MenuItem(submenuView, SWT.CHECK);
		checkToolBar.setText("도구 모음(&T)");
		checkToolBar.setSelection(true);
		checkToolBar.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (checkToolBar.getSelection()) {
					// 도구 모음 보이기
					if (toolBar.isDisposed()) {
						createToolBar();

						updateMenu();

						toolBar.moveAbove(null);

						sShell.layout();
					}
				} else {
					// 도구 모음 숨기기
					if (!toolBar.isDisposed()) {
						toolBar.dispose();

						sShell.layout();
					}
				}
			}
		});
		checkToolBar.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("도구 모음을 보이거나 감춥니다.");
			}
		});

		// 상태 표시줄
		final MenuItem checkStatusLine = new MenuItem(submenuView, SWT.CHECK);
		checkStatusLine.setText("상태 표시줄(&S)");
		checkStatusLine.setSelection(true);
		checkStatusLine.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (checkStatusLine.getSelection()) {
					// 상태 표시줄 보이기
					createStatusLine();

					sShell.layout();
				} else {
					// 상태 표시줄 숨기기
					if (!statusSeparator.isDisposed())
						statusSeparator.dispose();

					if (!statusLine.isDisposed())
						statusLine.dispose();

					sShell.layout();
				}
			}
		});
		checkStatusLine.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("상태 표시줄을 보이거나 감춥니다.");
			}
		});

		// 디렉토리 구조
		checkTree = new MenuItem(submenuView, SWT.CHECK);
		checkTree.setText("디렉토리 구조\tF9");
		checkTree.setAccelerator(SWT.F9);
		checkTree.setSelection(true);
		checkTree.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (checkTree.getSelection()) {
					createTree();
					setContentsLayout();
					contentsComposite.layout();
					updateTree();
				} else {
					// 디렉토리 구조 숨기기
					delTree();
				}
			}
		});
		checkTree.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("디렉토리 구조를 보이거나 감춥니다.");
			}
		});

		new MenuItem(submenuView, SWT.SEPARATOR);

		// 항목 정렬
		MenuItem submenuItemAlignment = new MenuItem(submenuView, SWT.CASCADE);
		submenuItemAlignment.setText("항목 정렬(&A)");
		submenuAlignment = new Menu(submenuItemAlignment);
		submenuItemAlignment.setMenu(submenuAlignment);
		submenuItemAlignment.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("항목을 정렬합니다.");
			}
		});

		// 이름으로 정렬
		radioName = new MenuItem(submenuAlignment, SWT.RADIO);
		radioName.setText("이름으로 정렬(&N)");
		radioName.setSelection(true);
		radioName.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioName.getSelection()) {
					updateContents();

					table.setSortColumn(tableColumnName);

					if (checkReverse.getSelection())
						table.setSortDirection(SWT.UP);
					else
						table.setSortDirection(SWT.DOWN);
				}
			}
		});
		radioName.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("이름으로 정렬합니다.");
			}
		});

		// 크기로 정렬
		radioSize = new MenuItem(submenuAlignment, SWT.RADIO);
		radioSize.setText("크기로 정렬(&S)");
		radioSize.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioSize.getSelection()) {
					updateContents();

					table.setSortColumn(tableColumnSize);

					if (checkReverse.getSelection())
						table.setSortDirection(SWT.UP);
					else
						table.setSortDirection(SWT.DOWN);
				}
			}
		});
		radioSize.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("크기로 정렬합니다.");
			}
		});

		// 형식으로 정렬
		radioType = new MenuItem(submenuAlignment, SWT.RADIO);
		radioType.setText("형식으로 정렬(&T)");
		radioType.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioType.getSelection()) {
					updateContents();

					table.setSortColumn(tableColumnType);

					if (checkReverse.getSelection())
						table.setSortDirection(SWT.UP);
					else
						table.setSortDirection(SWT.DOWN);
				}
			}
		});
		radioType.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("형식으로 정렬합니다.");
			}
		});

		// 바뀐 시간으로 정렬
		radioTime = new MenuItem(submenuAlignment, SWT.RADIO);
		radioTime.setText("바뀐 시간으로 정렬(&D)");
		radioTime.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioTime.getSelection()) {
					updateContents();

					table.setSortColumn(tableColumnTime);

					if (checkReverse.getSelection())
						table.setSortDirection(SWT.UP);
					else
						table.setSortDirection(SWT.DOWN);
				}
			}
		});
		radioTime.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("바뀐 시간으로 정렬합니다.");
			}
		});

		// 위치로 정렬
		radioPath = new MenuItem(submenuAlignment, SWT.RADIO);
		radioPath.setText("위치로 정렬(&P)");
		radioPath.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioPath.getSelection()) {
					if (!tableColumnPath.isDisposed()) {
						updateContents();

						table.setSortColumn(tableColumnPath);

						if (checkReverse.getSelection())
							table.setSortDirection(SWT.UP);
						else
							table.setSortDirection(SWT.DOWN);
					}
				}
			}
		});
		radioPath.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("위치로 정렬합니다.");
			}
		});

		new MenuItem(submenuAlignment, SWT.SEPARATOR);

		// 역순으로 정렬
		checkReverse = new MenuItem(submenuAlignment, SWT.CHECK);
		checkReverse.setText("역순으로 정렬(&R)");
		checkReverse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null) {
					updateContents();

					final TableColumn sortColumn = table.getSortColumn();
					if (sortColumn == null)
						table.setSortColumn(tableColumnName);
					else if (sortColumn == tableColumnName)
						table.setSortColumn(tableColumnName);
					else if (sortColumn == tableColumnSize)
						table.setSortColumn(tableColumnSize);
					else if (sortColumn == tableColumnType)
						table.setSortColumn(tableColumnType);
					else if (sortColumn == tableColumnTime)
						table.setSortColumn(tableColumnTime);
					else if (sortColumn == tableColumnPath)
						table.setSortColumn(tableColumnPath);
					else
						table.setSortColumn(tableColumnName);

					if (checkReverse.getSelection())
						table.setSortDirection(SWT.UP);
					else
						table.setSortDirection(SWT.DOWN);
				}
			}
		});
		checkReverse.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("거꾸로 정렬합니다.");
			}
		});

		// 바뀐 시간 출력 형식
		MenuItem submenuItemFormat = new MenuItem(submenuView, SWT.CASCADE);
		submenuItemFormat.setText("바뀐 시간 출력 형식(&F)");
		submenuFormat = new Menu(submenuItemFormat);
		submenuItemFormat.setMenu(submenuFormat);
		submenuItemFormat.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("바뀐 시간 출력 형식을 변경합니다.");
			}
		});

		// 간략하게
		radioShort = new MenuItem(submenuFormat, SWT.RADIO);
		radioShort.setText("간략하게(&S)");
		radioShort.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioShort.getSelection())
					updateContents();
			}
		});
		radioShort.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("바뀐 시간을 간략하게 출력합니다.");
			}
		});

		// 보통
		radioMid = new MenuItem(submenuFormat, SWT.RADIO);
		radioMid.setText("보통(&M)");
		radioMid.setSelection(true);
		radioMid.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioMid.getSelection())
					updateContents();
			}
		});
		radioMid.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("바뀐 시간의 출력 형식을 기본 값으로 변경합니다.");
			}
		});

		// 자세하게
		radioLong = new MenuItem(submenuFormat, SWT.RADIO);
		radioLong.setText("자세하게(&L)");
		radioLong.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (zip != null && radioLong.getSelection())
					updateContents();
			}
		});
		radioLong.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("바뀐 시간을 자세하게 출력합니다.");
			}
		});

		new MenuItem(submenuView, SWT.SEPARATOR);

		// 모든 파일 보기
		radioFile = new MenuItem(submenuView, SWT.RADIO);
		radioFile.setText("모든 파일 보기(&A)\tCtrl+1");
		radioFile.setAccelerator(SWT.CTRL | '1');
		radioFile.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (radioFile.getSelection()) {
					if (!dirComposite.isDisposed()) {
						dirComposite.dispose();

						sShell.layout();
					}

					delTree();
					checkTree.setEnabled(false);

					if (tableColumnPath.isDisposed())
						createTablecolumnPath();

					if (zip != null)
						open(zip.getFilePath());
				}
			}
		});
		radioFile.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("압축 파일 내의 모든 항목을 표시합니다.");
			}
		});

		// 디렉토리로 보기
		radioDir = new MenuItem(submenuView, SWT.RADIO);
		radioDir.setText("디렉토리로 보기(&D)\tCtrl+2");
		radioDir.setSelection(true);
		radioDir.setAccelerator(SWT.CTRL | '2');
		radioDir.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (radioDir.getSelection()) {
					if (dirComposite.isDisposed()) {
						createDirComposite();

						dirComposite.moveAbove(contentsComposite);

						sShell.layout();
					}

					checkTree.setEnabled(true);

					if (checkTree.getSelection()) {
						createTree();
						setContentsLayout();
						contentsComposite.layout();
						updateTree();
					}

					if (!tableColumnPath.isDisposed())
						tableColumnPath.dispose();

					if (zip != null)
						open(zip.getFilePath());
				}
			}
		});
		radioDir.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("압축 파일의 내용을 디렉토리 형태로 보여줍니다.");
			}
		});

		// 도움말
		MenuItem submenuItemHelp = new MenuItem(menuBar, SWT.CASCADE);
		submenuItemHelp.setText("도움말(&H)");

		submenuHelp = new Menu(submenuItemHelp);
		submenuHelp.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent e) {
				updateStatusLine();
			}

			public void menuShown(MenuEvent e) {
			}
		});

		submenuItemHelp.setMenu(submenuHelp);

		// 홈페이지 방문
		MenuItem pushHomePage = new MenuItem(submenuHelp, SWT.PUSH);
		pushHomePage.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/16x16/text-html.png")));
		pushHomePage.setText("홈페이지 방문(&V)");
		pushHomePage.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Program.launch("https://github.com/seungwon0/JZip");
			}
		});
		pushHomePage.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("프로그램 홈페이지를 방문합니다.");
			}
		});

		new MenuItem(submenuHelp, SWT.SEPARATOR);

		// JZip 정보
		MenuItem pushAbout = new MenuItem(submenuHelp, SWT.PUSH);
		pushAbout.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/16x16/dialog-information.png")));
		pushAbout.setText("JZip 정보(&A)...");
		pushAbout.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// JZip 정보
				MessageBox messageBox = new MessageBox(sShell, SWT.OK
						| SWT.ICON_INFORMATION);
				messageBox.setText("JZip 정보");

				StringBuilder message = new StringBuilder(50);
				message.append(jzip);
				message.append("\n\n이 프로그램은 Zip 파일 포맷을 지원하는 압축 프로그램입니다.");
				message.append("\n\n저작권 : GNU General Public License");
				message.append("\n홈페이지 : https://github.com/seungwon0/JZip");
				message.append("\n개발자 : 정승원 (seungwon0@gmail.com)");

				messageBox.setMessage(message.toString());
				messageBox.open();
			}
		});
		pushAbout.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("프로그램 정보를 보여줍니다.");
			}
		});

		sShell.setMenuBar(menuBar);
	}

	/**
	 * 새로운 압축 파일을 생성하는 메소드
	 */
	private void createNew() {
		// 새로운 압축 파일 선택 대화상자
		FileDialog fileDialog = new FileDialog(sShell, SWT.SAVE);
		fileDialog.setText("새로 만들 압축 파일을 선택하세요.");
		fileDialog.setFilterPath(defaultPath);
		fileDialog.setFilterExtensions(new String[] { "*.zip", "*.*" });
		fileDialog.setFilterNames(new String[] { "Zip 파일 (*.zip)",
				"모든 파일 (*.*)" });
		String fileName = fileDialog.open();

		if (fileName != null) {
			if (!fileName.toLowerCase().endsWith(".zip"))
				fileName += ".zip";

			if (createNew(fileName)) {
				// 인코딩 설정
				radioMs949.setSelection(true);
				radioUtf8.setSelection(false);

				open(fileName);
			}
		}
	}

	/**
	 * 새로운 압축 파일을 생성하는 메소드
	 * 
	 * @param fileName
	 *            압축 파일명
	 * @return 압축 성공 여부
	 */
	private boolean createNew(String fileName) {
		final File file = getSaveFile(fileName);

		if (file == null)
			// 압축 파일 저장 실패
			return false;

		Zip.createNew(file);

		// 압축 파일 저장 성공
		return true;
	}

	/**
	 * 주어진 파일들을 압축하여 새로운 압축 파일을 만드는 메소드
	 * 
	 * @param fileNames
	 *            압축할 파일명들
	 */
	private void createNewArchive(String[] fileNames) {
		MessageBox messageBox = new MessageBox(sShell, SWT.YES | SWT.NO
				| SWT.ICON_QUESTION);
		messageBox.setText("새로운 압축 파일을 생성할까요?");
		messageBox.setMessage("이 파일들로 새로운 압축 파일을 만들겠습니까?");

		if (messageBox.open() == SWT.YES) {
			createNew();

			setStatusLine("새로운 압축 파일을 생성하는 중입니다.");

			if (zip != null)
				zip.addFile(sShell, fileNames);

			updateContents();
		}
	}

	/**
	 * sShell을 생성하는 메소드
	 */
	private void createSShell() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 1;

		// Shell
		sShell = new Shell();
		jzipImage = new Image(sShell.getDisplay(), getClass()
				.getResourceAsStream("/icons/16x16/package-x-generic.png"));
		sShell.setImage(jzipImage);
		sShell.setText(jzip);
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(712, 460));

		createToolBar();

		createDirComposite();

		createContentsComposite();

		createStatusLine();

		createMenu();
	}

	/**
	 * 상태 표시줄을 생성하는 메소드
	 */
	private void createStatusLine() {
		if (statusSeparator == null || statusSeparator.isDisposed()) {
			statusSeparator = new Label(sShell, SWT.SEPARATOR | SWT.HORIZONTAL);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.verticalAlignment = GridData.CENTER;
			statusSeparator.setLayoutData(gridData);
		}

		if (statusLine == null || statusLine.isDisposed()) {
			statusLine = new Label(sShell, SWT.NONE);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.verticalAlignment = GridData.CENTER;
			statusLine.setLayoutData(gridData);
		}

		updateStatusLine();
	}

	/**
	 * 테이블에 위치 컬럼을 추가하는 메소드
	 */
	private void createTablecolumnPath() {
		// 위치
		tableColumnPath = new TableColumn(table, SWT.NONE);
		tableColumnPath.setWidth(200);
		tableColumnPath.setText("위치");
		tableColumnPath.setToolTipText("위치순으로 정렬합니다.");
		tableColumnPath.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final boolean reverse = (table.getSortColumn() == tableColumnPath && table
						.getSortDirection() == SWT.DOWN);

				radioName.setSelection(false);
				radioSize.setSelection(false);
				radioType.setSelection(false);
				radioTime.setSelection(false);
				radioPath.setSelection(true);

				checkReverse.setSelection(reverse);

				updateContents();

				table.setSortColumn(tableColumnPath);
				table.setSortDirection(reverse ? SWT.UP : SWT.DOWN);
			}
		});
	}

	/**
	 * 도구 모음을 생성하는 메소드
	 */
	private void createToolBar() {
		toolBar = new ToolBar(sShell, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.CENTER;
		toolBar.setLayoutData(gridData);

		final Display display = sShell.getDisplay();

		// 새로 만들기
		ToolItem toolItemNew = new ToolItem(toolBar, SWT.PUSH);
		toolItemNew.setImage(new Image(display, getClass().getResourceAsStream(
				"/icons/24x24/document-new.png")));
		toolItemNew.setText("새로 만들기");
		toolItemNew.setToolTipText("새로운 압축 파일을 생성합니다. (Ctrl+N)");
		toolItemNew.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				createNew();
			}
		});

		// 열기
		ToolItem toolItemOpen = new ToolItem(toolBar, SWT.PUSH);
		toolItemOpen.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/24x24/document-open.png")));
		toolItemOpen.setText("열기");
		toolItemOpen.setToolTipText("기존 압축 파일을 엽니다. (Ctrl+O)");
		toolItemOpen.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				open();
			}
		});

		// 풀기
		toolItemExtract = new ToolItem(toolBar, SWT.PUSH);
		toolItemExtract.setText("풀기");
		toolItemExtract.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/24x24/extract-archive.png")));
		toolItemExtract.setToolTipText("선택된 항목을 압축 해제합니다. (Ctrl+E)");
		toolItemExtract.setEnabled(false);
		toolItemExtract.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				extract();
			}
		});

		new ToolItem(toolBar, SWT.SEPARATOR);

		// 파일 더하기
		toolItemAddFile = new ToolItem(toolBar, SWT.PUSH);
		toolItemAddFile.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/24x24/add-files-to-archive.png")));
		toolItemAddFile.setText("파일 더하기");
		toolItemAddFile.setEnabled(false);
		toolItemAddFile.setToolTipText("압축 파일에 파일을 더합니다.");
		toolItemAddFile.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				addFile();
			}
		});

		// 디렉토리 더하기
		toolItemAddDir = new ToolItem(toolBar, SWT.PUSH);
		toolItemAddDir
				.setImage(new Image(display, getClass().getResourceAsStream(
						"/icons/24x24/add-folder-to-archive.png")));
		toolItemAddDir.setText("디렉토리 더하기");
		toolItemAddDir.setEnabled(false);
		toolItemAddDir.setToolTipText("압축 파일에 디렉토리를 더합니다.");
		toolItemAddDir.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				addDir();
			}
		});
	}

	/**
	 * Tree를 생성하는 메소드
	 */
	private void createTree() {
		// 트리
		if (tree == null || tree.isDisposed()) {
			tree = new Tree(contentsComposite, SWT.SINGLE | SWT.BORDER);
			tree.setVisible(false);
			tree.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					goTo((String) e.item.getData());
				}
			});
		}

		// Sash
		if (sash == null || sash.isDisposed()) {
			sash = new Sash(contentsComposite, SWT.VERTICAL | SWT.SMOOTH);
			sash.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// 크기 변경
					((FormData) sash.getLayoutData()).left = new FormAttachment(
							0, e.x);

					contentsComposite.layout();
				}
			});
		}
	}

	/**
	 * TreeItem을 생성하는 메소드
	 * 
	 * @param parentItem
	 *            부모 TreeItem
	 */
	private void createTreeItems(TreeItem parentItem) {
		// 상위 디렉토리명
		final String dir = (String) parentItem.getData();

		// 폴더 이미지
		final Image folderImage = new Image(sShell.getDisplay(), getClass()
				.getResourceAsStream("/icons/16x16/folder.png"));

		// 하위 디렉토리명
		String[] dirStrings = zip.getDirStrings(dir);

		if (dirStrings == null)
			return;

		for (int i = 0; i < dirStrings.length; i++) {
			TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
			treeItem.setImage(folderImage);
			treeItem.setText(dirStrings[i]);

			if (dir.equals("/"))
				treeItem.setData(dir + dirStrings[i]);
			else
				treeItem.setData(dir + "/" + dirStrings[i]);

			createTreeItems(treeItem);

			if (treeItem.getData().equals(zip.getPath())) {
				tree.setSelection(treeItem);
				treeItem.setExpanded(true);
			}
		}
	}

	/**
	 * 항목을 삭제하는 메소드
	 */
	private void delete() {
		if (!zip.canWrite()) {
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("지우기 실패!");
			messageBox.setMessage(zip.getFileName() + " 파일에 대한 쓰기 권한이 없습니다.");
			messageBox.open();

			return;
		}

		MessageBox messageBox = new MessageBox(sShell, SWT.YES | SWT.NO
				| SWT.ICON_QUESTION);
		messageBox.setText("정말로 삭제하시겠습니까?");
		messageBox
				.setMessage("선택된 항목을 정말로 삭제하시겠습니까?\n\n한 번 삭제하면, 다시 되돌릴 수 없습니다.");

		if (messageBox.open() == SWT.YES) {
			setStatusLine("항목을 삭제하는 중입니다.");

			zip.delete(sShell, table.getSelectionIndices());

			updateContents();
		}
	}

	/**
	 * Tree를 제거하는 메소드
	 */
	private void delTree() {
		if (!tree.isDisposed())
			tree.dispose();

		if (!sash.isDisposed())
			sash.dispose();

		contentsComposite.layout();
	}

	/**
	 * 압축 파일을 푸는 메소드 압축 파일이 열려있어야만 함
	 */
	private void extract() {
		DirectoryDialog directoryDialog = new DirectoryDialog(sShell, SWT.SAVE);
		directoryDialog.setText("압축을 풀 디렉토리를 선택하세요.");
		directoryDialog.setMessage("압축을 풀 디렉토리를 선택하세요.");
		directoryDialog.setFilterPath(defaultPath);
		extract(directoryDialog.open());
	}

	/**
	 * 압축 파일을 푸는 메소드 압축 파일이 열려있어야 함
	 * 
	 * @param directoryName
	 *            압축을 풀 디렉토리명
	 */
	private void extract(String directoryName) {
		if (directoryName == null)
			return;

		// 압축을 풀 디렉토리
		final File directory = new File(directoryName);

		if (!directory.exists()) {
			// 디렉토리가 존재하지 않는 경우
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("압축 풀기 실패!");
			messageBox.setMessage(directory.getPath() + " 디렉토리가 존재하지 않습니다.");
			messageBox.open();

			return;
		}

		if (directory.isFile()) {
			// 디렉토리가 아니라 파일인 경우
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("압축 풀기 실패!");
			messageBox
					.setMessage(directory.getPath() + " 파일을 디렉토리가 아니라 파일입니다.");
			messageBox.open();

			return;
		}

		if (!directory.canWrite()) {
			// 디렉토리에 쓰기 권한이 없는 경우
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("압축 풀기 실패!");
			messageBox.setMessage(directory.getPath()
					+ " 디렉토리에 대한 쓰기 권한이 없습니다.");
			messageBox.open();

			return;
		}

		setStatusLine("압축을 해제하는 중입니다.");

		if (table.getSelectionCount() >= 1)
			// 선택된 항목이 있는 경우
			zip.extract(sShell, directory, table.getSelectionIndices());
		else
			// 선택된 항목이 없는 경우
			zip.extractAll(sShell, directory);

		updateStatusLine();
	}

	/**
	 * 저장 가능한 파일인지 확인하는 메소드
	 * 
	 * @param fileName
	 *            확인할 파일명
	 * @return 저장 가능한 {@link File}, 저장 가능하지 않다면 null
	 */
	private File getSaveFile(String fileName) {
		File file = new File(fileName); // 저장할 압축 파일

		if (file.isDirectory()) {
			// 파일이 아니라 디렉토리인 경우

			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("압축 파일 생성 실패!");
			messageBox.setMessage(fileName + " 디렉토리는 파일이 아니라 디렉토리입니다.");
			messageBox.open();

			return null;
		}

		File parent = file.getParentFile(); // 저장할 압축 파일에 대한 부모 디렉토리

		if (!parent.exists()) {
			// 부모 디렉토리가 존재하지 않는 경우

			if (!parent.mkdirs()) {
				MessageBox messageBox = new MessageBox(sShell, SWT.OK
						| SWT.ICON_ERROR);
				messageBox.setText("압축 파일 생성 실패!");
				messageBox.setMessage(parent.getPath() + " 디렉토리 생성에 실패했습니다.");
				messageBox.open();

				return null;
			}
		}

		if (file.isFile()) {
			// 같은 이름을 가진 파일이 이미 존재하는 경우

			MessageBox messageBox = new MessageBox(sShell, SWT.YES | SWT.NO
					| SWT.ICON_QUESTION);
			messageBox.setText("파일을 덮어쓸까요?");
			messageBox.setMessage(fileName + " 파일이 이미 존재합니다.\n\n이 파일을 덮어쓸까요?");

			if (messageBox.open() == SWT.YES) {
				if (!file.canWrite()) {
					MessageBox messageBox1 = new MessageBox(sShell, SWT.OK
							| SWT.ICON_ERROR);
					messageBox1.setText("압축 파일 생성 실패!");
					messageBox1.setMessage(fileName + " 파일에 대한 쓰기 권한이 없습니다.");
					messageBox1.open();

					return null;
				}
			} else {
				MessageBox messageBox1 = new MessageBox(sShell, SWT.OK
						| SWT.ICON_INFORMATION);
				messageBox1.setText("압축 파일 저장 취소!");
				messageBox1.setMessage("압축 파일 저장이 취소되었습니다.");
				messageBox1.open();

				return null;
			}
		}

		return file;
	}

	/**
	 * 다음 디렉토리로 이동하는 메소드
	 */
	private void goNext() {
		if (nextStack.isEmpty())
			return;

		prevStack.add(zip.getPath());

		zip.setPath(nextStack.pop());

		updateContents();
	}

	/**
	 * 이전 디렉토리로 이동하는 메소드
	 */
	private void goPrev() {
		if (prevStack.isEmpty())
			return;

		nextStack.add(zip.getPath());

		zip.setPath(prevStack.pop());

		updateContents();
	}

	/**
	 * 특정 디렉토리로 이동하는 메소드
	 * 
	 * @param path
	 *            이동할 경로
	 */
	private void goTo(String path) {
		if (path.equals(zip.getPath()))
			return;

		prevStack.add(zip.getPath());
		nextStack.clear();

		zip.setPath(path);

		updateContents();
	}

	/**
	 * 최상위 디렉토리로 이동하는 메소드
	 */
	private void goToHome() {
		if (zip.getPath().equals("/"))
			return;

		prevStack.add(zip.getPath());
		nextStack.clear();

		zip.goToTop();

		updateContents();
	}

	/**
	 * 부모 디렉토리로 이동하는 메소드
	 */
	private void goToParent() {
		if (zip.getPath().equals("/"))
			return;

		prevStack.add(zip.getPath());
		nextStack.clear();

		zip.goToParent();

		updateContents();
	}

	/**
	 * 압축 파일을 불러오는 메소드
	 */
	private void open() {
		FileDialog fileDialog = new FileDialog(sShell, SWT.OPEN);
		fileDialog.setText("불러올 압축 파일을 선택하세요.");
		fileDialog
				.setFilterExtensions(new String[] { "*.zip", "*.jar", "*.*" });
		fileDialog.setFilterNames(new String[] { "Zip 파일 (*.zip)",
				"Jar 파일 (*.jar)", "모든 파일 (*.*)" });
		fileDialog.setFilterPath(defaultPath);
		open(fileDialog.open());
	}

	/**
	 * Zip 파일을 불러오는 메소드
	 * 
	 * @param fileName
	 *            불러올 파일명
	 */
	private void open(String fileName) {
		if (fileName == null) {
			updateStatusLine();

			return;
		}

		setStatusLine("파일을 불러오는 중입니다.");

		File file = new File(fileName);

		if (!file.exists()) {
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("압축 파일 열기 실패!");
			messageBox.setMessage(file.getPath() + " 파일이 존재하지 않습니다.");
			messageBox.open();

			updateStatusLine();

			return;
		}

		if (file.isDirectory()) {
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("압축 파일 열기 실패!");
			messageBox.setMessage(file.getPath() + " 디렉토리는 파일이 아니라 디렉토리입니다.");
			messageBox.open();

			updateStatusLine();

			return;
		}

		if (!file.canRead()) {
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("압축 파일 열기 실패!");
			messageBox.setMessage(file.getPath() + " 파일에 대한 읽기 권한이 없습니다.");
			messageBox.open();

			updateStatusLine();

			return;
		}

		// 인코딩
		String encoding = "MS949";
		if (radioUtf8.getSelection())
			encoding = "UTF8";

		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file, encoding);
		} catch (IOException e) {
			// Zip 파일이 아닌 경우

			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("압축 파일 열기 실패!");
			messageBox.setMessage(file.getPath()
					+ " 파일을 열지 못했습니다.\nZip 파일이 아닌 것 같습니다.");
			messageBox.open();

			updateStatusLine();

			return;
		}

		// 디렉토리로 보기 여부
		final boolean dir = (radioFile.getSelection()) ? false : true;

		zip = new Zip(file, zipFile, dir);

		if (dir) {
			// 디렉토리로 보기인 경우
			if (!tableColumnPath.isDisposed())
				tableColumnPath.dispose();
		} else {
			// 모든 파일 보기인 경우
			if (tableColumnPath.isDisposed())
				createTablecolumnPath();
		}

		prevStack = new Stack<String>();
		nextStack = new Stack<String>();

		// 최근 파일 열기 메뉴에 등록
		final int maxSize = 5;

		for (int i = 0; i < submenuOpenRecent.getItemCount(); i++) {
			final MenuItem menuItem = submenuOpenRecent.getItem(i);

			if (menuItem.getData().equals(zip.getFilePath())) {
				menuItem.dispose();

				break;
			}
		}

		if (submenuOpenRecent.getItemCount() >= maxSize)
			submenuOpenRecent.getItem(maxSize - 1).dispose();

		final MenuItem pushOpenRecent = new MenuItem(submenuOpenRecent,
				SWT.PUSH, 0);
		pushOpenRecent.setImage(new Image(sShell.getDisplay(), getClass()
				.getResourceAsStream("/icons/16x16/package-x-generic.png")));
		pushOpenRecent.setText(zip.getFileName());
		pushOpenRecent.setData(zip.getFilePath());
		pushOpenRecent.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				open((String) pushOpenRecent.getData());
			}
		});

		submenuItemOpenRecent.setEnabled(true);

		sShell.setText(zip.getFileName() + " - " + jzip);

		updateContents();
	}

	/**
	 * 항목의 이름을 변경하는 메소드
	 */
	private void rename() {
		if (!zip.canWrite()) {
			MessageBox messageBox = new MessageBox(sShell, SWT.OK
					| SWT.ICON_ERROR);
			messageBox.setText("이름 바꾸기 실패!");
			messageBox.setMessage(zip.getFileName() + " 파일에 대한 쓰기 권한이 없습니다.");
			messageBox.open();

			return;
		}

		final int index = table.getSelectionIndex();
		final String oldName = zip.getEntryName(index);

		RenameDialog renameDialog = new RenameDialog(sShell, oldName);
		final String newName = renameDialog.open();

		if (newName == null)
			// 이름 바꾸기 취소
			return;

		if (!newName.equals(oldName) && newName.length() != 0) {
			setStatusLine("항목의 이름을 바꾸는 중입니다.");

			zip.rename(sShell, index, newName);

			updateContents();
		}
	}

	/**
	 * 현재 열린 압축 파일을 다른 이름으로 저장하는 메소드
	 */
	private void saveAs() {
		// 압축 파일 선택 대화상자
		FileDialog fileDialog = new FileDialog(sShell, SWT.SAVE);
		fileDialog.setText("저장할 압축 파일을 선택하세요.");
		fileDialog.setFilterPath(defaultPath);
		fileDialog.setFilterExtensions(new String[] { "*.zip", "*.*" });
		fileDialog.setFilterNames(new String[] { "Zip 파일 (*.zip)",
				"모든 파일 (*.*)" });
		String fileName = fileDialog.open();

		if (fileName != null) {
			if (!fileName.toLowerCase().endsWith(".zip"))
				fileName += ".zip";

			if (saveAs(fileName)) {
				// 압축 파일 저장 완료 대화상자
				MessageBox messageBox = new MessageBox(sShell, SWT.YES | SWT.NO
						| SWT.ICON_INFORMATION);
				messageBox.setText("압축 완료!");
				final String message = fileName
						+ " 파일 생성이 완료되었습니다.\n\n새로 생성된 파일을 불러올까요?";
				messageBox.setMessage(message);

				if (messageBox.open() == SWT.YES) {
					// 인코딩 설정 변경
					radioUtf8.setSelection(false);
					radioMs949.setSelection(true);

					// 압축 파일 열기
					open(fileName);
				}
			}
		}
	}

	/**
	 * 압축 파일을 저장하는 메소드
	 * 
	 * @param fileName
	 *            저장할 파일명
	 * @return 압축 성공 여부
	 */
	private boolean saveAs(String fileName) {
		File file = getSaveFile(fileName);

		if (file == null)
			// 압축 파일 저장 실패
			return false;

		setStatusLine("압축하는 중입니다.");

		if (table.getSelectionCount() >= 1)
			// 선택된 항목이 있는 경우
			zip.save(sShell, file, table.getSelectionIndices());
		else
			// 선택된 항목이 없는 경우
			zip.saveAll(sShell, file);

		updateStatusLine();

		// 압축 파일 저장 성공
		return true;
	}

	/**
	 * contentsComposite의 레이아웃을 설정하는 메소드
	 */
	private void setContentsLayout() {
		FormData formData;

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(sash, 0);
		formData.bottom = new FormAttachment(100, 0);
		tree.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 150);
		formData.bottom = new FormAttachment(100, 0);
		sash.setLayoutData(formData);

		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(sash, 0);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, 0);
		table.setLayoutData(formData);
	}

	/**
	 * 테이블에 문맥 메뉴를 추가하는 메소드
	 */
	private void setContextMenu() {
		// 문맥 메뉴
		Menu contextMenu = new Menu(table);
		contextMenu.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent e) {
				updateStatusLine();
			}

			public void menuShown(MenuEvent e) {
			}
		});

		final Display display = sShell.getDisplay();

		// 디렉토리 열기
		contextPushOpenDir = new MenuItem(contextMenu, SWT.PUSH);
		contextPushOpenDir.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/16x16/document-open.png")));
		contextPushOpenDir.setText("디렉토리 열기");
		contextPushOpenDir.setEnabled(false);
		contextPushOpenDir.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				zip.openDir(table.getSelectionIndex());

				updateContents();
			}
		});
		contextPushOpenDir.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 디렉토리로 이동합니다.");
			}
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// 파일 보기
		contextPushView = new MenuItem(contextMenu, SWT.PUSH);
		contextPushView.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/24x24/preview-file.png")));
		contextPushView.setText("파일 보기(&V)");
		contextPushView.setEnabled(false);
		contextPushView.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				zip.openFile(table.getSelectionIndex());
			}
		});
		contextPushView.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 파일을 엽니다.");
			}
		});

		// 파일을 열 프로그램
		contextPushOpen = new MenuItem(contextMenu, SWT.PUSH);
		contextPushOpen.setText("파일을 열 프로그램(&O)...");
		contextPushOpen.setEnabled(false);
		contextPushOpen.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				zip.openWith(sShell, table.getSelectionIndex());
			}
		});
		contextPushOpen.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 파일을 열 프로그램을 선택합니다.");
			}
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// 풀기
		contextPushExtract = new MenuItem(contextMenu, SWT.PUSH);
		contextPushExtract.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/16x16/extract-archive.png")));
		contextPushExtract.setText("풀기(&E)...");
		contextPushExtract.setEnabled(false);
		contextPushExtract.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				extract();
			}
		});
		contextPushExtract.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 항목을 압축 해제합니다.");
			}
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// 이름 바꾸기
		contextPushRename = new MenuItem(contextMenu, SWT.PUSH);
		contextPushRename.setText("이름 바꾸기(&R)...");
		contextPushRename.setEnabled(false);
		contextPushRename.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				rename();
			}
		});
		contextPushRename.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 항목의 이름을 변경합니다.");
			}
		});

		// 지우기
		contextPushDel = new MenuItem(contextMenu, SWT.PUSH);
		contextPushDel.setImage(new Image(display, getClass()
				.getResourceAsStream("/icons/16x16/edit-delete.png")));
		contextPushDel.setText("지우기(&D)...");
		contextPushDel.setEnabled(false);
		contextPushDel.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				delete();
			}
		});
		contextPushDel.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				setStatusLine("선택된 항목을 삭제합니다.");
			}
		});

		table.setMenu(contextMenu);
	}

	/**
	 * DnD 기능을 설정하는 메소드
	 */
	private void setDnd() {
		/**
		 * {@link DropTargetListener}를 구현하는 클래스
		 * 
		 * @author Seungwon Jeong
		 * 
		 */
		final class DropListener implements DropTargetListener {

			/**
			 * 압축 파일에 파일 혹은 디렉토리들을 더하는 메소드
			 * 
			 * @param fileNames
			 *            더할 파일명들
			 */
			private void addFilesToArchive(String[] fileNames) {
				setStatusLine("압축 파일에 파일 혹은 디렉토리를 더하는 중입니다.");

				zip.addFile(sShell, fileNames);

				updateContents();
			}

			/**
			 * 압축 파일에 대한 쓰기 권한이 없어서 더하기를 취소하는 메소드
			 * 
			 * @param event
			 *            DropTargetEvent
			 */
			private void cancel(DropTargetEvent event) {
				MessageBox messageBox = new MessageBox(sShell, SWT.OK
						| SWT.ICON_ERROR);
				messageBox.setText("파일 또는 디렉토리 더하기 실패!");
				messageBox.setMessage(zip.getFilePath()
						+ " 파일에 대한 쓰기 권한이 없습니다.");
				messageBox.open();

				event.detail = DND.DROP_NONE;
			}

			/**
			 * 새로운 압축 파일을 생성하는 메소드
			 * 
			 * @param event
			 *            DropTargetEvent
			 * @param fileNames
			 *            압축할 파일명
			 */
			private void createNewArchive(DropTargetEvent event,
					String[] fileNames) {
				MessageBox messageBox = new MessageBox(sShell, SWT.YES | SWT.NO
						| SWT.ICON_QUESTION);
				messageBox.setText("새로운 압축 파일을 생성할까요?");
				messageBox.setMessage("이 파일들로 새로운 압축 파일을 만들겠습니까?");

				if (messageBox.open() == SWT.YES) {
					createNew();

					setStatusLine("새로운 압축 파일을 생성하는 중입니다.");

					if (zip != null)
						zip.addFile(sShell, fileNames);

					updateContents();
				} else {
					event.detail = DND.DROP_NONE;
				}
			}

			public void dragEnter(DropTargetEvent event) {
			}

			public void dragLeave(DropTargetEvent event) {
			}

			public void dragOperationChanged(DropTargetEvent event) {
			}

			public void dragOver(DropTargetEvent event) {
			}

			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}

				final String[] fileNames = (String[]) event.data;

				if (zip == null) {
					// 현재 열려있는 압축 파일이 없는 경우

					if (fileNames.length == 1) {
						// 파일이 하나인 경우
						if (canOpen(fileNames[0]))
							// 열 수 있는 파일인 경우
							open(fileNames[0]);
						else
							// 열 수 없는 파일인 경우
							createNewArchive(event, fileNames);
					} else {
						// 파일이 여러 개인 경우
						createNewArchive(event, fileNames);
					}
				} else {
					// 열려있는 압축 파일이 있는 경우
					if (fileNames.length > 1) {
						// 파일이 여러 개인 경우
						if (zip.canWrite())
							// Zip 파일에 대한 쓰기 권한이 있는 경우
							addFilesToArchive(fileNames);
						else
							// Zip 파일에 대한 쓰기 권한이 없는 경우
							cancel(event);
					} else {
						// 파일이 하나인 경우
						if (canOpen(fileNames[0])) {
							// 열 수 있는 파일인 경우

							// 동작 선택 대화상자
							switch (new ActionSelectDialog(sShell, fileNames[0])
									.open()) {
							case CANCEL: // 취소
								event.detail = DND.DROP_NONE;
								break;

							case ADD: // 추가
								if (zip.canWrite())
									// 쓰기 권한이 있는 경우
									addFilesToArchive(fileNames);
								else
									// 쓰기 권한이 없는 경우
									cancel(event);
								break;

							case OPEN: // 열기
								open(fileNames[0]);
								break;
							}
						} else {
							// 열 수 없는 파일인 경우
							if (zip.canWrite())
								// 쓰기 권한이 있는 경우
								addFilesToArchive(fileNames);
							else
								// 쓰기 권한이 없는 경우
								cancel(event);
						}
					}
				}
			}

			public void dropAccept(DropTargetEvent event) {
			}
		}

		final DropTarget target = new DropTarget(sShell, DND.DROP_MOVE
				| DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		final DropListener dropListener = new DropListener();
		target.addDropListener(dropListener);

		DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY
				| DND.DROP_LINK);
		source.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		source.addDragListener(new DragSourceListener() {

			public void dragFinished(DragSourceEvent event) {
				target.addDropListener(dropListener);
			}

			public void dragSetData(DragSourceEvent event) {
				int[] indices = table.getSelectionIndices();
				String[] filePaths = new String[indices.length];
				for (int i = 0; i < filePaths.length; i++)
					filePaths[i] = zip.extract(indices[i]);
				event.data = filePaths;
			}

			public void dragStart(DragSourceEvent event) {
				target.removeDropListener(dropListener);
			}

		});
	}

	/**
	 * 상태표시줄의 텍스트를 설정하는 메소드
	 * 
	 * @param text
	 *            설정할 텍스트
	 */
	private void setStatusLine(String text) {
		if (!statusLine.isDisposed()) {
			statusLine.setText(text);
			statusLine.update();
		}
	}

	/**
	 * 내용을 갱신하는 메소드
	 */
	private void updateContents() {
		table.removeAll();

		if (zip == null) {
			// 현재 열려있는 압축 파일이 없는 경우
			table.setVisible(false);
		} else {
			// 현재 열려있는 압축 파일이 있는 경우

			final boolean reverse = checkReverse.getSelection();

			if (radioName.getSelection()) {
				zip.sortByName(reverse);
				zip.sortByDir(false);
			} else if (radioSize.getSelection()) {
				zip.sortBySize(reverse);
			} else if (radioType.getSelection()) {
				zip.sortByType(reverse);
			} else if (radioTime.getSelection()) {
				zip.sortByTime(reverse);
			} else if (radioPath.getSelection()
					&& !tableColumnPath.isDisposed()) {
				zip.sortByPath(reverse);
			} else {
				zip.sortByName(reverse);

				radioName.setSelection(true);
				radioSize.setSelection(false);
				radioType.setSelection(false);
				radioTime.setSelection(false);
				radioPath.setSelection(false);
			}

			table.setItemCount(zip.getSize());
			table.clearAll();
			table.setSortColumn(null);
			table.deselectAll();
			table.setVisible(true);

			table.setFocus();
		}

		updateMenu();
		updateTree();
		updateStatusLine();
	}

	/**
	 * 메뉴를 갱신하는 메소드
	 */
	private void updateMenu() {
		if (zip == null) {
			// Zip 파일이 열려있지 않은 경우

			// Zip 파일
			pushSave.setEnabled(false);
			pushExtract.setEnabled(false);
			pushProperty.setEnabled(false);
			pushClose.setEnabled(false);

			// 편집
			pushAddFile.setEnabled(false);
			pushAddDir.setEnabled(false);
			pushRename.setEnabled(false);
			pushDel.setEnabled(false);
			pushSelectAll.setEnabled(false);
			pushDeselectAll.setEnabled(false);

			// 보기
			pushRefresh.setEnabled(false);

			// 도구 모음
			if (!toolBar.isDisposed()) {
				toolItemExtract.setEnabled(false);
				toolItemAddFile.setEnabled(false);
				toolItemAddDir.setEnabled(false);
			}

			// 디렉토리로 보기 메뉴
			if (!dirComposite.isDisposed()) {
				buttonPrev.setEnabled(false);
				buttonNext.setEnabled(false);
				buttonUp.setEnabled(false);
				buttonHome.setEnabled(false);
				compositeSeparator.setEnabled(false);
				labelPath.setEnabled(false);
				text.setText("");
				text.setEnabled(false);
			}

			// 문맥 메뉴
			contextPushOpenDir.setEnabled(false);
			contextPushView.setEnabled(false);
			contextPushOpen.setEnabled(false);
			contextPushExtract.setEnabled(false);
			contextPushRename.setEnabled(false);
			contextPushDel.setEnabled(false);
		} else {
			// Zip 파일이 열려있는 경우

			// 메뉴
			pushProperty.setEnabled(true);
			pushClose.setEnabled(true);
			pushAddFile.setEnabled(true);
			pushAddDir.setEnabled(true);
			pushExtract.setEnabled(true);
			pushRefresh.setEnabled(true);

			// 문맥 메뉴
			contextPushExtract.setEnabled(true);

			// 도구 모음
			if (!toolBar.isDisposed()) {
				toolItemAddFile.setEnabled(true);
				toolItemAddDir.setEnabled(true);
				toolItemExtract.setEnabled(true);
			}

			// 디렉토리 도구 모음
			if (!dirComposite.isDisposed()) {
				if (prevStack.isEmpty())
					buttonPrev.setEnabled(false);
				else
					buttonPrev.setEnabled(true);

				if (nextStack.isEmpty())
					buttonNext.setEnabled(false);
				else
					buttonNext.setEnabled(true);

				final String path = zip.getPath();
				if (path.equals("/")) {
					buttonUp.setEnabled(false);
					buttonHome.setEnabled(false);
				} else {
					buttonUp.setEnabled(true);
					buttonHome.setEnabled(true);
				}

				compositeSeparator.setEnabled(true);
				labelPath.setEnabled(true);
				text.setText(path);
				text.setEnabled(true);
			}

			// 항목 개수
			final int itemCount = table.getItemCount();

			// 선택된 항목 개수
			final int selectionCount = table.getSelectionCount();

			if (itemCount >= 1) {
				// 항목이 하나 이상 존재하는 경우

				pushSave.setEnabled(true);

				if (selectionCount == itemCount)
					// 모두 선택된 경우
					pushSelectAll.setEnabled(false);
				else
					pushSelectAll.setEnabled(true);
			} else {
				// 항목이 하나도 없는 경우

				pushSave.setEnabled(false);
				pushSelectAll.setEnabled(false);
			}

			if (selectionCount >= 1) {
				// 하나 이상 선택된 경우

				pushDeselectAll.setEnabled(true);
				pushDel.setEnabled(true);
				contextPushDel.setEnabled(true);
			} else {
				// 하나도 선택되지 않은 경우

				pushDeselectAll.setEnabled(false);
				pushDel.setEnabled(false);
				contextPushDel.setEnabled(false);
			}

			if (selectionCount == 1) {
				// 선택된 항목이 하나인 경우

				pushRename.setEnabled(true);
				contextPushRename.setEnabled(true);

				if (zip.isDirecotry(table.getSelectionIndex())) {
					// 선택된 항목이 디렉토리인 경우

					if (radioDir.getSelection())
						contextPushOpenDir.setEnabled(true);
					else
						contextPushOpenDir.setEnabled(false);

					contextPushView.setEnabled(false);
					contextPushOpen.setEnabled(false);
				} else {
					// 선택된 항목이 파일인 경우

					contextPushOpenDir.setEnabled(false);
					contextPushView.setEnabled(true);
					contextPushOpen.setEnabled(true);
				}
			} else {
				pushRename.setEnabled(false);
				contextPushRename.setEnabled(false);
				contextPushOpenDir.setEnabled(false);
				contextPushView.setEnabled(false);
				contextPushOpen.setEnabled(false);
			}
		}
	}

	/**
	 * 상태 표시줄을 갱신하는 메소드
	 */
	private void updateStatusLine() {
		if (statusLine.isDisposed())
			return;

		String text;

		if (zip == null) {
			// Zip 파일이 열려 있지 않은 경우
			text = "";
		} else {
			// Zip 파일이 열려 있는 경우
			text = "총 " + zip.getSize() + " 항목 ("
					+ Zip.getSizeString(zip.getOriginalLength()) + ")";

			final int count = table.getSelectionCount();

			if (count >= 1) {
				// 선택된 항목이 있는 경우

				long totalSize = 0;
				for (int index : table.getSelectionIndices())
					totalSize += zip.getEntrySize(index);

				text += " 중 " + count + " 항목 선택됨 ("
						+ Zip.getSizeString(totalSize) + ")";
			}
		}

		setStatusLine(text);
	}

	/**
	 * 디렉토리 구조를 갱신하는 메소드
	 */
	private void updateTree() {
		if (tree.isDisposed())
			return;

		tree.removeAll();

		if (zip != null) {
			TreeItem rootItem = new TreeItem(tree, SWT.NONE);
			rootItem.setImage(new Image(sShell.getDisplay(), getClass()
					.getResourceAsStream("/icons/16x16/package-x-generic.png")));
			rootItem.setText(zip.getFileName());
			rootItem.setData("/");

			createTreeItems(rootItem);

			rootItem.setExpanded(true);

			tree.setVisible(true);
		} else {
			tree.setVisible(false);
		}
	}
}
