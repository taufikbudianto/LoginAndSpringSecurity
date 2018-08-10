/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Randy
 */

@Entity
@Table(name = "psa_app_tenant_invited_user")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"tenantInvitedUserPK"})
@ToString(exclude = {"tenant", "user"})
@NoArgsConstructor
public class TenantInvitedUser implements Serializable {
    
    @EmbeddedId
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TenantInvitedUserPK tenantInvitedUserPK;
    
    @Column(name = "administrator")
    private Boolean administrator;
    
    @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", 
            insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Tenant tenant;
    
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", 
            insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private User user;

    public TenantInvitedUser(int tenantId, int userId) {
        this.tenantInvitedUserPK = new TenantInvitedUserPK(tenantId, userId);
    }

    public TenantInvitedUser(Tenant tenant, User user) {
        this.tenantInvitedUserPK = new TenantInvitedUserPK(tenant.getTenantId(), user.getUserId());
        this.tenant = tenant;
        this.user = user;
    }

}

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"tenantId", "userId"})
@ToString
@NoArgsConstructor
@AllArgsConstructor
class TenantInvitedUserPK implements Serializable {
    
    @Basic(optional = false)
    @Column(name = "tenant_id")
    private int tenantId;
    @Basic(optional = false)
    @Column(name = "user_id")
    private int userId;
    
}

