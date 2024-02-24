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
    private static Logger logger = LoggerFactory.getLogger("TwitterThread");

    private final static Twitter twitter = Twitter.INSTANCE;
    private final static Config config = Config.INSTANCE;
    private final static TwitterUrl url = new TwitterUrl();
    private static boolean flag = false;

    public static void run() {
        logger.info("执行线程TwitterThread");

        //执行线程
        Runnable runnable = () -> {
            try{
                //若没有处理好init就关闭
                if(!flag){
                    if (!url.initUrl()){
                        logger.warn("Url获取失败。");
                        return;
                    }else {
                        flag = true;
                    }
                }

                if (config.getDebug()){
                    logger.info("正常执行Thread");
                }

                //开始进行执行 TODO 将源站的做出来
                if(!twitter.getGroupUser()){
                    logger.warn("未能获取到相关群聊信息");
                    return;
                }

                if(!twitter.getTweets()){
                    return;
                }

                logger.info("获取推文成功，发送到各群完毕");
            }catch (Exception e){
                e.printStackTrace();
                logger.error("TwitterThread执行失败 error: {}",e, e);
            }

        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 20, config.getTime(), TimeUnit.SECONDS);
    }
}
