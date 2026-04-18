package com.demo.employees.repository;


import com.demo.employees.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Employee} entities.
 * Provides standard CRUD operations plus custom queries for soft-delete filtering and name search.
 */
public interface EmployeeRepository extends JpaRepository<Employee,UUID> {

    /**
     * Retrieves a paginated list of active employees.
     *
     * @param pageable pagination and sorting parameters
     * @return a page of active employees
     */
    Page<Employee> findByActiveTrue(Pageable pageable);

    /**
     * Finds an active employee by its UUID.
     *
     * @param id the employee UUID
     * @return an {@link Optional} containing the employee if found and active, or empty otherwise
     */
    Optional<Employee> findByIdAndActiveTrue(UUID id);

    /**
     * Searches active employees by performing a case-insensitive partial match
     * across all four name fields (first name, middle name, paternal last name, maternal last name).
     *
     * @param name     the search term
     * @param pageable pagination and sorting parameters
     * @return a page of matching active employees
     */

    @Query("SELECT e FROM Employee e WHERE " +
            "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(e.middleName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(e.fatherName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(e.motherName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND e.active = true")
    Page<Employee> searchByNameActive(@Param("name") String name, Pageable pageable);

    /**
     * Searches all employees (including soft-deleted) by performing a case-insensitive partial match
     * across all four name fields (first name, middle name, paternal last name, maternal last name).
     *
     * @param name     the search term
     * @param pageable pagination and sorting parameters
     * @return a page of matching employees regardless of active status
     */
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(e.middleName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(e.fatherName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(e.motherName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Employee> searchByNameAll(@Param("name") String name, Pageable pageable);


}
