package xyz.starsoc.message;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;

import java.util.*;

public class BuildMessageChain {

    public static final BuildMessageChain INSTANCE = new BuildMessageChain();

    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<Long, Set<String>> groups = info.getTwitterToGroup();
    private final Config config = Config.INSTANCE;


    public ArrayList<MessageChain> makeTextChain(String text){
        //TODO 用图片装
        //防止字数过长
        ArrayList<MessageChain> set = new ArrayList<>();
        for (int i = 0;i < text.length();i+=3000){
            MessageChainBuilder builder = new MessageChainBuilder();
            if(text.length() < i + 3000){
                builder.append(text.substring(i));
            }else {
                builder.append(text.substring(i, i + 3000));
            }
            set.add(builder.build());
        }
        return set;
    }

    public Group getGroup(long groupId){
        //TODO -> 后期整多个bot支持
        Bot bot = Bot.getInstance(config.getBot());
        return bot.getGroupOrFail(groupId);
    }
    public long getTwitterGroup(String username){
        //TODO 后期将其进行更改 -> Q：多个群聊相同的username怎么办
        for (long groupId : groups.keySet()){
            if(groups.get(groupId).contains(username)){
                return groupId;
            }
        }
        return 0;
    }

}
