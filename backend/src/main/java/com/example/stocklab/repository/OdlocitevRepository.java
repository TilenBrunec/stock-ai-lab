package com.example.stocklab.repository;

import com.example.stocklab.model.Odlocitev;
import com.example.stocklab.model.OdlocitevId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OdlocitevRepository extends JpaRepository<Odlocitev, OdlocitevId> {}
