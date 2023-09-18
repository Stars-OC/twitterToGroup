package xyz.starsoc.twitter.data;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.starsoc.file.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Set;

public class TwitterUrl {

    public static String webUrl = "https://twitter.com";
    public static String userUrl = "https://twitter.com/i/api/graphql/rePnxwe9LZ51nQ7Sn_xN_A/UserByScreenName?variables=%7B%22screen_name%22%3A%22%s%22%2C%22withSafetyModeUserFields%22%3Atrue%2C%22withSuperFollowsUserFields%22%3Atrue%7D&features=%7B%22responsive_web_twitter_blue_verified_badge_is_enabled%22%3Atrue%2C%22responsive_web_graphql_exclude_directive_enabled%22%3Afalse%2C%22verified_phone_label_enabled%22%3Afalse%2C%22responsive_web_graphql_skip_user_profile_image_extensions_enabled%22%3Afalse%2C%22responsive_web_graphql_timeline_navigation_enabled%22%3Atrue%7D";
    public static String tweetsUrl = "https://twitter.com/i/api/graphql/H8OOoI-5ZE4NxgRr8lfyWg/UserTweets?variables=%7B%22userId%22%3A%22%s%22%2C%22count%22%3A20%2C%22includePromotedContent%22%3Atrue%2C%22withQuickPromoteEligibilityTweetFields%22%3Atrue%2C%22withVoice%22%3Atrue%2C%22withV2Timeline%22%3Atrue%7D&features=%7B%22responsive_web_graphql_exclude_directive_enabled%22%3Atrue%2C%22verified_phone_label_enabled%22%3Afalse%2C%22creator_subscriptions_tweet_preview_api_enabled%22%3Atrue%2C%22responsive_web_graphql_timeline_navigation_enabled%22%3Atrue%2C%22responsive_web_graphql_skip_user_profile_image_extensions_enabled%22%3Afalse%2C%22tweetypie_unmention_optimization_enabled%22%3Atrue%2C%22responsive_web_edit_tweet_api_enabled%22%3Atrue%2C%22graphql_is_translatable_rweb_tweet_is_translatable_enabled%22%3Atrue%2C%22view_counts_everywhere_api_enabled%22%3Atrue%2C%22longform_notetweets_consumption_enabled%22%3Atrue%2C%22responsive_web_twitter_article_tweet_consumption_enabled%22%3Afalse%2C%22tweet_awards_web_tipping_enabled%22%3Afalse%2C%22freedom_of_speech_not_reach_fetch_enabled%22%3Atrue%2C%22standardized_nudges_misinfo%22%3Atrue%2C%22tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled%22%3Atrue%2C%22longform_notetweets_rich_text_read_enabled%22%3Atrue%2C%22longform_notetweets_inline_media_enabled%22%3Atrue%2C%22responsive_web_media_download_video_enabled%22%3Afalse%2C%22responsive_web_enhance_cards_enabled%22%3Afalse%7D";
    public static String tweetUrl = "https://twitter.com/i/api/graphql/xOhkmRac04YFZmOzU9PJHg/TweetDetail?variables=%7B%22focalTweetId%22%3A%22%s%22%2C%22referrer%22%3A%22profile%22%2C%22with_rux_injections%22%3Afalse%2C%22includePromotedContent%22%3Atrue%2C%22withCommunity%22%3Atrue%2C%22withQuickPromoteEligibilityTweetFields%22%3Atrue%2C%22withBirdwatchNotes%22%3Atrue%2C%22withVoice%22%3Atrue%2C%22withV2Timeline%22%3Atrue%7D&features=%7B%22responsive_web_graphql_exclude_directive_enabled%22%3Atrue%2C%22verified_phone_label_enabled%22%3Afalse%2C%22creator_subscriptions_tweet_preview_api_enabled%22%3Atrue%2C%22responsive_web_graphql_timeline_navigation_enabled%22%3Atrue%2C%22responsive_web_graphql_skip_user_profile_image_extensions_enabled%22%3Afalse%2C%22tweetypie_unmention_optimization_enabled%22%3Atrue%2C%22responsive_web_edit_tweet_api_enabled%22%3Atrue%2C%22graphql_is_translatable_rweb_tweet_is_translatable_enabled%22%3Atrue%2C%22view_counts_everywhere_api_enabled%22%3Atrue%2C%22longform_notetweets_consumption_enabled%22%3Atrue%2C%22responsive_web_twitter_article_tweet_consumption_enabled%22%3Afalse%2C%22tweet_awards_web_tipping_enabled%22%3Afalse%2C%22freedom_of_speech_not_reach_fetch_enabled%22%3Atrue%2C%22standardized_nudges_misinfo%22%3Atrue%2C%22tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled%22%3Atrue%2C%22longform_notetweets_rich_text_read_enabled%22%3Atrue%2C%22longform_notetweets_inline_media_enabled%22%3Atrue%2C%22responsive_web_media_download_video_enabled%22%3Afalse%2C%22responsive_web_enhance_cards_enabled%22%3Afalse%7D&fieldToggles=%7B%22withArticleRichContentState%22%3Afalse%7D";
    public static boolean isMirror = false;

    private final Config config = Config.INSTANCE;
    private final Set<String> urls = config.getMirrorList();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyHost(), config.getProxyPort())))
            .build();

    private boolean ensureUrl() {
        for (String url : urls){
            //判断是否链接
            Request request = new Request.Builder()
                    .url(String.format(url,"X"))
                    .addHeader("Accept-Language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6")
                    .addHeader("Host","nitter.unixfox.eu")
                    .addHeader("Accept","*/*")
                    .build();
            Response execute = null;
            try {
                execute = client.newCall(request).execute();
            } catch (IOException e) {
                continue;
            }
            if (!execute.isSuccessful()){
                continue;
            }
            switch (execute.code()){
                case 200:
                case 201:
                    execute.body().close();
                    webUrl = url.substring(0,url.length()-1) + "%s";
                    userUrl = url + "%s";
                    tweetsUrl = url + "%s";
                    tweetUrl = url + "%s/status/%s";
                    isMirror = true;
                    return true;
                default:
                    break;
            }
            execute.close();
        }
        return false;
    }

    //获取Url
    public boolean initUrl(){
        if(ensureUrl()){
            return true;
        }
        String cookies = config.getCookies();
        if(cookies == ""){
            cookies = getCookies();
        }
        Request request = new Request.Builder()
                .url(String.format(userUrl,"X"))
                //host暂且不弄 TODO host的获取
                .addHeader("host","")
                .addHeader("cookie",cookies)
                .addHeader("authorization","Bearer AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA")
                .addHeader("x-csrf-token","68a3ab3626ca14c1586badcb1af0c02613a159172b82da4b9766503d411b86c03a1bde4a48618681c88ddbf232b8a55b54bc78b76d51a7ad0e75ed10ebb0d55e7209803a49b98e0215622b9cc2ce8fba")
                .build();
        Response execute = null;
        try {
            execute = client.newCall(request).execute();
        } catch (IOException e) {
            execute.close();
            return false;
        }
        if (!execute.isSuccessful()){
            execute.close();
            return false;
        }
        switch (execute.code()){
            case 200:
            case 201:
                return true;
            default:
                break;
        }
        execute.close();
        return false;
    }

    private String getCookies() {
        //用selenium获取cookies
        return "";
    }
}
