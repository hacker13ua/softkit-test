package org.ysurovskyi.softkittest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ysurovskyi.softkittest.domain.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

}
