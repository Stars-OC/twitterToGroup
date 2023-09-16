package xyz.starsoc.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.starsoc.file.Config;
import xyz.starsoc.twitter.data.Twitter;
import xyz.starsoc.twitter.data.TwitterUrl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TwitterThread {
    private static Logger logger = LoggerFactory.getLogger("Twitter进程");

    private final static Twitter twitter = Twitter.INSTANCE;

    public static void run() {
        Config config = Config.INSTANCE;
        //执行线程
        Runnable runnable = () -> {
            //若没有处理好init就关闭
            if (new TwitterUrl().initUrl()){
                logger.warn("Url获取失败。");
                return;
            }

            //开始进行执行 TODO 将源站的做出来
            if(!twitter.getGroupUser()){
                return;
            }

            logger.info("获取User成功，开始进行推文的获取");
            if(!twitter.getTweets()){
                return;
            }

            logger.info("获取推文成功，正在发送到各群");

        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 10, config.getTime(), TimeUnit.SECONDS);
    }
}
