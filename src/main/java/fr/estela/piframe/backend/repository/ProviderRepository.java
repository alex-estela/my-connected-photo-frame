package fr.estela.piframe.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.estela.piframe.backend.entity.ProviderEntity;

public interface ProviderRepository extends JpaRepository<ProviderEntity, Long> {

}