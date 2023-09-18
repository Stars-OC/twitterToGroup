package xyz.starsoc;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import okhttp3.OkHttpClient;
import xyz.starsoc.event.GroupMsg;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;
import xyz.starsoc.twitter.TwitterThread;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class TwitterToGroup extends JavaPlugin{
    public static final TwitterToGroup INSTANCE = new TwitterToGroup();

    private TwitterToGroup(){
        super(new JvmPluginDescriptionBuilder("xyz.starsoc.twittertogroup","0.1.0")
                .name("TwitterToGroup")
                .author("Clusters_stars")
                .build());
    }

    @Override
    public void onEnable(){
        reload();
        //开启线程
        TwitterThread.run();

        GlobalEventChannel.INSTANCE.registerListenerHost(new GroupMsg());
        //监测okhttp
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        getLogger().info("TwitterToGroup加载成功");
    }


    public void reload(){
        reloadPluginConfig(Config.INSTANCE);
        reloadPluginData(TwitterInfo.INSTANCE);
    }
}