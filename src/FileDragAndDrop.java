
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.text.DefaultCaret;
import javax.swing.JTextArea;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

public class FileDragAndDrop {

	private JFrame mainFrame;
	private JTextField fileDragZone;
	private JTextArea textArea;
	private JScrollPane textAreaScrollBar;
	private JProgressBar progressBar; 
	private FileUtil fileUtil = new FileUtil();
	private List<File> files = null;
	private int threadCheck = 0;
	 
	public FileDragAndDrop() {
		initialize();
		DragAndDrop();
		mainFrame.setVisible(true);
	}

	public void DragAndDrop() {
		TransferHandler th = new TransferHandler() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
				return true;
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean importData(JComponent comp, Transferable t) {
				try {
					files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					
					if (threadCheck == 1 ){
						JOptionPane.showMessageDialog(null, "현재 변환중인 파일이 있습니다.");
						return false;
					}
					
					if (files.size() > 1) {
						JOptionPane.showMessageDialog(null, "파일변환은 한개씩 가능합니다.");
						return false;
					}
					
					for (File file : files) {
						if (!("mp4".equals(fileUtil.getFileExtension(file)))) {
							JOptionPane.showMessageDialog(null, "MP4 파일이 아닙니다.");
							return false;
						}
						
						if (fileUtil.checkFileMime(file)) {
							JOptionPane.showMessageDialog(null, "변조된 MP4 파일입니다.");
							return false;
						} 
						
						textArea.append("파일 업로드 명 :" + file.getName() + "\n");
						textArea.append("파일 경로 :" + file.getParent() + "\n");
						textArea.append("파일 경로 :" + file.getPath() + "\n");
						textArea.append("파일 헤더 :" + file.getName().substring(0, file.getName().lastIndexOf(".")) + "\n"); 
							
						List<String> command = new ArrayList<String>();
						command.add("ffmpeg.exe");
						
						//파일입력
						command.add("-i");
						command.add(file.getPath());
						
						//오디오 샘플링 freq 설정
						command.add("-ar");
						command.add("44100");
						
						//오디오 동기화 
						command.add("-async");
						command.add("44100");
						
						//출력파일 덮어쓰기
						command.add("-y");
						
						//프레임 설정
						command.add("-r");
						command.add("29.970");
						
						//오디오 채널 수 설정
						command.add("-ac");
						command.add("2");
							
						//고정된 양자화 비율 사용 (VBR)
						command.add("-qscale");
						command.add("1");
						
						//변환 사이즈 고청
						//command.add("-s");
						//command.add("1024x768");
						
						//출력파일 위치 지정
						command.add(file.getParent() + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".swf");
						
						ProcessBuilder builder = new ProcessBuilder(command);

						try {
							Process process = builder.start();
							fileDragZone.setText("MP4 -> SWF 변환중...");
							new Thread() {
								public void run() { 
									threadCheck = 1;
									Scanner sc = new Scanner(process.getErrorStream());
 
									//현재 영상의 전체 시간
									Pattern durPattern = Pattern.compile("(?<=Duration: )[^,]*");
									String videoFullTime = sc.findWithinHorizon(durPattern, 0);
									
									if (videoFullTime == null) { 
										JOptionPane.showMessageDialog(null, "영상 파일의 전체 시간을 알 수 업습니다.");
										progressBar.setValue(0); 
										fileDragZone.setText("MP4 파일을 드래그해주세요!");
										textArea.append("오류 발생!\n");
										return;
									}
									
									String[] videoFullTimehms = videoFullTime.split(":");
									double totalSec = Double.parseDouble(videoFullTimehms[0]) * 3600 + Double.parseDouble(videoFullTimehms[1]) * 60 + Double.parseDouble(videoFullTimehms[2]);
									textArea.append( "영상 총 시간 :" + totalSec + "초 \n");

									// 현재 처리중인 전체 시간
									Pattern timePattern = Pattern.compile("(?<=time=)[^ ]*");
									String videoRealTime;
									while (null != (videoRealTime = sc.findWithinHorizon(timePattern, 0))) { 
										String[] videoRealTimehms = videoRealTime.split(":");
										double nowSec = Double.parseDouble(videoRealTimehms[0]) * 3600 + Double.parseDouble(videoRealTimehms[1]) * 60
												+ Double.parseDouble(videoRealTimehms[2]);
										textArea.append("진행률: " +  String.format("%.1f" , nowSec / totalSec * 100) +"% \n");
										textArea.setCaretPosition(textArea.getDocument().getLength());
										progressBar.setValue((int)(nowSec / totalSec * 100));
									} 
									durPattern = Pattern.compile("(?<=video:)[^ ]*");
									videoFullTime = sc.findWithinHorizon(durPattern, 0);
									
									if (videoFullTime != null) {
										progressBar.setValue(100);
										threadCheck = 0;
										textArea.append("작업완료! \n");
										JOptionPane.showMessageDialog(null, "파일 변환이 완료되었습니다.\n파일경로 : " + file.getParent() + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".swf");
									}
									
									progressBar.setValue(0); 
									fileDragZone.setText("MP4 파일을 드래그해주세요!");
									System.out.println(videoFullTime); 
									System.out.println("끝?");
								}
							}.start();  
							System.out.println("?");
						} catch (Throwable ex) {
							ex.printStackTrace();
							System.out.println("An error occured when executing ffmpeg");
						}
					}
					System.out.println("종료");
				} catch (UnsupportedFlavorException ex) {
					ex.getStackTrace();
				} catch (IOException ex) {
					ex.getStackTrace();
				}
				return true;
			}
		};
		fileDragZone.setTransferHandler(th);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mainFrame = new JFrame();
		mainFrame.setBounds(100, 100, 451, 378);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().setLayout(null);
		
		fileDragZone = new JTextField();
		fileDragZone.setBounds(12, 10, 410, 106);
		mainFrame.getContentPane().add(fileDragZone);
		fileDragZone.setHorizontalAlignment(SwingConstants.CENTER);
		fileDragZone.setEditable(false);
		fileDragZone.setText("MP4 파일을 드래그 해주세요!");
		fileDragZone.setColumns(10);
		
		textArea = new JTextArea();
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		textArea.setEditable(false);
		textAreaScrollBar = new JScrollPane(textArea);
		textAreaScrollBar.setBounds(12, 125, 410, 178);
		textAreaScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textArea.setBounds(12, 125, 410, 106);
		mainFrame.getContentPane().add(textAreaScrollBar);
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setBounds(12, 313, 411, 16);
		mainFrame.getContentPane().add(progressBar);
 
	}
}
