package club.codefocus.framework.cache.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 随机生成IP
 * ClassName: IpUtils <br/>
 * Function:
 * Reason:IpUtils
 * date: 2019/10/25 10:37<br/>
 * @author jackl
 */
@Slf4j
public class IpUtils {

    /**
     * 获取请求IP
     */
    public static String getIpAddrExt(HttpServletRequest request) {
        String ip = getIpAddr(request);
        if (StringUtils.isNotBlank(ip)) {
            ip = ip.replace(":", ".");
        }
        return ip;
    }
    private static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 反向代理后会有多个ip值，第一个ip才是真实ip
            ip= StringUtils.split(ip,",")[0];
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }


}
