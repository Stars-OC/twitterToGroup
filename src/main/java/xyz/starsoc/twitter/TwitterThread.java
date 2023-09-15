package xyz.starsoc.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.starsoc.file.Config;
import xyz.starsoc.twitter.data.TwitterUrl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TwitterThread {
    private static Logger logger = LoggerFactory.getLogger("Twitter进程");
    public static void run() {
        Config config = Config.INSTANCE;
        //执行线程
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                //若没有处理好init就关闭
                if (new TwitterUrl().initUrl()){
                    logger.warn("Url获取失败。");
                    return;
                }
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 10, config.getTime(), TimeUnit.SECONDS);
    }
}
