/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.controller;

import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import taufik.app.model.Role;
import taufik.app.model.Task;
import taufik.app.model.User;
import taufik.app.repo.RoleRepo;
import taufik.app.repo.UserRepo;
import taufik.app.service.UserService;
import taufik.app.web.model.SecureItem;
import taufik.app.web.util.AbstractManagedBean;
import taufik.app.web.util.LazyDataModelJPA;

/**
 *
 * @author Randy
 */
@Controller
@Scope("view")
public class UserMBean extends AbstractManagedBean implements InitializingBean {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Getter
    @Setter
    private DualListModel<Role> roles;
    @Getter
    private LazyDataModelJPA<User> usersList;
    @Getter
    private List<Role> availableRoles;
    @Getter
    private List<SelectItem> rolesList;
    @Getter
    @Setter
    private List<User> selectedUsers;
    @Getter
    @Setter
    private Role selectedRole;
    @Getter
    private User userDetail;

    @Getter
    @Setter
    private boolean isGeneratedPassword;
    @Getter
    private boolean isMultipleRoles;
    @Getter
    private boolean isShowDetail;
    @Getter
    @Setter
    private String fullNameInput;
    @Getter
    @Setter
    private String userNameFilter;
    @Getter
    @Setter
    private String passwordInput;

    @Override
    protected List<SecureItem> getSecureItems() {
        List<SecureItem> secureItems = new ArrayList<>();

        secureItems.add(new SecureItem("CreateButton", Task.Type.ACTION));
        secureItems.add(new SecureItem("LockButton", Task.Type.ACTION));
        secureItems.add(new SecureItem("UnlockButton", Task.Type.ACTION));
        secureItems.add(new SecureItem("SaveButton", Task.Type.ACTION));

        secureItems.add(new SecureItem("UsernameInput", Task.Type.FIELD));
        secureItems.add(new SecureItem("PasswordInput", Task.Type.FIELD));
        secureItems.add(new SecureItem("FullNameInput", Task.Type.FIELD));
        secureItems.add(new SecureItem("RoleInput", Task.Type.FIELD));
        secureItems.add(new SecureItem("WarehouseInput", Task.Type.FIELD));
        secureItems.add(new SecureItem("GeneratePasswordCheckBox", Task.Type.FIELD));
        secureItems.add(new SecureItem("MustChangePasswordCheckBox", Task.Type.FIELD));

        return secureItems;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        boolean isSystemAdmin = getCurrentUser().getFirstRole() != null
                && getCurrentUser().getFirstRole().getSystem();

        // initiate users_list
        if (isSystemAdmin) {
            this.usersList = new LazyDataModelJPA(userRepo) {
                @Override
                protected long getDataSize() {
                    if (userNameFilter != null && !userNameFilter.isEmpty()) {
                        return userRepo.countByLoginContaining(userNameFilter);
                    } else {
                        return super.getDataSize();
                    }
                }

                @Override
                protected Page getDatas(PageRequest request) {
                    if (userNameFilter != null && !userNameFilter.isEmpty()) {
                        return userRepo.findAllByLoginContaining(userNameFilter, request);
                    } else {
                        return super.getDatas(request);
                    }
                }

            };
        } else {
            this.usersList = new LazyDataModelJPA(userRepo) {
                @Override
                protected long getDataSize() {
                    if (userNameFilter != null && !userNameFilter.isEmpty()) {
                        return userRepo.countByLoginAndCurrentTenant(userNameFilter);
                    } else {
                        return userRepo.countByCurrentTenant();
                    }
                }

                @Override
                protected Page getDatas(PageRequest request) {
                    if (userNameFilter != null && !userNameFilter.isEmpty()) {
                        return userRepo.findAllByLoginAndCurrentTenant(userNameFilter, request);
                    } else {
                        return userRepo.findAllByCurrentTenant(request);
                    }
                }

            };
        }

        // initiate available_roles
        this.availableRoles = isSystemAdmin ? roleRepo.findAllByEnabled(true)
                : roleRepo.findAllByEnabledAndCurrentTenant(true);
        if (!this.isMultipleRoles) {
            this.rolesList = new ArrayList<>();
            for (Role role : this.availableRoles) {
                this.rolesList.add(new SelectItem(role, role.getRoleName()));
            }
        }

        this.isShowDetail = false;
    }

    /**
     * Update status of the selected users.
     *
     * @param isUnlock
     */
    public void lockOrUnlockSelectedUsers(boolean isUnlock) {
        if (this.selectedUsers == null || this.selectedUsers.isEmpty()) {
            addPopUpMessage(FacesMessage.SEVERITY_WARN, getMessageLocale("error_header_wrong_input"),
                    getMessageLocale("error_user_no_user_selected"));

        } else {
            userService.updateUsersStatus(this.selectedUsers, isUnlock);
            addPopUpMessage(FacesMessage.SEVERITY_INFO, getMessageLocale("header_success"),
                    getMessageLocale("data_success_update"));
        }
    }

    /**
     * CLose pop-up message that triggered from method saveDetail, and change
     * the view back to user list.
     */
    public void closeSaveInformationDialog() {
        RequestContext.getCurrentInstance().execute("PF('saveNotification').hide()");
        hideDetail();
    }

    /**
     * Save user's detail or create a new user. This method will called the
     * pop-up message to informs the result.
     */
    public void saveDetail() {
        if (isSecureItemEditable("RoleInput") && this.roles != null
                && this.roles.getTarget().isEmpty()) {
            addPopUpMessage(FacesMessage.SEVERITY_WARN, getMessageLocale("error_header_wrong_input"),
                    getMessageLocale("error_user_empty_role_selecting"));
            return;
        }
//        if (isSecureItemEditable("WarehouseInput") && this.warehouses != null
//                && this.warehouses.getTarget().isEmpty()) {
//            addPopUpMessage(FacesMessage.SEVERITY_WARN, getMessageLocale("error_header_wrong_input"),
//                    getMessageLocale("error_user_empty_warehouse_selecting"));
//            return;
//        }

        try {
            if (this.userDetail.getUserId() == null) {
                this.userDetail.setEnabled(true);
                this.userDetail.setActivated(true);

                this.userDetail.setDefaultTenant(this.isMultipleRoles
                        ? this.roles.getTarget().get(0).getTenant()
                        : this.selectedRole.getTenant());
            }
            if (this.passwordInput != null) {
                this.userDetail.setHashedPassword(this.passwordEncoder.encode(this.passwordInput));
            }
            if (this.isMultipleRoles) {
            } else {
            }

            RequestContext.getCurrentInstance().execute("PF('saveNotification').show()");

        } catch (DuplicateKeyException dx) {
            addPopUpMessage(FacesMessage.SEVERITY_WARN, getMessageLocale("error_header"),
                    getMessageLocale("error_user_duplicate_username"));
            log.error("Failed save user's detail : ", dx);

        } catch (Exception ex) {
            addPopUpMessage(FacesMessage.SEVERITY_WARN, getMessageLocale("error_header"),
                    getMessageLocale("error_user_save"));
            log.error("Failed save user's detail : ", ex);
        }
    }
    public void loadDetail(User user) {
        this.isShowDetail = true;
        this.passwordInput = null;

        if (user != null) {
            this.userDetail = user;
            this.isGeneratedPassword = false;
            if (this.isMultipleRoles) {
                List<Role> sourceRoles = new ArrayList<>(this.availableRoles);
                List<Role> targetRoles = user.getRoles();
                sourceRoles.removeAll(targetRoles);
                this.roles = new DualListModel<>(sourceRoles, targetRoles);
                this.selectedRole = null;
            } else {
                this.roles = null;
                this.selectedRole = !user.getRoles().isEmpty() ? user.getRoles().get(0) : null;
            }

        } else {
            this.userDetail = new User();
            this.isGeneratedPassword = true;
            this.fullNameInput = null;
            if (this.isMultipleRoles) {
                this.roles = new DualListModel<>(this.availableRoles, new ArrayList<>());
            } else {
                this.roles = null;
            }
            this.selectedRole = null;
        }
        this.isGeneratedPassword = false;
    }

    public void hideDetail() {
        this.isShowDetail = false;
    }

}
