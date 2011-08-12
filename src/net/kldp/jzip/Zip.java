/**
 * Zip 파일에 대한 정보를 담고 있는 클래스
 */
package net.kldp.jzip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;

import net.kldp.jzip.OverwriteDialog.Overwrite;
import net.kldp.jzip.ProgressDialog.ProgressMode;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Zip 파일 대한 정보를 담고 있는 클래스
 * 
 * @author Seungwon Jeong
 * 
 */
public class Zip {
	private static final String defaultEncoding = "MS949"; // 기본 인코딩 : MS949

	/**
	 * 새로운(빈) Zip 파일을 생성하는 메소드
	 * 
	 * @param saveFile
	 *            새로 생성할 {@link File}
	 */
	public static void createNew(File saveFile) {
		try {
			ZipOutputStream zos = new ZipOutputStream(saveFile);

			try {
				// 인코딩 설정
				zos.setEncoding(defaultEncoding);
			} finally {
				zos.finish();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 파일 크기를 문자열로 반환하는 메소드
	 * 
	 * @param size
	 *            파일 크기
	 * @return 파일 크기를 표현하는 문자열
	 */
	public static String getSizeString(long size) {
		if (size <= -1) {
			return "알 수 없음";
		} else {
			final int KB = 1024;
			final int MB = 1024 * KB;
			final int GB = 1024 * MB;

			double newSize = size;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);

			if (size >= GB) {
				newSize /= GB;
				return nf.format(newSize) + " GB";
			} else if (size >= MB) {
				newSize /= MB;
				return nf.format(newSize) + " MB";
			} else if (size >= KB) {
				newSize /= KB;
				return nf.format(newSize) + " KB";
			} else {
				return size + " 바이트";
			}
		}
	}

	/**
	 * 시간을 문자열로 반환하는 메소드
	 * 
	 * @param time
	 *            시간
	 * @return 시간을 표현하는 문자열
	 */
	public static String getTimeString(long time, int dateFormat) {
		if (time == -1) {
			return "알 수 없음";
		} else {
			Date date = new Date(time);
			DateFormat df = DateFormat.getDateTimeInstance(dateFormat,
					dateFormat);

			return df.format(date);
		}
	}

	private File file; // Zip 파일

	private ZipFile zipFile; // ZipFile 객체

	private ArrayList<ZipEntry> entryList; // ZipEntry의 ArrayList

	private String path; // Zip 파일 내의 경로

	private int originalSize; // Zip 파일 내 항목의 숫자

	/**
	 * {@link Zip} 클래스의 생성자
	 * 
	 * @param file
	 *            {@link File}
	 * @param zipFile
	 *            {@link ZipFile}
	 * @param dir
	 *            디렉토리로 보기 여부
	 */
	public Zip(File file, ZipFile zipFile, boolean dir) {
		this.file = file;
		this.zipFile = zipFile;

		if (dir)
			// 디렉토리로 보기인 경우
			path = "";
		else
			// 모든 파일로 보기인 경우
			path = null;

		loadEntries();
	}

	/**
	 * Zip 파일에 디렉토리를 더하는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param directory
	 *            더할 디렉토리 {@link File}
	 */
	public void addDir(final Shell shell, final File directory) {
		String[] filePaths = { directory.getPath() };

		addFile(shell, filePaths);
	}

	/**
	 * Zip 파일에 파일이나 디렉토리를 더하는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param filePaths
	 *            더할 파일들의 경로
	 */
	public void addFile(final Shell shell, final String[] filePaths) {
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {

			// 더할 파일들의 배열
			private File[] files;

			// 덮어 쓰기 설정
			private Overwrite overwrite = Overwrite.NO;

			// 삭제할 엔트리 인덱스들의 HashSet
			private HashSet<Integer> indexSet;

			/**
			 * 실제로 파일을 추가하는 메소드
			 * 
			 * @param shell
			 *            Shell
			 */
			private void addFile(Shell shell) {
				final String encoding = zipFile.getEncoding(); // 인코딩

				// 임시 디렉토리 생성
				JZip.createTmpDir();

				// 임시 파일
				File tempFile = new File(JZip.tmpDir, file.getName());

				// 임시 Zip 파일 생성
				ZipOutputStream zos = null;
				try {
					zos = new ZipOutputStream(tempFile);

					try {
						zos.setEncoding(encoding);

						for (int i = 0; i < entryList.size(); i++) {
							if (indexSet.contains(i))
								continue;

							ZipEntry originalEntry = entryList.get(i);

							ZipEntry entry = new ZipEntry(originalEntry
									.getName());

							// 바뀐 시간 설정
							long time = originalEntry.getTime();
							if (time != -1)
								entry.setTime(time);

							zos.putNextEntry(entry);

							if (!originalEntry.isDirectory())
								// 파일인 경우
								archive(zipFile.getInputStream(originalEntry),
										zos);

							zos.closeEntry();
						}

						// 추가할 파일 및 디렉토리를 추가
						addFileNDir(shell, zos, files, path);
					} finally {
						zos.finish();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				// 임시 파일을 현재 파일로 복사
				copyFile(tempFile, file);

				// Zip 파일 새로 고침
				try {
					zipFile = new ZipFile(file, encoding);
				} catch (IOException e) {
					e.printStackTrace();
				}

				loadEntries();
			}

			/**
			 * {@link ZipOutputStream}에 파일을 추가하는 메소드
			 * 
			 * @param shell
			 *            {@link Shell}
			 * @param zos
			 *            {@link ZipOutputStream}
			 * @param files
			 *            더할 파일들
			 * @param parent
			 *            부모 엔트리 이름
			 */
			private void addFileNDir(Shell shell, ZipOutputStream zos,
					File[] files, String parent) {
				for (File file : files) {
					if (file == null)
						continue;

					if (!file.canRead()) {
						MessageBox messageBox = new MessageBox(shell, SWT.OK
								| SWT.ICON_ERROR);
						messageBox.setText("파일 또는 디렉토리 더하기 실패!");
						messageBox.setMessage(file.getPath()
								+ " 파일 또는 디렉토리에 대한 읽기 권한이 없습니다.");
						messageBox.open();

						continue;
					}

					if (file.isDirectory()) {
						// 디렉토리인 경우

						// 추가할 디렉토리 엔트리
						ZipEntry entry = null;
						if (parent == null)
							entry = new ZipEntry(file.getName() + "/");
						else
							entry = new ZipEntry(parent + file.getName() + "/");

						entry.setTime(file.lastModified());

						try {
							zos.putNextEntry(entry);

							zos.closeEntry();
						} catch (IOException e) {
							e.printStackTrace();
						}

						// 부모 디렉토리의 이름
						String parentName = file.getName() + "/";
						if (parent != null)
							parentName = parent + parentName;

						// 하위 디렉토리의 모든 파일과 디렉토리도 추가함
						addFileNDir(shell, zos, file.listFiles(), parentName);
					} else {
						// 파일인 경우

						// 추가할 파일 엔트리
						ZipEntry entry = null;
						if (parent == null)
							entry = new ZipEntry(file.getName());
						else
							entry = new ZipEntry(parent + file.getName());

						entry.setTime(file.lastModified());

						// 압축
						try {
							zos.putNextEntry(entry);

							archive(new FileInputStream(file), zos);

							zos.closeEntry();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			/**
			 * 덮어쓰기를 확인하는 메소드
			 * 
			 * @param fileName
			 *            확인할 파일 또는 디렉토리의 이름
			 * @return 덮어쓰기 여부
			 */
			private boolean checkOverwrite(String fileName) {
				if (fileName.endsWith("/"))
					fileName = fileName.substring(0, fileName.length() - 1);

				switch (overwrite) {
				case ALL_YES:
					return true;

				case ALL_NO:
					return false;

				case YES:
				case NO:
					OverwriteDialog overwriteDialog = new OverwriteDialog(
							shell, fileName);
					overwrite = overwriteDialog.open();

					switch (overwrite) {
					case ALL_YES:
					case YES:
						return true;

					case ALL_NO:
					case NO:
					case CANCEL:
						return false;
					}

				default:
					return false;
				}
			}

			/**
			 * 삭제할 엔트리를 구하는 메소드
			 */
			private void getIndexSet() {
				indexSet = new HashSet<Integer>();

				for (int i = 0; i < files.length; i++) {
					if (files[i] == null)
						continue;

					// 확인할 파일 이름
					String fileName = files[i].getName();

					if (path != null)
						fileName = path + fileName;

					// 확인할 디렉토리 이름
					String dirName = fileName + "/";

					for (int j = 0; j < entryList.size(); j++) {
						// ZipEntry
						ZipEntry zipEntry = entryList.get(j);

						// 엔트리 이름
						String entryName = zipEntry.getName();

						if (entryName.equals(fileName)) {
							// 같은 이름을 가진 파일이 이미 존재하는 경우

							if (checkOverwrite(fileName)) {
								indexSet.add(j);
							} else {
								if (overwrite == Overwrite.CANCEL)
									return;

								files[i] = null;
							}
						} else if (entryName.equals(dirName)) {
							// 같은 이름을 가진 디렉토리가 이미 존재하는 경우

							if (checkOverwrite(dirName)) {
								indexSet.add(j);

								for (int k = 0; k < entryList.size(); k++) {
									String parent = getParentEntryName(entryList
											.get(k));

									while (parent.length() != 0) {
										if (parent.equals(dirName)) {
											indexSet.add(k);

											break;
										}

										parent = getParentEntryName(parent);
									}
								}
							} else {
								if (overwrite == Overwrite.CANCEL)
									return;

								files[i] = null;
							}
						}
					}
				}
			}

			public void run() {
				files = new File[filePaths.length];

				for (int i = 0; i < files.length; i++) {
					files[i] = new File(filePaths[i]);

					if (!files[i].exists()) {
						MessageBox messageBox = new MessageBox(shell, SWT.OK
								| SWT.ICON_ERROR);
						messageBox.setText("파일 또는 디렉토리 더하기 실패!");
						messageBox.setMessage(files[i].getPath()
								+ " 파일 또는 디렉토리가 존재하지 않습니다.");
						messageBox.open();

						files[i] = null;

						continue;
					}
				}

				getIndexSet();

				if (overwrite == Overwrite.CANCEL) {
					// 파일 또는 디렉토리 더하기 취소
					MessageBox messageBox = new MessageBox(shell, SWT.OK
							| SWT.ICON_INFORMATION);
					messageBox.setText("파일 또는 디렉토리 더하기 취소!");
					messageBox.setMessage("파일 또는 디렉토리 더하기가 취소되었습니다.");
					messageBox.open();

					return;
				}

				for (File file : files) {
					if (file != null) {
						addFile(shell);

						break;
					}
				}

			}

		});

	}

	/**
	 * {@link InputStream}을 {@link ZipOutputStream}으로 보내는 메소드
	 * 
	 * @param is
	 *            {@link InputStream}
	 * @param zos
	 *            {@link ZipOutputStream}
	 */
	private void archive(InputStream is, ZipOutputStream zos) {
		try {
			BufferedInputStream bis = new BufferedInputStream(is);
			BufferedOutputStream bos = new BufferedOutputStream(zos);

			try {
				int i;
				while ((i = bis.read()) != -1)
					bos.write(i);
			} finally {
				bis.close();
				bos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Zip 파일에 대한 쓰기 권한이 있는지의 여부를 확인하는 메소드
	 * 
	 * @return Zip 파일에 대한 쓰기 권한 여부
	 */
	public boolean canWrite() {
		return file.canWrite();
	}

	/**
	 * 테이블의 인덱스를 {@link ZipEntry} {@link ArrayList}의 인덱스로 바꿔주는 메소드
	 * 
	 * @param index
	 *            테이블의 인덱스
	 * @return {@link ZipEntry} {@link ArrayList}의 인덱스
	 */
	private int convertIndex(int index) {
		int newIndex = 0;

		// 인덱스 변환
		for (ZipEntry entry : entryList) {
			if (getParentEntryName(entry).equals(path)) {
				if (index == 0)
					break;
				else
					index--;
			}

			newIndex++;
		}

		return newIndex;
	}

	/**
	 * 파일을 복사하는 메소드
	 * 
	 * @param input
	 *            원본 {@link File}
	 * @param output
	 *            대상 {@link File}
	 */
	private void copyFile(File input, File output) {
		try {
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(input));
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(output));

			try {
				int i;
				while ((i = bis.read()) != -1)
					bos.write(i);
			} finally {
				bis.close();
				bos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@link ZipEntry}를 삭제하는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param indices
	 *            테이블의 인덱스
	 */
	public void delete(Shell shell, final int[] indices) {
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {

			// 삭제할 항목들의 인덱스에 대한 ArrayList
			private ArrayList<Integer> indexList;

			/**
			 * 삭제할 항목들에 대한 인덱스 리스트를 구하는 메소드
			 * 
			 * @param indices
			 *            테이블의 인덱스 배열
			 */
			private void getIndexList(final int[] indices) {
				indexList = new ArrayList<Integer>(indices.length);

				if (path != null) {
					// 디렉토리로 보기인 경우

					// 인덱스 변환
					for (int i = 0; i < indices.length; i++)
						indices[i] = convertIndex(indices[i]);

					for (int index : indices) {
						ZipEntry dirEntry = entryList.get(index);

						if (dirEntry.isDirectory()) {
							// 디렉토리인 경우

							// 모든 하위 디렉토리의 엔트리 추가
							final String dirName = dirEntry.getName();

							for (int i = 0; i < entryList.size(); i++) {
								String entryPath = getParentEntryName(entryList
										.get(i));

								while (entryPath.length() != 0) {
									if (entryPath.equals(dirName)) {
										indexList.add(i);

										break;
									}

									entryPath = getParentEntryName(entryPath);
								}
							}
						}
					}
				}

				for (int index : indices)
					indexList.add(index);
			}

			public void run() {
				getIndexList(indices);

				// 임시 디렉토리 생성
				JZip.createTmpDir();

				// 임시 파일
				File tempFile = new File(JZip.tmpDir, file.getName());

				final String encoding = zipFile.getEncoding(); // 인코딩

				try {
					ZipOutputStream zos = new ZipOutputStream(tempFile);

					try {
						zos.setEncoding(encoding);

						for (int i = 0; i < entryList.size(); i++) {
							if (indexList.contains(i))
								continue;

							ZipEntry originalEntry = entryList.get(i);
							ZipEntry entry = new ZipEntry(originalEntry
									.getName());

							// 바뀐 시간 설정
							long time = originalEntry.getTime();
							if (time != -1)
								entry.setTime(time);

							zos.putNextEntry(entry);

							if (!originalEntry.isDirectory())
								// 파일인 경우
								archive(zipFile.getInputStream(originalEntry),
										zos);

							zos.closeEntry();
						}
					} finally {
						zos.finish();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

				// 임시 파일을 현재 파일에 복사
				copyFile(tempFile, file);

				// Zip 파일 다시 불러오기
				try {
					zipFile = new ZipFile(file, encoding);
				} catch (IOException e) {
					e.printStackTrace();
				}

				loadEntries();
			}

		});
	}

	/**
	 * 선택된 항목을 임시 디렉토리에 압축 해제하는 메소드
	 * 
	 * @param index
	 *            테이블의 인덱스
	 * @return 임시 디렉토리에 압축 해제된 파일 경로
	 */
	public String extract(int index) {
		if (path != null)
			// 디렉토리로 보기인 경우
			index = convertIndex(index);

		// 임시 디렉토리 생성
		JZip.createTmpDir();

		// 압축 해제할 ZipEntry
		ZipEntry entry = entryList.get(index);

		// 임시 파일
		File tempFile = new File(JZip.tmpDir, getEntryName(entry));

		if (entry.isDirectory()) {
			// 디렉토리인 경우

			if (tempFile.isFile())
				// 같은 이름의 파일이 이미 존재하는 경우
				tempFile.delete(); // 삭제

			// 디렉토리 생성
			tempFile.mkdirs();

			if (path != null) {
				// 디렉토리로 보기인 경우

				// 모든 하위 디렉토리와 파일까지 압축 해제
				for (ZipEntry zipEntry : entryList) {
					String parent = getParentEntryName(zipEntry);

					while (parent.length() != 0) {
						if (parent.equals(entry.getName())) {
							// 부모 디렉토리 이름
							String parentEntryName = getParentEntryName(zipEntry
									.getName().substring(parent.length()));

							// 임시 파일
							File entryFile = null;

							if (parentEntryName.length() != 0) {
								// 부모 디렉토리가 존재하는 경우

								// 부모 디렉토리 생성
								File parentDir = new File(tempFile,
										parentEntryName);
								if (!parentDir.exists())
									parentDir.mkdirs();

								entryFile = new File(parentDir,
										getEntryName(zipEntry));
							} else {
								// 부모 디렉토리가 존재하지 않는 경우

								entryFile = new File(tempFile,
										getEntryName(zipEntry));
							}

							if (zipEntry.isDirectory()) {
								// 디렉토리인 경우

								if (entryFile.isFile())
									// 같은 이름의 파일이 이미 존재하는 경우
									entryFile.delete(); // 삭제

								// 디렉토리 생성
								entryFile.mkdirs();
							} else {
								// 파일인 경우

								if (entryFile.isDirectory())
									// 같은 이름의 디렉토리가 이미 존재하는 경우
									// 모든 하위 디렉토리까지 삭제
									JZip.deleteDir(entryFile);

								// 압축 해제
								extract(zipEntry, entryFile);
							}

							break;
						}

						parent = getParentEntryName(parent);
					}
				}
			}
		} else {
			// 파일인 경우

			if (tempFile.isDirectory())
				// 같은 이름의 디렉토리가 이미 존재하는 경우
				JZip.deleteDir(tempFile); // 모든 하위 디렉토리와 파일까지 삭제

			// 압축 해제
			extract(entry, tempFile);
		}

		// 압축 해제한 파일의 경로 반환
		return tempFile.getPath();
	}

	/**
	 * 인덱스에 해당하는 {@link ZipEntry}의 압축을 푸는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param directory
	 *            압축을 풀 대상 디렉토리 {@link File}
	 * @param indices
	 *            테이블 인덱스
	 */
	public void extract(final Shell shell, final File directory,
			final int[] indices) {
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {

			// 덮어쓰기 설정
			private Overwrite overwrite = Overwrite.NO;

			// 압축 해제할 항목들의 인덱스에 대한 ArrayList
			private ArrayList<Integer> indexList;

			/**
			 * 덮어쓰기 설정을 확인하는 메소드
			 * 
			 * @param path
			 *            덮어쓸 파일 또는 디렉토리의 경로
			 * @return 덮어쓰기 여부
			 */
			private boolean checkOverwrite(String path) {
				switch (overwrite) {
				case ALL_YES:
					return true;

				case ALL_NO:
					return false;

				case YES:
				case NO:
					// 덮어쓰기 확인 대화상자
					OverwriteDialog overwriteDialog = new OverwriteDialog(
							shell, path);
					overwrite = overwriteDialog.open();

					switch (overwrite) {
					case ALL_YES:
					case YES:
						return true;

					case ALL_NO:
					case NO:
					case CANCEL:
						return false;
					}

				default:
					return false;
				}
			}

			/**
			 * 실제로 압축을 푸는 메소드
			 * 
			 * @param shell
			 *            Shell
			 * @param directory
			 *            압축을 해제할 디렉토리
			 */
			private void extractAll(final Shell shell, final File directory) {
				ProgressDialog progressDialog = new ProgressDialog(shell,
						ProgressMode.EXTRACT, indexList.size());
				progressDialog.open();

				for (int i = 0; i < indexList.size(); i++) {
					ZipEntry entry = entryList.get(indexList.get(i));

					if (entry.isDirectory()) {
						// 디렉토리인 경우

						File entryDir = new File(directory, entry.getName());

						if (entryDir.isFile()) {
							// 같은 이름의 파일이 이미 존재하는 경우

							if (checkOverwrite(entryDir.getPath())) {
								// 덮어쓰는 경우

								if (!entryDir.canWrite()) {
									MessageBox messageBox = new MessageBox(
											shell, SWT.OK | SWT.ICON_ERROR);
									messageBox.setText("압축 풀기 실패!");
									messageBox.setMessage(entryDir.getPath()
											+ " 파일에 대한 쓰기 권한이 없습니다.");
									messageBox.open();

									continue;
								} else {
									entryDir.delete();
								}
							} else {
								// 덮어쓰지 않는 경우

								if (overwrite == Overwrite.CANCEL) {
									// 압축 해제 취소
									MessageBox messageBox = new MessageBox(
											shell, SWT.OK
													| SWT.ICON_INFORMATION);
									messageBox.setText("압축 풀기 취소!");
									messageBox.setMessage("압축 풀기가 취소되었습니다.");
									messageBox.open();

									return;
								} else {
									continue;
								}
							}
						}

						// 디렉토리 생성
						entryDir.mkdirs();
					} else {
						// 파일인 경우

						File entryFile = null;

						String parentEntryName = getParentEntryName(entry);

						if (parentEntryName.length() != 0) {
							// 부모 디렉토리 생성
							File parentDir = new File(directory,
									parentEntryName);
							if (!parentDir.exists())
								parentDir.mkdirs();

							entryFile = new File(parentDir, getEntryName(entry));
						} else {
							entryFile = new File(directory, getEntryName(entry));
						}

						if (entryFile.exists()) {
							// 같은 이름의 파일이나 디렉토리가 이미 존재하는 경우
							if (checkOverwrite(entryFile.getPath())) {
								// 덮어쓰는 경우

								if (!entryFile.canWrite()) {
									MessageBox messageBox = new MessageBox(
											shell, SWT.OK | SWT.ICON_ERROR);
									messageBox.setText("압축 풀기 실패!");
									messageBox.setMessage(entryFile.getPath()
											+ " 파일 또는 디렉토리에 대한 쓰기 권한이 없습니다.");
									messageBox.open();

									continue;
								} else if (entryFile.isDirectory()) {
									// 디렉토리 삭제
									JZip.deleteDir(entryFile);
								}
							} else {
								// 덮어쓰지 않는 경우

								if (overwrite == Overwrite.CANCEL) {
									// 압축 해제 취소
									MessageBox messageBox = new MessageBox(
											shell, SWT.OK
													| SWT.ICON_INFORMATION);
									messageBox.setText("압축 풀기 취소!");
									messageBox.setMessage("압축 풀기가 취소되었습니다.");
									messageBox.open();

									return;
								} else {
									continue;
								}
							}
						}

						// 파일 압축 해제
						extract(entry, entryFile);
					}

					progressDialog.update(i + 1);
				}

				progressDialog.close();

				// 압축 해제 완료 대화상자
				MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO
						| SWT.ICON_INFORMATION);
				messageBox.setText("압축 풀기 완료!");
				messageBox.setMessage("압축 풀기가 완료되었습니다.\n\n압축 푼 항목들을 표시할까요?");

				if (messageBox.open() == SWT.YES)
					// 압축 해제한 항목 표시
					Program.launch(directory.getPath());
			}

			/**
			 * 압축을 해제할 인덱스를 구하는 메소드
			 * 
			 * @param indices
			 *            테이블의 인덱스 배열, null이면 모두 압축 해제
			 */
			private void getIndexList(final int[] indices) {
				indexList = new ArrayList<Integer>();

				if (indices != null) {
					// 모두 압축 풀기가 아닌 경우

					indexList = new ArrayList<Integer>(indices.length);

					if (path != null) {
						// 디렉토리로 보기인 경우

						// 인덱스 변환
						for (int i = 0; i < indices.length; i++)
							indices[i] = convertIndex(indices[i]);

						for (int index : indices) {
							ZipEntry dirEntry = entryList.get(index);

							if (dirEntry.isDirectory()) {
								// 디렉토리인 경우
								// 하위 디렉토리의 엔트리 추가
								final String dirName = dirEntry.getName();

								for (ZipEntry entry : entryList) {
									String entryPath = getParentEntryName(entry);

									while (entryPath.length() != 0) {
										if (entryPath.equals(dirName)) {
											indexList.add(entryList
													.indexOf(entry));
											break;
										}

										entryPath = getParentEntryName(entryPath);
									}
								}
							}
						}
					}

					for (int index : indices)
						indexList.add(index);
				} else {
					// 모두 압축 풀기인 경우

					// 전체 엔트리 숫자
					final int size = entryList.size();

					indexList = new ArrayList<Integer>(size);

					for (int i = 0; i < size; i++)
						indexList.add(i);
				}
			}

			public void run() {
				// 압축 해제할 항목들에 대한 인덱스 구하기
				getIndexList(indices);

				// 압축 해제하기
				extractAll(shell, directory);
			}

		});

	}

	/**
	 * {@link ZipEntry}의 압축을 푸는 메소드
	 * 
	 * @param entry
	 *            압축을 풀 엔트리
	 * @param entryFile
	 *            압축을 풀 대상 {@link File}
	 */
	private void extract(ZipEntry entry, File entryFile) {
		try {
			BufferedInputStream bis = new BufferedInputStream(
					zipFile.getInputStream(entry));
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(entryFile));

			try {
				int i;
				while ((i = bis.read()) != -1)
					bos.write(i);
			} finally {
				bis.close();
				bos.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 압축 파일 내의 모든 파일과 디렉토리를 압축 해제하는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param directory
	 *            압축을 풀 디렉토리 {@link File}
	 */
	public void extractAll(final Shell shell, final File directory) {
		extract(shell, directory, null);
	}

	/**
	 * 압축 파일 내 디렉토리의 크기를 반환하는 메소드
	 * 
	 * @param dirEntry
	 *            크기를 계산할 디렉토리 엔트리
	 * @return 디렉토리의 크기
	 */
	private long getDirSize(ZipEntry dirEntry) {
		// 디렉토리의 크기
		long size = 0;

		// 크기를 계산할 디렉토리 이름
		final String dirName = dirEntry.getName();

		// 디렉토리의 크기 계산
		for (ZipEntry entry : entryList) {
			if (!entry.isDirectory()) {
				// 파일인 경우
				String entryPath = getParentEntryName(entry);

				while (entryPath.length() != 0) {
					if (entryPath.equals(dirName)) {
						size += entry.getSize();

						break;
					}

					entryPath = getParentEntryName(entryPath);
				}
			}
		}

		return size;
	}

	public String[] getDirStrings(String dir) {
		ArrayList<String> dirList = new ArrayList<String>();

		for (ZipEntry entry : entryList)
			if (entry.isDirectory() && getEntryPath(entry).equals(dir))
				dirList.add(getEntryName(entry));

		if (dirList.isEmpty())
			return null;
		else
			return dirList.toArray(new String[dirList.size()]);
	}

	/**
	 * {@link ZipEntry}의 이름(경로 포함 안함)을 반환하는 메소드
	 * 
	 * @param index
	 *            테이블의 인덱스
	 * @return {@link ZipEntry}의 이름(경로 포함 안함)
	 */
	public String getEntryName(int index) {
		if (path != null)
			// 디렉토리로 보기인 경우
			index = convertIndex(index);

		return getEntryName(entryList.get(index));
	}

	/**
	 * {@link ZipEntry}의 이름(경로 포함 안함)을 반환하는 메소드
	 * 
	 * @param entry
	 *            {@link ZipEntry}
	 * @return {@link ZipEntry}의 이름(경로 포함 안함)
	 */
	private String getEntryName(ZipEntry entry) {
		// ZipEntry의 이름
		String name = entry.getName();

		if (entry.isDirectory())
			name = name.substring(0, name.length() - 1);

		if (name.indexOf('/') == -1)
			// 최상위 디렉토리의 파일인 경우
			return name;
		else
			return name.substring(name.lastIndexOf('/') + 1);
	}

	/**
	 * {@link ZipEntry}의 위치를 반환합니다.
	 * 
	 * @param entry
	 *            {@link ZipEntry}
	 * @return {@link ZipEntry}의 위치
	 */
	private String getEntryPath(ZipEntry entry) {
		String name = entry.getName(); // 엔트리의 이름

		if (name.endsWith("/"))
			// 디렉토리인 경우
			name = name.substring(0, name.length() - 1);

		if (name.indexOf('/') == -1)
			// 최상위 디렉토리의 파일인 경우
			return "/";
		else
			return "/" + name.substring(0, name.lastIndexOf('/'));
	}

	/**
	 * {@link ZipEntry}의 크기를 반환하는 메소드
	 * 
	 * @param index
	 *            테이블의 인덱스
	 * @return {@link ZipEntry}의 크기
	 */
	public long getEntrySize(int index) {
		if (path != null) {
			// 디렉토리로 보기인 경우

			index = convertIndex(index);

			ZipEntry zipEntry = entryList.get(index);

			if (zipEntry.isDirectory())
				return getDirSize(zipEntry);
			else
				return zipEntry.getSize();
		} else {
			// 모든 파일 보기인 경우

			return entryList.get(index).getSize();
		}

	}

	/**
	 * Zip 파일의 이름(경로 포함 안함)을 반환하는 메소드
	 * 
	 * @return Zip 파일의 이름(경로 포함 안함)
	 */
	public String getFileName() {
		return file.getName();
	}

	/**
	 * Zip 파일의 경로(파일 이름 포함 안함)를 반환하는 메소드
	 * 
	 * @return Zip 파일의 경로(파일 이름 포함 안함)
	 */
	public String getFileParentPath() {
		return file.getParent();
	}

	/**
	 * Zip 파일의 이름(경로 포함)을 반환하는 메소드
	 * 
	 * @return Zip 파일의 이름(경로 포함)
	 */
	public String getFilePath() {
		return file.getPath();
	}

	/**
	 * Zip 파일의 크기(압축 크기)를 반환하는 메소드
	 * 
	 * @return Zip 파일의 크기(압축 크기)
	 */
	public long getLength() {
		return file.length();
	}

	/**
	 * Zip 파일에 빠진 디렉토리 엔트리를 추가하는 메소드
	 */
	private void getMissingEntries() {
		// 디렉토리 엔트리 이름에 대한 HashSet
		HashSet<String> nameSet = new HashSet<String>();

		for (ZipEntry entry : entryList) {
			String parent = getParentEntryName(entry);

			while (parent.length() != 0) {
				if (zipFile.getEntry(parent) == null)
					// 디렉토리 엔트리가 빠져있는 경우
					nameSet.add(parent);

				parent = getParentEntryName(parent);
			}
		}

		for (String name : nameSet)
			entryList.add(new ZipEntry(name));
	}

	/**
	 * Zip 파일의 실제 크기를 반한하는 메소드
	 * 
	 * @return Zip 파일의 실제 크기
	 */
	public long getOriginalLength() {
		long size = 0;

		for (ZipEntry entry : entryList)
			size += entry.getSize();

		return size;
	}

	/**
	 * Zip 파일 내의 모든 항목의 숫자를 반환하는 메소드
	 * 
	 * @return Zip 파일 내의 모든 항목의 숫자
	 */
	public int getOriginalSize() {
		return originalSize;
	}

	/**
	 * {@link ZipEntry}의 경로(부모 엔트리 이름)을 반환하는 메소드
	 * 
	 * @param name
	 *            {@link ZipEntry}의 이름
	 * @return {@link ZipEntry}의 경로(부모 엔트리 이름)
	 */
	private String getParentEntryName(String name) {
		if (name.endsWith("/"))
			// 디렉토리인 경우
			name = name.substring(0, name.length() - 1);

		if (name.indexOf('/') == -1)
			// 최상위 디렉토리의 파일인 경우
			return "";
		else
			return name.substring(0, name.lastIndexOf('/') + 1);
	}

	/**
	 * {@link ZipEntry}의 경로(부모 엔트리 이름)을 반환하는 메소드
	 * 
	 * @param entry
	 *            {@link ZipEntry}
	 * @return {@link ZipEntry}의 경로(부모 엔트리 이름)
	 */
	private String getParentEntryName(ZipEntry entry) {
		return getParentEntryName(entry.getName());
	}

	/**
	 * Zip 파일 내의 현재 경로를 반환하는 메소드
	 * 
	 * @return Zip 파일 내의 현재 경로
	 */
	public String getPath() {
		if (path == null)
			return null;

		if (path.length() == 0)
			return "/";
		else
			return "/" + path.substring(0, path.length() - 1);
	}

	/**
	 * 현재 경로에 해당하는 항목의 숫자를 반환하는 메소드
	 * 
	 * @return 현재 경로에 해당하는 항목의 숫자
	 */
	public int getSize() {
		if (path != null) {
			// 디렉토리로 보기인 경우

			// 현재 경로에 해당하는 항목의 숫자
			int count = 0;

			// 숫자 계산
			for (ZipEntry entry : entryList)
				if (getParentEntryName(entry).equals(path))
					count++;

			return count;
		} else {
			// 모든 파일 보기인 경우
			return originalSize;
		}
	}

	/**
	 * 테이블 출력을 위한 {@link String} 배열을 반환하는 메소드
	 * 
	 * @param index
	 *            테이블의 인덱스
	 * @param dateFormat
	 *            바뀐 시간 형식
	 * @return 테이블 출력을 위한 {@link String} 배열
	 */
	public String[] getStrings(int index, int dateFormat) {
		if (path != null) {
			// 디렉토리로 보기인 경우

			// 인덱스 변환
			index = convertIndex(index);

			// 정보를 출력할 ZipEntry
			ZipEntry entry = entryList.get(index);

			final String name = getEntryName(entry);
			String size = null;
			if (entry.isDirectory())
				// 디렉토리인 경우
				size = getSizeString(getDirSize(entry));
			else
				// 파일인 경우
				size = getSizeString(entry.getSize());
			final String type = getType(entry);
			final String time = getTimeString(entry.getTime(), dateFormat);

			return new String[] { name, size, type, time };
		} else {
			// 모든 파일 보기인 경우

			// 정보를 출력할 ZipEntry
			ZipEntry entry = entryList.get(index);

			String name = getEntryName(entry);
			String size = getSizeString(entry.getSize());
			String type = getType(entry);
			String time = getTimeString(entry.getTime(), dateFormat);
			String path = getEntryPath(entry);

			return new String[] { name, size, type, time, path };
		}

	}

	/**
	 * {@link ZipEntry}의 형식을 문자열로 반환하는 메소드
	 * 
	 * @param entry
	 *            {@link ZipEntry}
	 * @return {@link ZipEntry}의 형식을 나타내는 문자열
	 */
	private String getType(ZipEntry entry) {
		if (entry.isDirectory()) {
			return "디렉토리";
		}

		// 파일 이름
		final String entryName = getEntryName(entry);

		int index = -1;

		if ((index = entryName.lastIndexOf('.')) == -1)
			return "일반 파일";

		// 확장자
		final String extension = entryName.substring(index + 1).toLowerCase();

		if (extension.equals("jpg") || extension.equals("jpeg")
				|| extension.equals("bmp") || extension.equals("png")
				|| extension.equals("gif"))
			return "그림 파일";
		else if (extension.equals("mp3") || extension.equals("wav")
				|| extension.equals("ogg"))
			return "음악 파일";
		else if (extension.equals("avi") || extension.equals("mpg")
				|| extension.equals("mpeg"))
			return "동영상 파일";
		else if (extension.equals("txt"))
			return "텍스트  파일";
		else if (extension.equals("html") || extension.equals("htm"))
			return "HTML 파일";
		else if (extension.equals("zip") || extension.equals("gz")
				|| extension.equals("bz2") || extension.equals("rar")
				|| extension.equals("jar"))
			return "압축 파일";
		else
			return "일반 파일";
	}

	/**
	 * 부모 디렉토리로 현재 경로를 변경하는 메소드
	 */
	public void goToParent() {
		if (path.length() != 0)
			path = getParentEntryName(path);
	}

	/**
	 * 최상위 디렉토리로 현재 경로를 변경하는 메소드
	 */
	public void goToTop() {
		if (path.length() != 0)
			path = "";
	}

	/**
	 * 인덱스에 해당하는 ZipEntry가 디렉토리이면 true, 파일이면 false를 반환하는 메소드
	 * 
	 * @param index
	 *            테이블의 인덱스
	 * @return 디렉토리 여부
	 */
	public boolean isDirecotry(int index) {
		if (path != null)
			// 디렉토리로 보기
			index = convertIndex(index);

		return entryList.get(index).isDirectory();
	}

	/**
	 * Zip 파일의 바뀐 시간을 반환하는 메소드
	 * 
	 * @return Zip 파일의 바뀐 시간
	 */
	public long lastModified() {
		return file.lastModified();
	}

	/**
	 * {@link ZipEntry}를 읽어들이는 메소드
	 * 
	 * @param zipFile
	 *            {@link ZipFile}
	 */
	private void loadEntries() {
		// ZipEntry의 ArrayList
		entryList = new ArrayList<ZipEntry>();

		Enumeration<?> entries = zipFile.getEntries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			entryList.add(entry);
		}

		originalSize = entryList.size();

		if (path != null)
			// 디렉토리로 보기인 경우
			getMissingEntries();
	}

	/**
	 * 선택된 항목으로 현재 경로를 변경하는 메소드
	 * 
	 * @param index
	 *            테이블의 인덱스
	 */
	public void openDir(int index) {
		// 인덱스 변환
		index = convertIndex(index);

		// 경로 변경
		path = entryList.get(index).getName();
	}

	/**
	 * 압축 파일 내의 파일을 여는 메소드
	 * 
	 * @param index
	 *            테이블의 인덱스
	 */
	public void openFile(int index) {
		if (path != null)
			// 디렉토리로 보기인 경우
			index = convertIndex(index);

		// 임시 디렉토리 생성
		JZip.createTmpDir();

		// 열어야하는 ZipEntry
		ZipEntry zipEntry = entryList.get(index);

		// 임시 파일
		File tempFile = new File(JZip.tmpDir, getEntryName(zipEntry));

		if (tempFile.isDirectory())
			// 같은 이름의 디렉토리가 이미 존재하는 경우
			JZip.deleteDir(tempFile);

		// 임시 디렉토리에 압축 해제
		extract(zipEntry, tempFile);

		// 파일 열기
		Program.launch(tempFile.getPath());
	}

	/**
	 * Zip 파일 내의 파일을 열 프로그램을 선택하여 여는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param index
	 *            테이블의 인덱스
	 */
	public void openWith(Shell shell, int index) {
		if (path != null)
			// 디렉토리로 보기인 경우
			index = convertIndex(index);

		// 열어야하는 ZipEntry
		ZipEntry zipEntry = entryList.get(index);

		// 프로그램 선택 대화상자
		ProgramSelectDialog select = new ProgramSelectDialog(shell,
				getEntryName(zipEntry));
		final String command = select.open();

		if (command != null && (command.length() != 0)) {
			// 임시 디렉토리 생성
			JZip.createTmpDir();

			// 임시 파일
			File tempFile = new File(JZip.tmpDir, getEntryName(zipEntry));

			if (tempFile.isDirectory())
				// 같은 이름의 디렉토리가 이미 존재하는 경우
				JZip.deleteDir(tempFile);

			// 임시 디렉토리에 압축 해제
			extract(zipEntry, tempFile);

			// 선택된 프로그램 실행
			try {
				Runtime.getRuntime().exec(
						new String[] { command, tempFile.getPath() });
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * {@link ZipEntry}의 이름을 변경하는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param index
	 *            테이블의 인덱스
	 * @param newName
	 *            새로운 이름
	 */
	public void rename(final Shell shell, int index, String name) {
		// 새로운 항목 이름을 확인함
		final String[] characters = { "*", "|", "\\", ":", "\"", "<", ">", "?",
				"/" };

		for (String character : characters) {
			if (name.indexOf(character) != -1) {
				MessageBox messageBox = new MessageBox(shell, SWT.OK
						| SWT.ICON_ERROR);
				messageBox.setText("이름 바꾸기 실패!");
				messageBox.setMessage("항목 이름에 '" + character + "'가 포함되면 안됩니다.");
				messageBox.open();

				return;
			}
		}

		if (path != null)
			// 디렉토리로 보기인 경우
			index = convertIndex(index);

		// 이름을 변경할 ZipEntry
		final ZipEntry zipEntry = entryList.get(index);

		// 원래 이름
		final String originalName = zipEntry.getName();

		// 부모 엔트리 이름
		final String parentName = getParentEntryName(originalName);
		if (parentName.length() != 0)
			// 부모 엔트리가 있는 경우
			name = parentName + name;

		// 이름을 변경할 ZipEntry가 디렉토리이면 이름 끝에 '/'를 추가함
		if (zipEntry.isDirectory())
			name += "/";

		// 변경할 이름
		final String newName = name;

		// 변경할 ZipEntry의 인덱스
		final int newIndex = index;

		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {

			/**
			 * 같은 이름을 가진 항목이 있는지 확인하는 메소드
			 * 
			 * @param shell
			 *            Shell
			 * @param newName
			 *            확인할 이름
			 * @return 같은 이름을 가진 항목이 있으면 false, 없으면 true
			 */
			private boolean checkName(final Shell shell, final String newName) {
				for (ZipEntry entry : entryList) {
					String entryName = entry.getName();

					if (entry.isDirectory()) {
						// 디렉토리 엔트리인 경우

						if (!newName.endsWith("/"))
							entryName = entryName.substring(0,
									entryName.length() - 1);

						if (entryName.equals(newName)) {
							MessageBox messageBox = new MessageBox(shell,
									SWT.OK | SWT.ICON_ERROR);
							messageBox.setText("이름 바꾸기 실패!");
							messageBox.setMessage("같은 이름을 가진 디렉토리가 이미 존재합니다.");
							messageBox.open();

							return false;
						}
					} else {
						// 파일 엔트리인 경우
						if (newName.endsWith("/"))
							entryName += "/";

						if (entryName.equals(newName)) {
							MessageBox messageBox = new MessageBox(shell,
									SWT.OK | SWT.ICON_ERROR);
							messageBox.setText("이름 바꾸기 실패!");
							messageBox.setMessage("같은 이름을 가진 파일이 이미 존재합니다.");
							messageBox.open();

							return false;
						}
					}
				}

				return true;
			}

			public void run() {
				if (checkName(shell, newName)) {
					// 같은 이름을 가진 항목이 없는 경우

					// 임시 디렉토리 생성
					JZip.createTmpDir();

					// 임시 파일
					File tempFile = new File(JZip.tmpDir, file.getName());

					final String encoding = zipFile.getEncoding(); // 인코딩

					// 임시 Zip 파일 생성
					try {
						ZipOutputStream zos = new ZipOutputStream(tempFile);

						try {
							zos.setEncoding(encoding);

							for (int i = 0; i < entryList.size(); i++) {
								ZipEntry originalEntry = entryList.get(i);

								final String name = originalEntry.getName();

								ZipEntry entry = null;

								if (i == newIndex)
									entry = new ZipEntry(newName);

								if (entry == null && path != null) {
									// 디렉토리로 보기인 경우

									String parent = getParentEntryName(name);

									while (parent.length() != 0) {
										if (parent.equals(originalName)) {
											entry = new ZipEntry(newName
													+ name.substring(parent
															.length()));

											break;
										}

										parent = getParentEntryName(parent);
									}
								}

								if (entry == null)
									entry = new ZipEntry(name);

								// 바뀐 시간 설정
								long time = originalEntry.getTime();
								if (time != -1)
									entry.setTime(time);

								zos.putNextEntry(entry);

								if (!originalEntry.isDirectory())
									// 파일인 경우
									archive(zipFile
											.getInputStream(originalEntry), zos);

								zos.closeEntry();
							}
						} finally {
							zos.finish();
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

					// 임시 파일을 현재 파일에 복사
					copyFile(tempFile, file);

					// Zip 파일 새로 고침
					try {
						zipFile = new ZipFile(file, encoding);
					} catch (IOException e) {
						e.printStackTrace();
					}

					loadEntries();
				}

			}

		});
	}

	/**
	 * Zip 파일을 다른 이름으로 저장하는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param saveFile
	 *            저장할 {@link File}
	 * @param indices
	 *            테이블의 인덱스 배열
	 */
	public void save(final Shell shell, final File saveFile, final int[] indices) {
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {

			// 저장할 엔트리의 인덱스에 대한 ArrayList
			private ArrayList<Integer> indexList;

			/**
			 * 저장해야될 인덱스를 구하는 메소드
			 * 
			 * @param indices
			 *            테이블의 인덱스, null이면 모두 저장
			 */
			private void getIndexList(final int[] indices) {
				indexList = new ArrayList<Integer>();

				if (indices == null) {
					// 모든 항목 저장
					for (int i = 0; i < entryList.size(); i++)
						indexList.add(i);
				} else {
					if (path != null) {
						// 디렉토리로 보기인 경우

						// 인덱스 변환
						for (int i = 0; i < indices.length; i++)
							indices[i] = convertIndex(indices[i]);

						for (int index : indices) {
							ZipEntry dirEntry = entryList.get(index);

							if (dirEntry.isDirectory()) {
								// 디렉토리인 경우

								// 하위 디렉토리의 엔트리 추가
								final String dirName = dirEntry.getName();

								for (ZipEntry entry : entryList) {
									String entryPath = getParentEntryName(entry);

									while (entryPath.length() != 0) {
										if (entryPath.equals(dirName)) {
											indexList.add(entryList
													.indexOf(entry));

											break;
										}

										entryPath = getParentEntryName(entryPath);
									}
								}
							}
						}
					}

					for (int index : indices)
						indexList.add(index);
				}
			}

			public void run() {
				getIndexList(indices);

				save();
			}

			/**
			 * 실제로 저장하는 메소드
			 */
			private void save() {
				try {
					ZipOutputStream zos = new ZipOutputStream(saveFile);

					try {
						// 인코딩 설정
						zos.setEncoding(defaultEncoding);

						ProgressDialog progressDialog = new ProgressDialog(
								shell, ProgressMode.ARCHIVE, indexList.size());
						progressDialog.open();

						for (int i = 0; i < indexList.size(); i++) {
							ZipEntry originalEntry = entryList.get(indexList
									.get(i));
							ZipEntry entry = new ZipEntry(originalEntry
									.getName());

							// 바뀐 시간 설정
							long time = originalEntry.getTime();
							if (time != -1)
								entry.setTime(time);

							zos.putNextEntry(entry);

							if (!originalEntry.isDirectory())
								// 파일인 경우
								archive(zipFile.getInputStream(originalEntry),
										zos);

							zos.closeEntry();

							progressDialog.update(i + 1);
						}

						progressDialog.close();
					} finally {
						zos.finish();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});
	}

	/**
	 * Zip 파일 내의 모든 항목을 다른 이름으로 (압축하여) 저장하는 메소드
	 * 
	 * @param shell
	 *            {@link Shell}
	 * @param saveFile
	 *            저장할 {@link File}
	 */
	public void saveAll(Shell shell, File saveFile) {
		save(shell, saveFile, null);
	}

	/**
	 * Zip 파일 내의 현재 경로를 설정하는 메소드
	 * 
	 * @param text
	 *            Zip 파일 내의 현재 경로
	 */
	public void setPath(String text) {
		if (text.equals("/") || (text.length() == 0)) {
			path = "";

			return;
		}

		if (text.startsWith("/"))
			text = text.substring(1);

		if (!text.endsWith("/"))
			text += "/";

		for (ZipEntry entry : entryList) {
			if (entry.getName().equals(text)) {
				path = text;

				return;
			}
		}
	}

	public void sortByDir(final boolean reverse) {
		Collections.sort(entryList, new Comparator<ZipEntry>() {

			public int compare(ZipEntry o1, ZipEntry o2) {
				final boolean type1 = o1.isDirectory();
				final boolean type2 = o2.isDirectory();

				if (type1 && type2)
					return 0;

				if (!type1 && !type2)
					return 0;

				if (reverse) {
					if (type2 && !type1)
						return -1;
					else
						return 1;
				} else {
					if (type1 && !type2)
						return -1;
					else
						return 1;
				}
			}

		});
	}

	/**
	 * {@link ZipEntry}를 이름으로 정렬하는 메소드
	 * 
	 * @param reverse
	 *            역순 여부
	 */
	public void sortByName(final boolean reverse) {
		Collections.sort(entryList, new Comparator<ZipEntry>() {

			public int compare(ZipEntry o1, ZipEntry o2) {
				final String name1 = getEntryName(o1);
				final String name2 = getEntryName(o2);

				if (reverse)
					return name2.compareTo(name1);
				else
					return name1.compareTo(name2);
			}

		});
	}

	/**
	 * {@link ZipEntry}를 위치로 정렬하는 메소드
	 * 
	 * @param reverse
	 *            역순 여부
	 */
	public void sortByPath(final boolean reverse) {
		if (path != null) {
			return;
		}

		Collections.sort(entryList, new Comparator<ZipEntry>() {

			public int compare(ZipEntry o1, ZipEntry o2) {
				final String path1 = getEntryPath(o1);
				final String path2 = getEntryPath(o2);

				if (reverse)
					return path2.compareTo(path1);
				else
					return path1.compareTo(path2);
			}

		});
	}

	/**
	 * {@link ZipEntry}를 크기로 정렬하는 메소드
	 * 
	 * @param reverse
	 *            역순 여부
	 */
	public void sortBySize(final boolean reverse) {
		Collections.sort(entryList, new Comparator<ZipEntry>() {

			public int compare(ZipEntry o1, ZipEntry o2) {
				long size1 = 0;
				long size2 = 0;

				if (path == null) {
					// 모든 파일 보기
					size1 = o1.getSize();
					size2 = o2.getSize();
				} else {
					// 디렉토리로 보기
					if (o1.isDirectory())
						// 디렉토리인 경우
						size1 = getDirSize(o1);
					else
						// 파일인 경우
						size1 = o1.getSize();

					if (o2.isDirectory())
						// 디렉토리인 경우
						size2 = getDirSize(o2);
					else
						// 파일인 경우
						size2 = o2.getSize();
				}

				if (reverse)
					return (int) (size2 - size1);
				else
					return (int) (size1 - size2);
			}

		});
	}

	/**
	 * {@link ZipEntry}를 바뀐 시간으로 정렬하는 메소드
	 * 
	 * @param reverse
	 *            역순 여부
	 */
	public void sortByTime(final boolean reverse) {
		Collections.sort(entryList, new Comparator<ZipEntry>() {

			public int compare(ZipEntry o1, ZipEntry o2) {
				final long time1 = o1.getTime();
				final long time2 = o2.getTime();

				if (reverse)
					return (int) (time2 - time1);
				else
					return (int) (time1 - time2);
			}

		});
	}

	/**
	 * {@link ZipEntry}를 형태로 정렬하는 메소드
	 * 
	 * @param reverse
	 *            역순 여부
	 */
	public void sortByType(final boolean reverse) {
		Collections.sort(entryList, new Comparator<ZipEntry>() {

			public int compare(ZipEntry o1, ZipEntry o2) {
				final String type1 = getType(o1);
				final String type2 = getType(o2);

				if (reverse)
					return type2.compareTo(type1);
				else
					return type1.compareTo(type2);
			}

		});
	}
}