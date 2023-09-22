package xyz.starsoc.message;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;

import java.util.*;

public class BuildMessageChain {

    public static final BuildMessageChain INSTANCE = new BuildMessageChain();

    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<Long, Set<String>> groups = info.getTwitterToGroup();
    private final Config config = Config.INSTANCE;

    public static void main(Group group,ArrayList<MessageChain> chains) {
        ForwardMessageBuilder builder = new ForwardMessageBuilder(group);

    }

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

}
