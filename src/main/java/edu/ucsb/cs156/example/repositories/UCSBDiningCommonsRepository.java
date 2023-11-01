package edu.ucsb.cs156.example.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.ucsb.cs156.example.entities.UCSBDiningCommons;


@Repository
public interface UCSBDiningCommonsRepository extends CrudRepository<UCSBDiningCommons, String> {
 
}