package fr.estela.piframe.backend.sourcepack.smugmug;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.estela.piframe.backend.entity.ProviderEntity;

@Entity
@DiscriminatorValue("SMUGMUG")
public class SmugmugProviderEntity extends ProviderEntity {

	private String album;

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}
}