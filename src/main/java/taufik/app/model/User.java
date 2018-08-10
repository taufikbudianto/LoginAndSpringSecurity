/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author Randy
 */
@Entity
@Table(name = "psa_app_user")
@EqualsAndHashCode(callSuper = false, of = {"userId"})
@ToString(exclude = {"hashedPassword", "changePassword", "lastLogin", "userRoles", "userTenants"})
@NoArgsConstructor
public class User extends AbstractAuditingEntity implements Serializable, UserDetails {
    
    public enum AuthSource {
        INTERNAL,
        LDAP,
        SOCIAL_MEDIA
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id")
    @Getter
    @Setter
    private Integer userId;
    
    @Size(max = 100)
    @Basic(optional = false)
    @Column(name = "login", length = 100, unique = true)
    @Getter
    @Setter
    private String login;
    
    @Size(max = 60)
    @Column(name = "hashed_password", length = 60)
    @Setter
    private String hashedPassword;
    
    @Column(name = "auth_source", length = 15)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private AuthSource authenticationSource;
    
    @Column(name = "enabled")
    @Getter
    @Setter
    private Boolean enabled;
    
    @Column(name = "activated")
    @Getter
    @Setter
    private Boolean activated;
    
    @Column(name = "is_change_password")
    @Getter
    @Setter
    private Boolean changePassword;
    
    @JoinColumn(name = "default_tenant_id", referencedColumnName = "tenant_id")
    @ManyToOne
    @Getter
    @Setter
    private Tenant defaultTenant;
    
    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date lastLogin;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    @OrderBy(value = "primary_ desc")
    private List<UserRole> userRoles;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<TenantInvitedUser> userTenants;
    
    @Transient
    private List<SimpleGrantedAuthority> authorities;

    public User(String login, String hashedPassword) {
        this.login = login;
        this.hashedPassword = hashedPassword;
    }
    
    /**
     * Mapping a new Tenant to this User object. As a note, after call this method, save method must be called 
     * from repository class to commit the new Tenant to database. 
     * @param tenant
     * @param isAdmin
     * @throws java.lang.Exception when tenant is already exists in this user.
     */
    public void addTenant(Tenant tenant, boolean isAdmin) throws Exception {
        TenantInvitedUser userTenant = new TenantInvitedUser(tenant, this);
        userTenant.setAdministrator(isAdmin);
        
        if(this.userTenants == null) {
            this.userTenants = new ArrayList<>();
        } else if (this.userTenants.contains(userTenant)) {
            throw new Exception("Tenant is already exists in this User.");
        }
        
        this.userTenants.add(userTenant);
    }
    
    /**
     * Remove a specific Tenant that has been mapped to this User object. As a note, after call this method, 
     * save method must be called from repository class to remove the mapping Tenant from database.
     * @param tenant 
     * @throws java.lang.Exception when tenant is not exists in this user.
     */
    public void removeTenant(Tenant tenant) throws Exception {
        if (this.userTenants == null) {
            throw new Exception("Tenant is empty.");
        }
        
        TenantInvitedUser userTenant = new TenantInvitedUser(tenant, this);
        
        boolean result = this.userTenants.remove(userTenant);
        
        if (!result) {
            throw new Exception("Tenant is not exists in this User.");
        }
    }
    
    /**
     * Update a specific tenant's administrator value from this User object. As a note, after call this method, 
     * save method must be called from repository class to update the mapping Tenant in database.
     * @param tenant
     * @param isAdmin
     * @throws java.lang.Exception when tenant is not exists in this user.
     */
    public void updateTenant(Tenant tenant, boolean isAdmin) throws Exception {
        if (this.userTenants == null) {
            throw new Exception("Tenant is empty.");
        }
        
        TenantInvitedUser userTenant = new TenantInvitedUser(tenant, this);
        
        int index = this.userTenants.indexOf(userTenant);
        if (index != -1) {
            this.userTenants.get(index).setAdministrator(isAdmin);
        } else {
            throw new Exception("Tenant is not exists in this User.");
        }
    }
    
    /**
     * Mapping a new Role to this User object. As a note, after call this method, save method must be called 
     * from repository class to commit the new Role to database. 
     * @param role
     * @param isPrimary
     * @throws java.lang.Exception when role is already exists in this user.
     */
    public void addRole(Role role, boolean isPrimary) throws Exception {
        UserRole userRole = new UserRole(this, role);
        userRole.setPrimary(isPrimary);
        
        if (this.userRoles == null) {
            this.userRoles = new ArrayList<>();
        } else if (this.userRoles.contains(userRole)) {
            throw new Exception("Role is already exists in this User.");
        }
        
        this.userRoles.add(userRole);
    }
    
    /**
     * Remove a specific Role that has been mapped to this User object. As a note, after call this method, 
     * save method must be called from repository class to remove the mapping Role from database.
     * @param roleId 
     * @throws java.lang.Exception when role is not exists in this user.
     */
    public void removeRole(int roleId) throws Exception {
        if (this.userRoles == null) {
            throw new Exception("Role is empty.");
        }
        
        UserRole userRole = new UserRole(this.userId, roleId);
        
        boolean result = this.userRoles.remove(userRole);
        
        if (!result) {
            throw new Exception("Role is not exists in this User.");
        }
    }
    
    /**
     * Update the status of a specific user's role that has been mapped to this User object. As a note, after 
     * call this method, save method must be called from repository class to update the Role to database. 
     * @param roleId
     * @param isPrimary
     * @return 
     * @throws java.lang.Exception when role is not exists in this user.
     */
    public boolean updateRole(int roleId, boolean isPrimary) throws Exception { 
        if (this.userRoles == null) {
            throw new Exception("Role is empty.");
        }
        
        UserRole userRole = new UserRole(this.userId, roleId);
        
        int index = this.userRoles.indexOf(userRole);
        if (index != -1) {
            if (this.userRoles.get(index).getPrimary() != isPrimary) {
                this.userRoles.get(index).setPrimary(isPrimary);
                
                return true;
            }
        } else {
            throw new Exception("Role is not exists in this User.");
        }
        
        return false;
    }
    
    /**
     * @return list of roles from this user.
     */
    @JsonIgnore
    public List<Role> getRoles() {
        List<Role> roles = new ArrayList<>();
        if (this.userRoles != null) {
            for (UserRole userRole : this.userRoles) {
                roles.add(userRole.getRole());
            }
        }
        
        return roles;
    }
    
    /**
     * @return list of roles from this user.
     */
    @JsonIgnore
    public Role getFirstRole() {
        return (this.userRoles != null && !this.userRoles.isEmpty()) ? this.userRoles.get(0).getRole() : null;
    }

    public List<UserRole> getUserRoles() {
        return Collections.unmodifiableList(this.userRoles != null ? this.userRoles : new ArrayList<>());
    }

    public List<TenantInvitedUser> getUserInvitedTenants() {
        return Collections.unmodifiableList(this.userTenants != null ? this.userTenants : new ArrayList<>());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableCollection(this.authorities != null ? this.authorities : new ArrayList<>());
    }

    public void setAuthorities(List<SimpleGrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    public String getPassword() {
        return this.hashedPassword;
    }

    @Override
    public boolean isAccountNonExpired() {
        return getActivated();
    }

    @Override
    public boolean isAccountNonLocked() {
        return getActivated() && getEnabled() && getFirstRole() != null && getFirstRole().getEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return getEnabled();
    }
    
}
