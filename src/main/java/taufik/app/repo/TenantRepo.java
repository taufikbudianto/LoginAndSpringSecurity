/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taufik.app.model.Tenant;

/**
 *
 * @author Randy
 */
@Repository
public interface TenantRepo extends JpaRepository<Tenant, Integer> {
    
    /**
     * Find Tenant by its tenant_name
     * @param tenantName
     * @return 
     */
    public Tenant findTop1ByTenantName(String tenantName);
    
    /**
     * @param enabled
     * @return list of all active tenants.
     */
    public List<Tenant> findAllByEnabled(Boolean enabled);
    
}
