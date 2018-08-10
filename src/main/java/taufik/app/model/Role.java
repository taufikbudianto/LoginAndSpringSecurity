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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "psa_app_role")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, of = {"roleId"})
@ToString(exclude = {"roleTasks"})
@NoArgsConstructor
public class Role extends AbstractAuditingEntity implements Serializable {

    public static final String X_ROLE_SUPERADMIN_NAME = "SUPERADMIN";
    
    public enum AccessRight {
        NO_ACCESS,
        READ_ONLY,
        READ_WRITE
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "role_id")
    private Integer roleId;
    
    @Basic(optional = false)
    @Column(name = "role_identifier", length = 30, unique = true)
    private String roleIdentifier;
    
    @Column(name = "role_name", length = 50)
    private String roleName;
    
    @Column(name = "initial_access", length = 20)
    @Enumerated(EnumType.STRING)
    private AccessRight initialAccess;
    
    @Column(name = "enabled")
    private Boolean enabled;
    
    @Column(name = "system_")
    private Boolean system = false;
    
    @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id")
    @ManyToOne
    private Tenant tenant;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "role")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<RoleTask> roleTasks;

    public Role(String roleIdentifier, String roleName, AccessRight initialAccess, Tenant tenant) {
        this.roleIdentifier = roleIdentifier;
        this.roleName = roleName;
        this.initialAccess = initialAccess;
        this.tenant = tenant;
    }
    
    /**
     * Mapping a new Task to this Role object. The value of the access_right will be generated same as this 
     * Role's initial_access. As a note, after call this method, save method must be called from repository 
     * class to commit the new Task to database. 
     * @param taskId
     * @return 
     * @throws java.lang.Exception when task is already exists in this role.
     */
    public RoleTask addTask(String taskId) throws Exception {
        return addTask(taskId, this.initialAccess);
    }
    
    /**
     * Mapping a new Task to this Role object. The value of the access_right will be generated same as this 
     * Role's initial_access. As a note, after call this method, save method must be called from repository 
     * class to commit the new Task to database. 
     * @param task
     * @return 
     * @throws java.lang.Exception when task is already exists in this role.
     */
    public RoleTask addTask(Task task) throws Exception {
        return addTask(task, this.initialAccess);
    }
    
    /**
     * Mapping a new Task to this Role object. As a note, after call this method, save method must be called 
     * from repository class to commit the new Task to database. 
     * @param taskId
     * @param accessRight
     * @return 
     * @throws java.lang.Exception when task is already exists in this role.
     */
    public RoleTask addTask(String taskId, AccessRight accessRight) throws Exception {
        RoleTask roleTask = new RoleTask(taskId, this.roleId);
        roleTask.setAccessRight(accessRight);
        
        if (this.roleTasks == null) {
            this.roleTasks = new ArrayList<>();
        } else if (this.roleTasks.contains(roleTask)) {
            throw new Exception("Task is already exists in this Role.");
        }
        
        this.roleTasks.add(roleTask);
        
        return roleTask;
    }
    
    /**
     * Mapping a new Task to this Role object. As a note, after call this method, save method must be called 
     * from repository class to commit the new Task to database. 
     * @param task
     * @param accessRight
     * @return 
     * @throws java.lang.Exception when task is already exists in this role.
     */
    public RoleTask addTask(Task task, AccessRight accessRight) throws Exception {
        RoleTask roleTask = new RoleTask(task, this, accessRight);
        
        if (this.roleTasks == null) {
            this.roleTasks = new ArrayList<>();
        } else if (this.roleTasks.contains(roleTask)) {
            throw new Exception("Task is already exists in this Role.");
        }
        
        this.roleTasks.add(roleTask);
        
        return roleTask;
    }
    
    /**
     * Remove a specific Task that has been mapped to this Role object. As a note, after call this method, 
     * save method must be called from repository class to remove the mapping Task from database.
     * @param taskId 
     * @throws java.lang.Exception when task is not exists in this role.
     */
    public void removeTask(String taskId) throws Exception {
        if (this.roleTasks == null) {
            throw new Exception("Task is empty.");
        }
        
        RoleTask roleTask = new RoleTask(taskId, this.roleId);
        
        boolean result = this.roleTasks.remove(roleTask);
        
        if (!result) {
            throw new Exception("Task is not exists in this Role.");
        }
    }
    
    /**
     * Update a specific access_right of the task that has been mapped to this Role object. As a note, after call 
     * this method, save method must be called from repository class to update the mapping Task in database.
     * @param taskId
     * @param accessRight
     * @throws java.lang.Exception when task is not exists in this role.
     */
    public void updateTask(String taskId, AccessRight accessRight) throws Exception {
        if (this.roleTasks == null) {
            throw new Exception("Task is empty.");
        }
        
        RoleTask roleTask = new RoleTask(taskId, this.roleId);
        
        int index = this.roleTasks.indexOf(roleTask);
        if (index != -1) {
            if (this.roleTasks.get(index).getAccessRight() != null 
                    && !this.roleTasks.get(index).getAccessRight().equals(accessRight)) {
                this.roleTasks.get(index).setAccessRight(accessRight);
            }
        } else {
            throw new Exception("Task is not exists in this Role.");
        }
    }
    
    /**
     * @return list of tasks from this role.
     */
    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        if (this.roleTasks != null) {
            for (RoleTask roleTask : this.roleTasks) {
                tasks.add(roleTask.getTask());
            }
        }
        
        return tasks;
    }
    
    /**
     * Get all the task for this role with the specific type.
     * @param type
     * @return 
     */
    public List<Task> getTasksByType(Task.Type type) {
        List<Task> tasks = new ArrayList<>();
        
        if (this.roleTasks != null) {
            for (RoleTask roleTask : this.roleTasks) {
                if (roleTask.getTask().getTaskType().equals(type)) {
                    tasks.add(roleTask.getTask());
                }
            }
        }
        
        return tasks;
    }
    
    /**
     * Clear all tasks that has been mapped to this Role object. As a note, after call this method, 
     * save method must be called from repository class to delete the mapping Task in database.
     */
    public void clearRoleTasks() {
        if (this.roleTasks != null) {
            this.roleTasks.clear();
        }
    }

    public List<RoleTask> getRoleTasks() {
        return Collections.unmodifiableList(roleTasks != null ? roleTasks : new ArrayList<>());
    }
    
}
