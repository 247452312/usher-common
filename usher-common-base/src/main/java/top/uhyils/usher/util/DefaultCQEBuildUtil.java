package top.uhyils.usher.util;

import top.uhyils.usher.context.UsherContext;
import top.uhyils.usher.pojo.DTO.UserDTO;
import top.uhyils.usher.pojo.cqe.DefaultCQE;

/**
 * 创建一个默认的ADMIN用户的请求 此方法只用来作服务之间的调用
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年08月24日 07时25分
 */
public final class DefaultCQEBuildUtil {

    private DefaultCQEBuildUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取管理员的默认请求, 内部用
     *
     * @return 管理员的默认请求
     */
    public static DefaultCQE getAdminDefaultCQE() {
        DefaultCQE defaultRequest = new DefaultCQE();
        UserDTO user = getAdminUserDTO();
        defaultRequest.setUser(user);
        return defaultRequest;
    }

    /**
     * 获取默认用户
     *
     * @return
     */
    public static UserDTO getAdminUserDTO() {
        UserDTO user = new UserDTO();
        user.setId(UsherContext.ADMIN_USER_ID);
        user.setUsername("admin");
        return user;
    }

    /**
     * 用管理员身份填充一个请求
     *
     * @param t
     * @param <T>
     */
    public static <T extends DefaultCQE> void fillRequestByAdminRequest(T t) {
        DefaultCQE adminDefaultCqe = getAdminDefaultCQE();
        t.setUnique(adminDefaultCqe.getUnique());
        t.setUser(adminDefaultCqe.getUser());
    }

}
