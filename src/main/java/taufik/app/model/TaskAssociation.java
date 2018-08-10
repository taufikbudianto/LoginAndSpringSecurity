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
@Table(name = "psa_app_task_association")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"taskAssociationPK"})
@ToString(exclude = {"parent", "child"})
@NoArgsConstructor
public class TaskAssociation implements Serializable {
    
    @EmbeddedId
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TaskAssociationPK taskAssociationPK;
    
    @Column(name = "sequence_")
    private Short sequence;
    
    @JoinColumn(name = "parent_id", referencedColumnName = "task_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Task parent;
    
    @JoinColumn(name = "child_id", referencedColumnName = "task_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Task child;

    public TaskAssociation(Task parent, Task child, Short sequence) {
        this.taskAssociationPK = new TaskAssociationPK(parent.getTaskId(), child.getTaskId());
        this.parent = parent;
        this.child = child;
        this.sequence = sequence;
    }
    
}

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"parentId", "childId"})
@ToString
@NoArgsConstructor
@AllArgsConstructor
class TaskAssociationPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "parent_id", length = 100)
    private String parentId;
    
    @Basic(optional = false)
    @Column(name = "child_id", length = 100)
    private String childId;
    
}
