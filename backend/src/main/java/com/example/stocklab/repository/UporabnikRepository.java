package com.example.stocklab.repository;

import com.example.stocklab.model.Uporabnik;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UporabnikRepository extends JpaRepository<Uporabnik, UUID> {}