package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.LogAuditoria;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface LogAuditoriaRepository extends ListCrudRepository<LogAuditoria, String> {

    List<LogAuditoria> findTop50ByOrderByDataHoraDesc();
}
