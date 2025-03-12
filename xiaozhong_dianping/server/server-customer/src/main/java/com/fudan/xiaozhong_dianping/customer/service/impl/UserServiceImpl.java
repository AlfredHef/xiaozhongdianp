package com.fudan.xiaozhong_dianping.customer.service.impl;
import com.fudan.xiaozhong_dianping.customer.service.CaptchaService; // 引入验证码服务接口
import com.fudan.xiaozhong_dianping.customer.service.UserService;
import com.fudan.entity.User;
import com.fudan.xiaozhong_dianping.customer.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.fudan.xiaozhong_dianping.customer.enums.RegisterResult;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CaptchaService captchaService; // 注入验证码服务

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 用户名规则：只能包含字母、数字和下划线，长度为3-20位
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    // 密码强度规则：至少包含一个大写字母、一个小写字母、一个数字，长度为8-20位
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$");

    @Override
    public RegisterResult register(User user, String captchaId, String userInputCaptcha) {
        // 验证用户名是否符合规则
        if (!isValidUsername(user.getUsername())) {
            return RegisterResult.USERNAME_INVALID;
        }
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            return RegisterResult.USERNAME_EXISTS;
        }
        // 验证密码强度
        if (!isValidPassword(user.getPassword())) {
            return RegisterResult.PASSWORD_INVALID;
        }

        // 校验验证码（替换原来的固定值判断）
        if (!captchaService.validateCaptcha(captchaId, userInputCaptcha)) {
            return RegisterResult.CAPTCHA_INVALID;
        }

        // 对密码进行加密处理
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        // 插入新用户
        if (userMapper.insertUser(user) > 0) {
            return RegisterResult.SUCCESS;
        }
        return RegisterResult.USERNAME_EXISTS; // 插入失败，可能是用户名已存在
    }

    @Override
    public boolean login(String username, String password) {
        User user = userMapper.findByUsername(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 验证用户名是否符合规则
     * @param username 用户名
     * @return 是否符合规则
     */
    private boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证密码强度
     * @param password 密码
     * @return 密码强度是否符合要求
     */
    private boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}


//register 方法
//此方法主要用于新用户的注册流程，它会对用户输入的信息进行一系列的验证，若验证通过则将新用户信息插入到数据库中。以下是该方法的具体步骤：
//用户名格式验证：使用正则表达式 USERNAME_PATTERN 检查用户名是否仅包含字母、数字和下划线，且长度在 3 - 20 位之间。若不符合规则，注册失败。
//用户名唯一性检查：调用 userMapper.findByUsername 方法在数据库中查找是否已存在该用户名。若存在，注册失败。
//密码强度验证：使用正则表达式 PASSWORD_PATTERN 检查密码是否至少包含一个大写字母、一个小写字母、一个数字，且长度在 8 - 20 位之间。若不符合要求，注册失败。
//验证码验证：调用 captchaService.validateCaptcha 方法验证用户输入的验证码是否正确。若不正确，注册失败。
//密码加密：若上述验证都通过，使用 BCryptPasswordEncoder 对用户输入的密码进行加密处理。
//插入新用户：调用 userMapper.insertUser 方法将新用户信息插入到数据库中。若插入成功，注册成功；否则，注册失败。
//login 方法
//该方法用于用户登录，接收用户名和密码作为参数，通过以下步骤验证用户身份：
//查找用户：调用 userMapper.findByUsername 方法在数据库中查找该用户名对应的用户信息。
//密码验证：使用 passwordEncoder.matches 方法将用户输入的密码与数据库中存储的加密密码进行比对。若用户名存在且密码匹配，登录成功；否则，登录失败。