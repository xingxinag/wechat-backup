public class ServerConfig {
    // 网络请求配置
    public static final int CONNECT_TIMEOUT = 10;
    public static final int READ_TIMEOUT = 15;
    public static final int WRITE_TIMEOUT = 15;
    
    // 重试配置
    public static final int MAX_RETRIES = 3;
    public static final int RETRY_DELAY = 1000; // 毫秒
    
    // GitHub Pages地址
    private static final String BASE_URL = "https://xingxinag.github.io/wechat-backup";
    
    // API端点
    public static final String ANNOUNCEMENT_URL = BASE_URL + "/api/announcement.json";
    public static final String AGREEMENT_URL = BASE_URL + "/api/agreement.txt";
    public static final String VERSION_URL = BASE_URL + "/api/version.json";
    
    public static String getWorkingUrl(String path) {
        return BASE_URL + path;
    }
} 