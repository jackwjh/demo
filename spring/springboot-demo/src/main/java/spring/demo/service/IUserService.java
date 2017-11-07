package spring.demo.service;

import spring.demo.dto.UserDto;

/**
 * Created by facheng on 16.03.17.
 */
public interface IUserService {

    void create(String name, Integer age);

    void deleteByName(String name);

    Long getAllUsers();

    void deleteAllUsers();

    UserDto getUserByName(String username);
}
