package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    /**
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置账号的状态，默认正常状态 1表示正常 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);
        // 设置密码，使用BCrypt加密默认密码123456
        employee.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));
        //设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        Long currentUserId = BaseContext.getCurrentId();
        employee.setCreateUser(currentUserId);
        employee.setUpdateUser(currentUserId);

        employeeMapper.insert(employee);
    }

}
