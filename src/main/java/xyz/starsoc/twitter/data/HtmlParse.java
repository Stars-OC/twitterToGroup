package xyz.starsoc.twitter.data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.starsoc.file.TwitterInfo;
import xyz.starsoc.object.Tweet;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParse {
    public static final HtmlParse INSTANCE = new HtmlParse();

    public static final HashMap<String, HashSet<Long>> tweets = new HashMap<>();

    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<String,Long> lastId = info.getLastTweetId();
    private static Logger logger = LoggerFactory.getLogger("HtmlParse");

    //将新网址进行访问
    public String getNewUrl(String html){

        Document parse = Jsoup.parse(html);
        Elements elements = parse.select("meta[http-equiv=refresh]");
        if(elements.size() == 0){
            return null;
        }

        Element meta = elements.get(0);
        String content = meta.attr("content");

        Pattern regex = Pattern.compile("URL='(.*?)'");
        Matcher matcher = regex.matcher(content);

        if (matcher.find()){
            return matcher.group(1).replace("/","");
        }

        return null;
    }

    private long getTweetId(String[] content){
        return Long.parseLong(content[content.length-1].replace("#m",""));
    }

    public Tweet getTweet(String html){
        Tweet tweet = new Tweet();
        Document parse = Jsoup.parse(html);

        Element mainBody = parse.getElementById("m");
        Element bodyText = mainBody.getElementsByClass("tweet-content media-body").get(0);

        Set<String> imageList = tweet.getImage();
        Elements images = mainBody.getElementsByClass("still-image");
        for (int i = 0;i< images.size();i++){
            String pic = images.get(i).attr("href");
            imageList.add(pic);
        }
//        logger.info("imagePath " + imageList.size());
        tweet.setText(bodyText.text());
        return tweet;
    }

    private void addTweet(String user,Long tweet){
        if(!tweets.containsKey(user)){
            tweets.put(user,new HashSet<>());
        }

        HashSet<Long> userTweets = tweets.get(user);
        userTweets.add(tweet);
        //为其添加最后获取的推文id 以防重复获取
        lastId.put(user,tweet);
    }

    public boolean setTweets(String html){
        //将获取到的推文id存起来并进行放送
        Document parse = Jsoup.parse(html);

        Elements tweets = parse.getElementsByClass("tweet-link");
        if(tweets.size() == 0){
            //logger.warn("尚未解析到该页面的推文");
            return false;
        }

        Elements pinned = parse.getElementsByClass("pinned");
        //先用总的判断有哪些进行了更新
        String[] content = tweets.get(0).attr("href").split("/");

        String user = content[1];
        if(!lastId.containsKey(user)){
            lastId.put(user,getTweetId(content));
            return false;
        }

        long oldId = lastId.get(user);
        //判断新的推文，然后取代非新的
        for (int i = pinned.size();i < tweets.size();i++){
            String[] link = tweets.get(i).attr("href").split("/");
            long newId = getTweetId(link);
            if(oldId < newId){
                addTweet(user,newId);
            }

        }

        return true;
    }

}
