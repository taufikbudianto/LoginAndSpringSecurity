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
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.Size;
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
@Table(name = "psa_app_task")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"taskId"})
@ToString(exclude = {"childTaskAssociations"})
@NoArgsConstructor
public class Task implements Serializable {
    
    public enum Status {
        INACTIVE,
        ACTIVE,
        BETA_VERSION
    }
    
    public enum Type {
        MENU,
        ACTION,
        FIELD
    }
    
    public static final String DEFAULT_ROLE_TASK_PACKAGE = "taufik.app.web.controller.RoleMBean";
    public static final String TOP_LEVEL_MENU_TASK_ID = "TOP_LEVEL_MENU";
    public static final String SUB_MENU_ADMIN_TASK_ID = "ADMIN_MENU";
    
    @Id
    @Basic(optional = false)
    @Column(name = "task_id", length = 150)
    private String taskId;
    
    @Column(name = "task_name", length = 50)
    private String taskName;
    
    @Column(name = "task_type", length = 20)
    @Enumerated(EnumType.STRING)
    private Type taskType;
    
    @Size(max = 25)
    @Column(name = "help_text", length = 25)
    private String helpText;
    
    @Size(max = 25)
    @Column(name = "icon_name", length = 25)
    private String iconName;
    
    @Size(max = 50)
    @Column(name = "outcome", length = 50, unique = true)
    private String outcome;
    
    @Column(name = "status_")
    @Enumerated(EnumType.ORDINAL)
    private Task.Status status;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "parent")
    @OrderBy(value = "sequence_")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<TaskAssociation> childTaskAssociations;

    public Task(String taskId, String taskName, Type taskType) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskType = taskType;
    }
    
    /**
     * Add a new child to this Task object. As a note, after call this method, save method must be called 
     * from repository class to commit the new child to database. 
     * @param childTask
     * @param sequence
     * @throws java.lang.Exception when task_child is already exists.
     */
    public void addChild(Task childTask, Short sequence) throws Exception {
        TaskAssociation taskAssociation = new TaskAssociation(this, childTask, sequence);
        
        if(this.childTaskAssociations == null) {
            this.childTaskAssociations = new ArrayList<>();
        } else if (this.childTaskAssociations.contains(taskAssociation)) {
            throw new Exception("Task child is already exists.");
        }
        
        this.childTaskAssociations.add(taskAssociation);
    }
    
    /**
     * Remove a specific child from this Task object. As a note, after call this method, save method must 
     * be called from repository class to remove the child from database.
     * @param childTask 
     * @throws java.lang.Exception when task_child is not exists.
     */
    public void removeChild(Task childTask) throws Exception {
        if (this.childTaskAssociations == null) {
            throw new Exception("Task child is empty.");
        }
        
        TaskAssociation taskAssociation = new TaskAssociation(this, childTask, null);
        
        boolean result = this.childTaskAssociations.remove(taskAssociation);
        
        if (!result) {
            throw new Exception("Task child is not exists.");
        }
    }
    
    /**
     * Update a specific child's sequence from this Task object. As a note, after call this method, 
     * save method must be called from repository class to update the child in database.
     * @param childTask
     * @param sequence
     * @throws java.lang.Exception when task_child is not exists.
     */
    public void updateChild(Task childTask, Short sequence) throws Exception {
        if (this.childTaskAssociations == null) {
            throw new Exception("Task child is empty.");
        }
        
        TaskAssociation taskAssociation = new TaskAssociation(this, childTask, sequence);
        
        int index = this.childTaskAssociations.indexOf(taskAssociation);
        if (index != -1) {
            this.childTaskAssociations.get(index).setSequence(sequence);
        } else {
            throw new Exception("Task child is not exists.");
        }
    }
    
    /**
     * @return the children of this task.
     */
    public List<Task> getChildren() {
        List<Task> children = new ArrayList<>();
        if (this.childTaskAssociations != null) {
            for (TaskAssociation taskAssociation : this.childTaskAssociations) {
                children.add(taskAssociation.getChild());
            }
        }
        
        return children;
    }
    
    /**
     * @param type
     * @return the children of this task with the specific type.
     */
    public List<Task> getChildrenByType(Task.Type type) {
        List<Task> children = new ArrayList<>();
        if (this.childTaskAssociations != null) {
            for (TaskAssociation taskAssociation : this.childTaskAssociations) {
                if (taskAssociation.getChild().getTaskType().equals(type)) {
                    children.add(taskAssociation.getChild());
                }
            }
        }
        
        return children;
    }
    
    /**
     * @param status 
     * @return the children of this task based on its status.
     */
    public List<Task> getChildrenByStatus(Task.Status status) {
        List<Task> children = new ArrayList<>();
        if (this.childTaskAssociations != null) {
            for (TaskAssociation taskAssociation : this.childTaskAssociations) {
                if (taskAssociation.getChild().getStatus().equals(status)) {
                    children.add(taskAssociation.getChild());
                }
            }
        }
        
        return children;
    }
    
    /**
     * 
     * @param type
     * @param status
     * @return the children of this task based on its type and status.
     */
    public List<Task> getChildrenByTypeAndStatus(Task.Type type, Task.Status status) {
        List<Task> children = new ArrayList<>();
        if (this.childTaskAssociations != null) {
            for (TaskAssociation taskAssociation : this.childTaskAssociations) {
                if (taskAssociation.getChild().getStatus().equals(status) 
                        && taskAssociation.getChild().getTaskType().equals(type)) {
                    children.add(taskAssociation.getChild());
                }
            }
        }
        
        return children;
    }

    public List<TaskAssociation> getChildTaskList() {
        return Collections.unmodifiableList(this.childTaskAssociations != null ? 
                this.childTaskAssociations : new ArrayList<>());
    }
    
}
