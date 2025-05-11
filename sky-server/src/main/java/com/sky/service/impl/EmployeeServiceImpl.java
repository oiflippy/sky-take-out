package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        // Long currentUserId = BaseContext.getCurrentId();
        // employee.setCreateUser(currentUserId);
        // employee.setUpdateUser(currentUserId);

        employeeMapper.insert(employee);
    }
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
                // select * from employee limit 0,10
        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total, records);
    }
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****"); // 密码脱敏
        return employee;
    }

     /**
     * 编辑员工信息
     *
     * @param employeeDTO
     */
    @Override
     /**
     * 更新员工信息，将传入的员工数据传输对象转换为实体对象后进行持久化更新。
     * 
     * <p>该方法会自动设置更新时间和更新人信息，并执行数据库更新操作。
     * 注意：当前实现中未启用员工存在性校验和用户名唯一性校验逻辑</p>
     *
     * @param employeeDTO 包含员工更新数据的数据传输对象，需包含有效ID和必要的业务字段
     */
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        
        /**
         * 将员工DTO对象属性复制到实体对象
         * 用于构建待更新的持久化实体
         */
        BeanUtils.copyProperties(employeeDTO, employee);
    
        // 根据ID查询原始员工信息
        Employee originalEmployee = employeeMapper.getById(employeeDTO.getId());
        if (originalEmployee == null) {
            // 理论上更新操作时员工应该存在，但作为防御性编程，进行检查
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
    
        // 只有当用户名实际发生改变时，才进行唯一性校验
        if (employeeDTO.getUsername() != null && !employeeDTO.getUsername().equals(originalEmployee.getUsername())) {
            Employee existingEmployee = employeeMapper.getByUsernameAndNotId(employeeDTO.getUsername(), employeeDTO.getId());
            if (existingEmployee != null) {
                // 使用已有的 MessageConstant.ALREADY_EXISTS
                throw new AccountLockedException(MessageConstant.ALREADY_EXISTS);
            }
        }
    
        /**
         * 设置审计字段：
         * 1. 更新时间设置为当前系统时间
         * 2. 更新人设置为当前线程绑定的操作用户ID
         */
        // employee.setUpdateTime(LocalDateTime.now());
        // employee.setUpdateUser(BaseContext.getCurrentId());
    
        /**
         * 执行数据库更新操作
         * 使用MyBatis Mapper将实体对象同步到数据库
         */
        employeeMapper.update(employee);
    }
     @Override
     public void logout(Employee employee) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
     }

}
