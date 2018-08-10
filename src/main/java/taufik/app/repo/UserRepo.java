/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import taufik.app.model.User;

/**
 *
 * @author Randy
 */
@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
    
    /**
     * Find User by its user_login.
     * @param login
     * @return 
     */
    public User findByLogin(String login);
    
    /**
     * Find list of users with specific user_login for display with pagination.
     * @param login
     * @param pageable
     * @return list of users that has the same tenant as the current tenant of the user's login.
     */
    @Query(value = "select ur.user from UserRole ur where ur.user.login like %?1% "
            + "and ur.role.tenant = ?#{principal.defaultTenant}")
    public Page<User> findAllByLoginAndCurrentTenant(String login, Pageable pageable);
    
    /**
     * Returns the number of entities available for the specific user_login.
     * @param login
     * @return the number of entities that has the same tenant as the current tenant of the user's login
     */
    @Query(value = "select count(ur) from UserRole ur where ur.user.login like %?1% "
            + "and ur.role.tenant = ?#{principal.defaultTenant}")
    public long countByLoginAndCurrentTenant(String login);
    
    /**
     * Find list of users for display with pagination.
     * @param pageable
     * @return list of users that has the same tenant as the current tenant of the user's login.
     */
    @Query(value = "select ur.user from UserRole ur where ur.role.tenant = ?#{principal.defaultTenant}")
    public Page<User> findAllByCurrentTenant(Pageable pageable);
    
    /**
     * Returns the number of entities available.
     * @return the number of entities that has the same tenant as the current tenant of the user's login
     */
    @Query(value = "select count(ur) from UserRole ur where ur.role.tenant = ?#{principal.defaultTenant}")
    public long countByCurrentTenant();
    
    /**
     * Find list of users with specific user_login for display with pagination.
     * @param login
     * @param pageable
     * @return list of users for the specific user's login.
     */
    public Page<User> findAllByLoginContaining(String login, Pageable pageable);
    
    /**
     * @param login
     * @return the number of entities available for the specific user_login.
     */
    public long countByLoginContaining(String login);
        
}
