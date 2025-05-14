package com.racha.rachavoting.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.racha.rachavoting.model.Account;
import com.racha.rachavoting.repository.AccountRepository;
import com.racha.rachavoting.util.constants.AuthorityEnums;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Account createAdminAccount(String username, String rawPassword) {
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setAdmin(true);
        return accountRepository.save(account);
    }

    @Order(1)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        String authority = account.isAdmin()
                ? AuthorityEnums.ROLE_ADMIN.name()
                : AuthorityEnums.ROLE_USER.name();

        return new User(
                account.getUsername(),
                account.getPassword(),
                true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(authority)));
    }

    public boolean usernameExists(String username) {
        return accountRepository.existsByUsername(username);
    }
}