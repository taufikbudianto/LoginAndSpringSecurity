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
@Table(name = "psa_app_user_role")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"userRolePK"})
@ToString(exclude = {"role", "user"})
@NoArgsConstructor
public class UserRole extends AbstractAuditingEntity implements Serializable {

    @EmbeddedId
    private UserRolePK userRolePK;
    
    @Column(name = "primary_")
    private Boolean primary;
    
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Role role;
    
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private User user;

    public UserRole(int userId, int roleId) {
        this.userRolePK = new UserRolePK(userId, roleId);
    }

    public UserRole(User user, Role role) {
        this.userRolePK = new UserRolePK(user.getUserId(), role.getRoleId());
        this.user = user;
        this.role = role;
    }
    
}

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"userId", "roleId"})
@ToString
@NoArgsConstructor
@AllArgsConstructor
class UserRolePK implements Serializable {

    @Basic(optional = false)
    @Column(name = "user_id")
    private int userId;
    
    @Basic(optional = false)
    @Column(name = "role_id")
    private int roleId;
    
}
