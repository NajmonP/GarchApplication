package com.example.garchapplication.service;

import com.example.garchapplication.exception.InvalidCredentialsException;
import com.example.garchapplication.mapper.UserMapper;
import com.example.garchapplication.model.dto.AuditInfoDTO;
import com.example.garchapplication.model.dto.api.ChangePasswordRequest;
import com.example.garchapplication.model.dto.api.UpdateUserRequest;
import com.example.garchapplication.model.dto.api.UserProfileDTO;
import com.example.garchapplication.model.entity.*;
import com.example.garchapplication.model.enums.EntityType;
import com.example.garchapplication.repository.UserRepository;
import com.example.garchapplication.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfigurationService configurationService;
    private final GarchModelService garchModelService;
    private final TimeSeriesService timeSeriesService;
    private final AuditLogService auditLogService;
    private final CalculationService calculationService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ConfigurationService configurationService, GarchModelService garchModelService, TimeSeriesService timeSeriesService, AuditLogService auditLogService, CalculationService calculationService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.configurationService = configurationService;
        this.garchModelService = garchModelService;
        this.timeSeriesService = timeSeriesService;
        this.auditLogService = auditLogService;
        this.calculationService = calculationService;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(UpdateUserRequest updateUserRequest) {
        User user = getUser();

        if (userRepository.existsByUsernameAndIdNot(updateUserRequest.username(), user.getId())) {
            throw new InvalidCredentialsException("Uživatelské jméno již existuje.");
        }
        if (userRepository.existsByEmailAndIdNot(updateUserRequest.email(), user.getId())) {
            throw new InvalidCredentialsException("Účet s emailovou adresou již existuje.");
        }

        user.setUsername(updateUserRequest.username());
        user.setEmail(updateUserRequest.email());
        auditLogService.logUpdateEvent(EntityType.USER, user.getId() ,user.getUsername());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = getUser();
        if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), user.getHashedPassword())) {
            throw new InvalidCredentialsException("Původní heslo se neshoduje.");
        }
        if(!changePasswordRequest.newPassword().equals(changePasswordRequest.confirmNewPassword())) {
            throw new InvalidCredentialsException("Nová hesla se neshodují.");
        }
        user.setHashedPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
        auditLogService.logUpdateEvent(EntityType.USER, user.getId() ,user.getUsername());
    }


    @Override
    @Transactional
    public void deleteUser() {
        User user = getUser();
        long userId = user.getId();
        String username = user.getUsername();
        userRepository.delete(user);
        logUserDeletion(userId, username);
    }

    private void logUserDeletion(long userId, String username) {
        List<AuditInfoDTO> auditInfoDTOS = configurationService.findAllAuditInfoByUserId(userId);
        for(AuditInfoDTO configurationInfoDTO : auditInfoDTOS){
            List<AuditInfoDTO> modelInfoList = garchModelService.findAllAuditInfoByConfigurationId(configurationInfoDTO.id());
            for(AuditInfoDTO modelInfoDTO : modelInfoList){
                auditLogService.logDeleteEvent(modelInfoDTO.type(), modelInfoDTO.id(), modelInfoDTO.name());
            }
        }
        auditInfoDTOS.addAll(timeSeriesService.findAllAuditInfoByUserId(userId));
        auditInfoDTOS.addAll(calculationService.findAllAuditInfoByUserId(userId));

        for(AuditInfoDTO auditInfoDTO : auditInfoDTOS){
            auditLogService.logDeleteEvent(auditInfoDTO.type(), auditInfoDTO.id(), auditInfoDTO.name());
        }
        auditLogService.logDeleteEvent(EntityType.USER, userId ,username);
    }

    @Override
    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public UserProfileDTO getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        Long userId = ((UserDetailsImpl) userDetails).getId();
        User user = getUserById(userId);
        return UserMapper.toUserProfileDTO(user);
    }

    @Transactional
    public User getUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        Long userId = ((UserDetailsImpl) userDetails).getId();
        return getUserById(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new UserDetailsImpl(user);
    }
}
