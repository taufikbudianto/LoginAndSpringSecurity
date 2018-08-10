/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Randy
 * @param <T>
 */
public class LazyDataModelJPA<T> extends LazyDataModel<T> {

    private JpaRepository<T, Serializable> repository;

    public LazyDataModelJPA(JpaRepository<T, Serializable> repository) {
        this.repository = repository;
    }

    @Override
    public T getRowData(String rowKey) {
        try {
            // Integer ID
            return repository.findOne(Integer.parseInt(rowKey));
        } catch (NumberFormatException e) {
            // String ID
            return repository.findOne(rowKey);
        }
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        this.setRowCount((int) getDataSize());
        this.setPageSize(pageSize);

        try {
            if (sortOrder == SortOrder.UNSORTED || sortField == null || sortField.isEmpty()) {
                PageRequest request = new PageRequest(first / pageSize, pageSize);
                return getDatas(request).getContent();
            } else {
                Sort sort = new Sort(sortOrder == SortOrder.ASCENDING
                        ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
                PageRequest request = new PageRequest(first / pageSize, pageSize, sort);
                return getDatas(request).getContent();
            }
        } catch (NullPointerException ne) {
            return null;
        }
    }
    
    /**
     * Override this method to change the behaviors of load data. For example, to load data with search parameters.
     * @param request pageRequest contains pagination and sorting default from primefaces datatable.
     * @return 
     */
    protected Page<T> getDatas(PageRequest request) {
        return repository.findAll(request);
    }
    
    /**
     * Override this method to change the behaviors of count the data. Basically, this method must be overrides if
     * the method getDatas() is also overrides.
     * @return 
     */
    protected long getDataSize() {
        return repository.count();
    }
    
}
