package fr.estela.piframe.backend.api;

public class MediaResource {

	private int localWidth;
	private int localHeight;
	private String localContentURI;
	
	public int getLocalWidth() {
		return localWidth;
	}
	public void setLocalWidth(int localWidth) {
		this.localWidth = localWidth;
	}
	public int getLocalHeight() {
		return localHeight;
	}
	public void setLocalHeight(int localHeight) {
		this.localHeight = localHeight;
	}
	public String getLocalContentURI() {
		return localContentURI;
	}
	public void setLocalContentURI(String localContentURI) {
		this.localContentURI = localContentURI;
	}
}