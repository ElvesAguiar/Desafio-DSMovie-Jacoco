package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;
    @Mock
    private CustomUserUtil userUtil;

    private UserEntity user;
    private String userName;

    private List<UserDetailsProjection> list;

    @BeforeEach
    void setUp() {
        user = UserFactory.createUserEntity();
        userName = user.getUsername();

        list = UserDetailsFactory.createCustomAdminUser(userName);

        Mockito.when(repository.findByUsername(ArgumentMatchers.any())).thenReturn(Optional.of(user));

    }

    @Test
    public void authenticatedShouldReturnUserEntityWhenUserExists() {
        Mockito.when(userUtil.getLoggedUsername()).thenReturn(userName);
        UserEntity result = service.authenticated();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getUsername(), user.getUsername());
        Assertions.assertEquals(result.getId(), user.getId());
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        Mockito.when(userUtil.getLoggedUsername()).thenThrow(UsernameNotFoundException.class);

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            UserEntity result = service.authenticated();
        });
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        Mockito.when(repository.searchUserAndRolesByUsername(userName)).thenReturn(list);
        UserDetails result = service.loadUserByUsername(userName);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getUsername(), user.getUsername());
    }

    @Test
    public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        list.clear();
        Mockito.when(repository.searchUserAndRolesByUsername(userName)).thenReturn(list);


        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            UserDetails result = service.loadUserByUsername(userName);
        });
    }
}
