package xyz.starsoc.twitter.data;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Set;

public class Twitter {

    private final HtmlParse parse = HtmlParse.INSTANCE;
    private final Config config = Config.INSTANCE;
    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<String,Long> user = info.getTwitterUser();
    private final Map<Long,Long> lastId = info.getLastTweetId();

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
            //这个要重写 以后要用的话 TODO
            return 1;
        }

        return 0;
    }

    public long getTwitterUserId(String username) throws IOException {

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

    private String getRefresh(String value) throws IOException {
        //因为是一样的前缀，故用同一个方法
        Call call = getCall(String.format(TwitterUrl.tweetsUrl, value));

        Response execute = call.execute();
        if(execute.isSuccessful() && execute.code() == 200){
            return execute.body().string();
        }

        return null;
    }
    public String getUserTweets(String username) throws IOException {

        Call call = getCall(String.format(TwitterUrl.tweetsUrl, username));

        Response execute = call.execute();
        if(!execute.isSuccessful()){
            return null;
        }

        String html = execute.body().string();
        if (execute.code() == 201){

            String newUrl = parse.getNewUrl(html);
            if(newUrl == null){
                return null;
            }

            String tweets = getUserTweets(newUrl);
            if(tweets == null){
                return null;
            }


        }else if(execute.code() == 200){

        }
    }
}
