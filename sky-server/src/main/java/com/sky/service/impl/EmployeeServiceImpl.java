package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {


    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;  // 使用final声明，提高不可变性

    // 构造函数注入
    public EmployeeServiceImpl(PasswordEncoder passwordEncoder,
                               EmployeeMapper employeeMapper) {
        this.passwordEncoder = passwordEncoder;
        this.employeeMapper = employeeMapper;
    }
    /**
     * 员工登录
     *
     * @param employeeLoginDTO 包含员工登录信息的DTO对象
     * @return 登录成功的员工实体对象
     */

    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
    // 从DTO对象中获取用户名
        String username = employeeLoginDTO.getUsername();
    // 从DTO对象中获取用户输入的明文密码
        String rawPassword = employeeLoginDTO.getPassword(); // 用户输入的明文密码

        //1、根据用户名查询数据库中的数据
    // 调用employeeMapper的getByUsername方法，根据用户名查询员工信息
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
    // 如果查询结果为null，说明用户名不存在
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 使用BCrypt的matches方法进行比对
        // 第一个参数是用户输入的明文密码，第二个参数是数据库中存储的BCrypt哈希密码
        if (!passwordEncoder.matches(rawPassword, employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }
//    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
//        String username = employeeLoginDTO.getUsername();
//        String password = employeeLoginDTO.getPassword();
//
//        //1、根据用户名查询数据库中的数据
//        Employee employee = employeeMapper.getByUsername(username);
//
//        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
//        if (employee == null) {
//            //账号不存在
//            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
//        }
//
//        //密码比对
//        // TODO 后期需要进行md5加密，然后再进行比对
//        if (!password.equals(employee.getPassword())) {
//            //密码错误
//            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
//        }
//
//        if (employee.getStatus() == StatusConstant.DISABLE) {
//            //账号被锁定
//            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
//        }
//
//        //3、返回实体对象
//        return employee;
//    }

}
