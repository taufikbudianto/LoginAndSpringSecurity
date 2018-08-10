/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 *
 * @author Randy
 */
@Entity
@Table(name = "psa_app_tenant")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"tenantId"})
@ToString(exclude = {"tenantInvitedUsers"})
@NoArgsConstructor
public class Tenant extends AbstractAuditingEntity implements Serializable {
    
    public enum UserLimitType{
        CONCURRENT_USER,
        REGISTER_USER
    }
    
    public static final String DEFAULT_TENANT_NAME = "PROJECT";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tenant_id")
    private Integer tenantId;
    
    @Column(name = "tenant_name", length = 100)
    private String tenantName;
    
    @Column(name = "max_users")
    private Short maxUsers;
    
    @Column(name = "user_limit_type", length = 20)
    @Enumerated(EnumType.STRING)
    private UserLimitType userLimitType;
    
    @Column(name = "enabled")
    private Boolean enabled;
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tenant")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<TenantInvitedUser> tenantInvitedUsers;

    public Tenant(String tenantName) {
        this.tenantName = tenantName;
    }

    public Tenant(String tenantName, Short maxUsers, UserLimitType userLimitType) {
        this.tenantName = tenantName;
        this.maxUsers = maxUsers;
        this.userLimitType = userLimitType;
    }

    public List<TenantInvitedUser> getTenantInvitedUsers() {
        return Collections.unmodifiableList(this.tenantInvitedUsers != null 
                ? this.tenantInvitedUsers : new ArrayList<>());
    }
    
}
