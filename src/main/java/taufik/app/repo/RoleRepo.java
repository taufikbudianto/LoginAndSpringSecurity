/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.repo;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import taufik.app.model.Role;

/**
 *
 * @author Randy
 */
@Repository
public interface RoleRepo extends JpaRepository<Role, Integer> {
    
    /**
     * Find list of roles for specific task and access right filter.
     * @param taskId
     * @param accessRight
     * @return 
     */
    @Query(value = "select rt.role from RoleTask rt where rt.task.taskId = ?1 and rt.accessRight <> ?2")
    public List<Role> findAllByTaskIdAndAccessRightNot(String taskId, Role.AccessRight accessRight);
    
    /**
     * Find list of roles and current tenant for display with pagination.
     * @param pageable
     * @return list of roles that has the same tenant as the current tenant of the user's login.
     */
    @Query(value = "select r from Role r where r.tenant = ?#{principal.defaultTenant}")
    public Page<Role> findAllByCurrentTenant(Pageable pageable);
    
    /**
     * Returns the number of entities available for current tenant.
     * @return the number of entities that has the same tenant as the current tenant of the user's login
     */
    @Query(value = "select count(r) from Role r where r.tenant = ?#{principal.defaultTenant}")
    public long countByCurrentTenant();
    
    /**
     * Find list of roles with specific role_name and current tenant for display with pagination.
     * @param roleName
     * @param pageable
     * @return list of roles that has the same tenant as the current tenant of the user's login.
     */
    @Query(value = "select r from Role r where r.roleName like %?1% and r.tenant = ?#{principal.defaultTenant}")
    public Page<Role> findAllByRoleNameAndCurrentTenant(String roleName, Pageable pageable);
    
    /**
     * Returns the number of entities available for the specific roleName and current tenant.
     * @param roleName
     * @return the number of entities that has the same tenant as the current tenant of the user's login.
     */
    @Query(value = "select count(r) from Role r where r.roleName like %?1% and r.tenant = ?#{principal.defaultTenant}")
    public long countByRoleNameAndCurrentTenant(String roleName);
    
    /**
     * @param roleName
     * @param pageable
     * @return list of roles with specific role_name for display with pagination.
     */
    public Page<Role> findAllByRoleNameContaining(String roleName, Pageable pageable);
    
    /**
     * @param roleName
     * @return the number of entities available for the specific roleName.
     */
    public long countByRoleNameContaining(String roleName);
    
    /**
     * @param roleIdentifier
     * @return a role with specific role_identifier.
     */
    public Role findOneByRoleIdentifier(String roleIdentifier);
    
    /**
     * @param enabled
     * @return list of roles with specific enabled status.
     */
    public List<Role> findAllByEnabled(boolean enabled);
    
    /**
     * @param enabled
     * @return list of roles with specific enabled status and current tenant of the user's login.
     */
    @Query(value = "select r from Role r where r.enabled = ?1 and r.tenant = ?#{principal.defaultTenant}")
    public List<Role> findAllByEnabledAndCurrentTenant(boolean enabled);
    
}
