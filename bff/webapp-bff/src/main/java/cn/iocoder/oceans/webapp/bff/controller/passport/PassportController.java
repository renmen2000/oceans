package cn.iocoder.oceans.webapp.bff.controller.passport;

import cn.iocoder.oceans.core.exception.ServiceException;
import cn.iocoder.oceans.user.api.MobileCodeService;
import cn.iocoder.oceans.user.api.OAuth2Service;
import cn.iocoder.oceans.user.api.UserService;
import cn.iocoder.oceans.user.api.constants.ErrorCodeEnum;
import cn.iocoder.oceans.user.api.dto.OAuth2AccessTokenDTO;
import cn.iocoder.oceans.webapp.bff.annotation.PermitAll;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/passport")
public class PassportController {

    @Reference
    private OAuth2Service oauth2Service;
    @Reference
    private UserService userService;
    @Reference
    private MobileCodeService mobileCodeService;

    // TODO 功能：手机密码登陆
//    @PostMapping("/mobile/pwd/login")
//    public OAuth2AccessToken mobileLogin(@RequestParam("mobile") String mobile,
//                                         @RequestParam("password") String password) {
//        return oauth2Service.getAccessToken(clientId, clientSecret, mobile, password);
//    }

    /**
     * 手机号 + 验证码登陆
     *
     * @param mobile 手机号
     * @param code 验证码
     * @return 授权信息
     */
    @PermitAll
    @PostMapping("/mobile/login")
    public OAuth2AccessTokenDTO mobileRegister(@RequestParam("mobile") String mobile,
                                               @RequestParam("code") String code) {
        // 尝试直接授权
        OAuth2AccessTokenDTO accessTokenDTO;
        try {
            accessTokenDTO = oauth2Service.getAccessToken(mobile, code);
            return accessTokenDTO;
        } catch (ServiceException serviceException) {
            if (!serviceException.getCode().equals(ErrorCodeEnum.USER_MOBILE_NOT_REGISTERED.getCode())) { // 如果是未注册异常，忽略。下面发起自动注册逻辑。
                throw serviceException;
            }
        }
        // 上面尝试授权失败，说明用户未注册，发起自动注册。
        try {
            userService.createUser(mobile, code);
        } catch (ServiceException serviceException) {
            if (!serviceException.getCode().equals(ErrorCodeEnum.USER_MOBILE_ALREADY_REGISTERED.getCode())) { // 如果是已注册异常，忽略。下面再次发起授权
                throw serviceException;
            }
        }
        // 再次发起授权
        accessTokenDTO = oauth2Service.getAccessToken(mobile, code);
        return accessTokenDTO;
    }

    /**
     * 发送手机验证码
     *
     * @param mobile 手机号
     * @return 无
     */
    @PermitAll
    @PostMapping("mobile/send")
    public String mobileSend(@RequestParam("mobile") String mobile) {
        mobileCodeService.send(mobile);
        return null;
    }

    // TODO 功能：qq 登陆
    @PermitAll
    @PostMapping("/qq/login")
    public String qqLogin() {
        return null;
    }

    // TODO 功能：qq 绑定
    @PermitAll
    @PostMapping("/qq/bind")
    public String qqBind() {
        return null;
    }

    // TODO 功能：刷新 token

    // TODO 功能：退出，销毁 token
}