package xyz.starsoc.twitter.data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;
import xyz.starsoc.object.Tweet;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParse {
    public static final HtmlParse INSTANCE = new HtmlParse();

    public static final HashMap<String, HashSet<Long>> tweets = new HashMap<>();

    public static final HashMap<String, HashSet<Long>> forwardGroup = new HashMap<>();

    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Config config = Config.INSTANCE;
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
        if(lastId.get(user) < tweet){
            lastId.put(user,tweet);
        }
    }

    public boolean setTweets(String html){
        //将获取到的推文id存起来并进行放送
        Document parse = Jsoup.parse(html);

        Elements tweets = parse.getElementsByClass("tweet-link");
        int tweetSize = tweets.size();
        if(tweetSize == 0){
            //logger.warn("尚未解析到该页面的推文");
            return false;
        }
        //初始User
        String user = parse.getElementsByClass("profile-card-username").get(0).text().substring(1);
        Elements pinned = parse.getElementsByClass("pinned");
        //logger.info(user);

        //先用总的判断有哪些进行了更新 转发推特修改
        int realSize = pinned.size();
        //这个是除去转发的推文 先去除明天再说
        while (!config.getEnableForward()){
            if(!tweets.get(realSize).attr("href").contains(user)){
                realSize++;
            }else {
                break;
            }
        }

        //TODO 适配转发的推文 -> 只需要一个User对象即可
        if(realSize >= tweetSize){
            //防止这货转发太多照成的问题
            return false;
        }

        String[] content = tweets.get(realSize).attr("href").split("/");
        if(!lastId.containsKey(user)){
            lastId.put(user,getTweetId(content));
            return false;
        }

        long oldId = lastId.get(user);
        //判断新的推文，然后取代非新的
        for (int i = pinned.size(); i < tweetSize; i++){

            //判断是否重复的user
            String text = tweets.get(i).attr("href");
            String[] link = text.split("/");
            //用时间戳重构比较

            if(!text.contains(user)){
                if(!config.getEnableForward()){
                    continue;
                }
//                if(!Twitter.ALL.contains(user)){
//                    user = link[1];
//                    oldId = lastId.get(user);
//                }
            }

            //转发的对应
            long newId = getTweetId(link);
            if(oldId < newId){
                addTweet(user,newId);
            }else {
                break;
            }

        }

        return true;
    }

}
