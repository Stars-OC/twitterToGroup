package xyz.starsoc.twitter.data;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;
import xyz.starsoc.message.BuildMessageChain;
import xyz.starsoc.object.Tweet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

public class Twitter {

    public static final Twitter INSTANCE = new Twitter();

    private static Logger logger = LoggerFactory.getLogger("Twitter推送");

    private final HtmlParse parse = HtmlParse.INSTANCE;
    private final BuildMessageChain messageChain = BuildMessageChain.INSTANCE;
    private final Config config = Config.INSTANCE;
    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<String,Long> user = info.getTwitterUser();
    private final Map<Long, Set<String>> group = info.getTwitterToGroup();
    private final HashMap<String, HashSet<Long>> tweets = HtmlParse.tweets;
    private final ArrayList<Tweet> tweet = new ArrayList<>();


    private final OkHttpClient client = new OkHttpClient.Builder()
            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyHost(), config.getProxyPort())))
            .build();

    private Call getCall(String url){
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6")
                .addHeader("Host", "nitter.unixfox.eu")
                .addHeader("Accept", "*/*")
                .build();
        return client.newCall(request);
    }
    //这一块是源站进行获取
    private long getUserId(String value) throws IOException {

        Call call = getCall(String.format(TwitterUrl.userUrl, value));
        Response execute = call.execute();
        if(execute.isSuccessful() && execute.code() == 200){
            //这个要重写 以后要用的话 TODO 源站用户ID的获取
            return 1;
        }

        return 0;
    }

    public long getTwitterUserId(String username) throws IOException {
        //这个是对应的源站
        if (user.containsKey(username)){
            return user.get(username);
        }

        Call call = getCall(String.format(TwitterUrl.userUrl, username));
        Response execute = call.execute();
        if(!execute.isSuccessful()){
            return 0;
        }

        String html = execute.body().string();
        if (execute.code() == 201){

            String newUrl = parse.getNewUrl(html);
            if(newUrl == null){
                return 0;
            }

            return getUserId(newUrl);

        }else if(execute.code() == 200){
            return getUserId(username);

        }

        return 0;
    }

//    public boolean addTwitterUser(String username){
//
//    }

    private String getRefresh(String value) throws IOException {
        //因为是一样的前缀，故用同一个方法
        Call call = getCall(String.format(TwitterUrl.tweetsUrl, value));

        Response execute = call.execute();
        if(execute.isSuccessful() && execute.code() == 200){
            return execute.body().string();
        }

        return null;
    }

    public boolean getGroupUser(){
        int count = 0;
        int successful = 0;

        Set<String> all = new HashSet<>();
        for (Set<String> user : group.values()){
            for (String username : user){
                all.add(username);
            }
        }

        //防止重复获取
        for (String username : all){
            try {
                count++;
                successful += getUserTweets(username) ? 1 : 0;
            } catch (IOException e) {
                logger.warn("推特用户 :" + username + " 不存在或者网络链接不上");
            }
        }

        if(successful == 0){
            logger.warn("获取用户数:" + count + " 失败");
            return false;
        }

        logger.info("成功获取用户 [" + successful + "/" + count + "]");
        return true;
    }

    public boolean getUserTweets(String username) throws IOException {

        Call call = getCall(String.format(TwitterUrl.tweetsUrl, username));

        Response execute = call.execute();
        if(!execute.isSuccessful()){
            return false;
        }

        String html = execute.body().string();
        if (execute.code() == 201){

            String newUrl = parse.getNewUrl(html);
            if(newUrl == null){
                return false;
            }

            String tweets = getRefresh(newUrl);
            if(tweets == null){
                return false;
            }

            return parse.setTweets(tweets);

        }else if(execute.code() == 200){

            return parse.setTweets(html);

        }
        return false;
    }

    private byte[] getImage(String url) {
        //获取站点就行
        Call call = getCall(String.format(TwitterUrl.userUrl, url));

        try {
            Response execute = call.execute();
            //TODO 将其进行刷新 -> 后面有问题再说

            if(execute.isSuccessful()){
                return execute.body().bytes();
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    public boolean getTweet(String username,long tweetId){
        Call call = getCall(String.format(TwitterUrl.tweetUrl, username, tweetId));

        try {

            Response execute = call.execute();
            if(!execute.isSuccessful()){
                return false;
            }
            Tweet tweetObject = null;
            //TODO 出问题就改这边 将URL进行替换
            String html = execute.body().string();
            if (execute.code() == 201){

                String newUrl = parse.getNewUrl(html);
                if(newUrl == null){
                    return false;
                }

                String tweet = getRefresh(newUrl);
                if(tweet == null){
                    return false;
                }

                //需要将文字进行限制 -> 3000字
                tweetObject = parse.getTweet(tweet);


            }else if(execute.code() == 200){

                tweetObject = parse.getTweet(html);

            }

            ArrayList<byte[]> list = new ArrayList<>();
            for(String imageUrl : tweetObject.getImage()){

                byte[] image = getImage(imageUrl);
                if(image == null){
                    continue;
                }

                list.add(image);
            }

            long groupId = messageChain.getTwitterGroup(username);
            if (groupId == 0){
                return false;
            }

            Group group = messageChain.getGroup(groupId);
            MessageChainBuilder messages = messageChain.makeChain(group, list);
            MessageChain chain = messages.append(tweetObject.getText()).build();
            group.sendMessage(chain);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public boolean getTweets(){
        int count = 0;
        int successful = 0;

        for (String username : tweets.keySet()){

            for (long tweetId : tweets.get(username)){
                count++;
                successful += getTweet(username,tweetId) ? 1 : 0;
            }

        }

        if(successful == 0){
            logger.warn("获取推文数:" + count + " 失败");
            return false;
        }

        //开始下一轮的tweet的获取
        tweets.clear();
        logger.info("成功获取推文 [" + successful + "/" + count + "]");
        return true;
    }

}
