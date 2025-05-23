package com.cupidmeet.userdetailsservice.security.service;

import java.util.Set;

public interface SecurityUserService {

    /**
     * Получить доменное имя пользователя.
     *
     * @return доменное имя пользователя.
     */
    String getDomainUsername();

    Set<String> getUserRoles();
}
