package fr.estela.piframe.backend.route;

public class PaginatedDownloadConfig {

	private boolean lastPageDownloaded = true;
	private boolean providerUpdated = false;
	private int pageIndex = 1;
	
	public boolean isLastPageDownloaded() {
		return lastPageDownloaded;
	}
	public void setLastPageDownloaded(boolean lastPageDownloaded) {
		this.lastPageDownloaded = lastPageDownloaded;
	}
	public boolean isProviderUpdated() {
		return providerUpdated;
	}
	public void setProviderUpdated(boolean providerUpdated) {
		this.providerUpdated = providerUpdated;
	}
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
}