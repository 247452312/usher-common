package top.uhyils.usher.pojo.cqe;


import top.uhyils.usher.context.LoginInfoHelper;
import top.uhyils.usher.pojo.DTO.UserDTO;

/**
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年08月26日 08时23分
 */
public class DefaultCQE implements BaseCQE {

    /**
     * token
     */
    private String token;

    /**
     * 令牌登录
     */
    private String accessToken;

    /**
     * 请求时如果携带则代表已经有了,不需要解析token
     */
    private UserDTO user;

    /**
     * 保证请求幂等性, 不会在前一个相同幂等id执行结束前执行方法
     * <p>
     * 此标记只是防止请求的超时重发等操作,并不是业务上的幂等,业务上的幂等在业务上实现
     */
    private Long unique;

    public DefaultCQE(DefaultCQE request) {
        this.token = request.token;
        this.user = request.user;
        this.unique = request.unique;
        this.accessToken = request.accessToken;
    }

    public DefaultCQE() {
        // 默认是没有用户登录的
        LoginInfoHelper.getToken().ifPresent(this::setToken);
        LoginInfoHelper.get().ifPresent(this::setUser);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Long getUnique() {
        return unique;
    }

    public void setUnique(Long unique) {
        this.unique = unique;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void copyOf(DefaultCQE defaultCQE) {
        this.setUser(defaultCQE.user);
        this.setToken(defaultCQE.token);
        this.setUnique(defaultCQE.unique);
        this.setAccessToken(defaultCQE.accessToken);
    }

}
