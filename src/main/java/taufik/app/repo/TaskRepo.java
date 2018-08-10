/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taufik.app.model.Task;

/**
 *
 * @author Randy
 */
@Repository
public interface TaskRepo extends JpaRepository<Task, String> {
    
    /**
     * @param taskType
     * @return list of task with specific task_type and no status filter
     */
    public List<Task> findAllByTaskType(Task.Type taskType);
    
    /**
     * @param outcome
     * @return 
     */
    public Task findOneByOutcome(String outcome);
    
}
