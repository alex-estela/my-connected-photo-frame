package fr.estela.piframe.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.estela.piframe.backend.entity.MediaEntity;

public interface MediaRepository extends JpaRepository<MediaEntity, UUID> {
	
	public MediaEntity findByRemoteId(String remoteId);
}