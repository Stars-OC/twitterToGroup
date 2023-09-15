package xyz.starsoc.twitter.data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.starsoc.TwitterToGroup;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParse {
    public static final HtmlParse INSTANCE = new HtmlParse();

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

    public

}
