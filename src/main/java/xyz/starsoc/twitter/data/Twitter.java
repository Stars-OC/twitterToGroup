package xyz.starsoc.twitter.data;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;
import xyz.starsoc.message.BuildMessageChain;
import xyz.starsoc.object.Tweet;
import xyz.starsoc.object.UserToGroup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class Twitter {

    public static final Twitter INSTANCE = new Twitter();
    public static final HashSet<UserToGroup> ALL = new HashSet<>();
    public static Set<String> users = new HashSet<>();
    public static boolean isAdd = true;

    private static final Logger logger = LoggerFactory.getLogger("Twitter");

    private final HtmlParse parse = HtmlParse.INSTANCE;
    private final BuildMessageChain messageChain = BuildMessageChain.INSTANCE;
    private final Config config = Config.INSTANCE;
    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<String,Long> user = info.getTwitterUser();
    private final Map<Long, Set<String>> groups = info.getTwitterToGroup();
    private final HashMap<String, HashSet<Long>> tweets = HtmlParse.tweets;



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
        //TODO 多bot -> 还是直接对象存储
        int count = 0;
        int successful = 0;

        if(isAdd){
            isAdd = false;
            for(long group : groups.keySet()){

                for(String username : groups.get(group)){
                    UserToGroup userToGroup = new UserToGroup(username, group);
                    //logger.info(String.valueOf(userToGroup.hashCode()));
                    ALL.add(userToGroup);
                }

            }

            //防止重复获取 lambda优化
            users = ALL.stream().map(UserToGroup::getUsername).collect(Collectors.toSet());
//        for (UserToGroup userToGroup : ALL){
//            users.add(userToGroup.getUsername());
//        }
        }

        for (String username : users){
            try {
                count++;
                successful += getUserTweets(username) ? 1 : 0;
            } catch (IOException e) {
                logger.warn("推特用户 :" + username + " 不存在或者网络链接不上");
            }
        }
        if(successful == 0){
            if (count != 0){
                logger.warn("获取用户:" + count + " 失败");
            }
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

        ResponseBody body = execute.body();
        String html = body.string();
        if (execute.code() == 201){
            String newUrl = parse.getNewUrl(html);
            if(newUrl == null){
                return false;
            }

            String tweets = getRefresh(newUrl);
            if(tweets == null){
                return false;
            }

            body.close();
            return parse.setTweets(tweets);

        }else if(execute.code() == 200){

            body.close();
            return parse.setTweets(html);

        }
        return false;
    }

    private byte[] getImage(String url) {
        //获取站点就行
        Call call = getCall(String.format(TwitterUrl.webUrl, url));

        try {
            Response execute = call.execute();
            //TODO 将其进行刷新 -> 后面有问题再说
//            logger.info(execute.body().string());
//            logger.info(String.format(TwitterUrl.userUrl, url));
            if(execute.isSuccessful()){
//                logger.info("图片获取成功");
                ResponseBody body = execute.body();
                byte[] bytes = body.bytes();
                body.close();
                return bytes;
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    public boolean getTweet(UserToGroup userToGroup, long tweetId){
        String username = userToGroup.getUsername();
        String format = String.format(TwitterUrl.tweetUrl, username, tweetId);
//        logger.info(format);
        Call call = getCall(format);
        //TODO 将回复消息另写一个存储池
        try {

            Response execute = call.execute();
            if(!execute.isSuccessful()){
                logger.warn("非正常退出链接: " + format);
                execute.close();
                return false;
            }
            Tweet tweetObject = null;
            //TODO 出问题就改这边 将URL进行替换
            ResponseBody body = execute.body();
            String html = body.string();
            //logger.info(html);
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

            body.close();
            ArrayList<byte[]> list = new ArrayList<>();

            for(String imageUrl : tweetObject.getImage()){

                byte[] image = getImage(imageUrl);
                if(image == null){
                    continue;
                }

                list.add(image);
            }
            //logger.info("Image Size: " + list.size());
            long groupId = userToGroup.getGroup();

            //TODO 用合并消息进行发送
            Group group = messageChain.getGroup(groupId);
//            logger.info(group.getName());
            MessageChainBuilder messages = new MessageChainBuilder().append("用户 [" + username + "] 更新了推文{ID" + tweetId + "}:\n");
            String text = tweetObject.getText();
            //防止字数过长
            if(text.length() > 3000){
                ArrayList<MessageChain> arr = messageChain.makeTextChain(text);
                for (int i = 0;i < arr.size();i++){
                    group.sendMessage(arr.get(i));
                }

            }else {
                messages.append(text);
            }
            for (byte[] image : list){
                Image pic = group.uploadImage(ExternalResource.create(image).toAutoCloseable());
                messages.append(pic);
            }
            group.sendMessage(messages.build());

        } catch (IOException e) {

            return false;
        }

        return true;
    }

    public boolean getTweets(){
        int count = 0;
        int successful = 0;

        for (UserToGroup userToGroup : ALL){

            String username = userToGroup.getUsername();
            if(!tweets.containsKey(username)){
                continue;
            }

            for (long tweetId : tweets.get(username)){
                count++;
                //TODO 将推文进行保存然后多群聊推送
                successful += getTweet(userToGroup,tweetId) ? 1 : 0;
//                logger.info("test");
            }

        }

        if(successful == 0){
            if (count != 0){
                logger.info("获取推文: " + count + " 获取失败");
            }
            tweets.clear();
            return false;
        }

        //开始下一轮的tweet的获取
        tweets.clear();
        logger.info("成功获取推文 [" + successful + "/" + count + "]");
        return true;
    }

}
