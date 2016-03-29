package fr.estela.piframe.backend.util;

public class FileUtils {
	
	public static org.springframework.http.MediaType getMediaContentType(fr.estela.piframe.backend.util.MediaType type) {
		if (type.equals(fr.estela.piframe.backend.util.MediaType.JPG)) return new org.springframework.http.MediaType("image", "jpeg");
		if (type.equals(fr.estela.piframe.backend.util.MediaType.GIF)) return new org.springframework.http.MediaType("image", "gif");
		if (type.equals(fr.estela.piframe.backend.util.MediaType.PNG)) return new org.springframework.http.MediaType("image", "png");
		return null;
	}

	public static String getMediaFileExtension(MediaType type) {
		if (type.equals(fr.estela.piframe.backend.util.MediaType.JPG)) return "jpg";
		if (type.equals(fr.estela.piframe.backend.util.MediaType.GIF)) return "gif";
		if (type.equals(fr.estela.piframe.backend.util.MediaType.PNG)) return "png";
		return null;
	}
}
