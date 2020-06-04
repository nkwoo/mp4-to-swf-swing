import java.io.File;
import java.io.FileInputStream;
import java.io.IOException; 

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class FileUtil {

	public String getFileExtension(File file) {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		} else {
			return "";
		}
	}

	public Boolean checkFileMime(File file) {
		FileInputStream fileIs = null;
		try {
			fileIs = new FileInputStream(file);

			ContentHandler contenthandler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
			Parser parser = new AutoDetectParser();
			ParseContext parseContext = new ParseContext();

			parser.parse(fileIs, contenthandler, metadata, parseContext);
			System.out.println("Mime: " + metadata.get(Metadata.CONTENT_TYPE));
			if("video/mp4".equals(metadata.get(Metadata.CONTENT_TYPE))){			//mime 타입도 mp4인지
				return true;
			}else{
				return false;
			}

		} catch (IOException | SAXException | TikaException e) {
			e.printStackTrace();
		} finally {
			if (fileIs != null){
				try { fileIs.close(); } catch (IOException e) { e.printStackTrace(); }
			}
			return false;
		}

	}

	/*
	public void convertVideo(String filePath) {
		try {
			ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", "in.swf", filePath);
			Process p = pb.start();

			new Thread() {
				public void run() {

					Scanner sc = new Scanner(p.getErrorStream());

					// Find duration
					Pattern durPattern = Pattern.compile("(?<=Duration: )[^,]*");
					String dur = sc.findWithinHorizon(durPattern, 0);
					if (dur == null)
						throw new RuntimeException("Could not parse duration.");
					String[] hms = dur.split(":");
					double totalSecs = Integer.parseInt(hms[0]) * 3600 + Integer.parseInt(hms[1]) * 60
							+ Double.parseDouble(hms[2]);
					System.out.println("Total duration: " + totalSecs + " seconds.");

					// Find time as long as possible.
					Pattern timePattern = Pattern.compile("(?<=time=)[\\d.]*");
					String match;
					while (null != (match = sc.findWithinHorizon(timePattern, 0))) {
						double progress = Double.parseDouble(match) / totalSecs;
						System.out.printf("Progress: %.2f%%%n", progress * 100);
					}
				}
			}.start();
		} catch (IOException e) {
			// TODO: handle exception
		}
	}
	*/
}
