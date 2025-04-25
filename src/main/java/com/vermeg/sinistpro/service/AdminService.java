package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Admin;
import com.vermeg.sinistpro.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }
}