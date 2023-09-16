package xyz.starsoc.message;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class BuildMessageChain {

    public static final BuildMessageChain INSTANCE = new BuildMessageChain();

    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<Long, Set<String>> groups = info.getTwitterToGroup();
    private final Config config = Config.INSTANCE;

    public MessageChainBuilder makeChain(Group group, ArrayList<byte[]> images){
        MessageChainBuilder messages = new MessageChainBuilder();
        for (byte[] image : images){
            Image pic = group.uploadImage(ExternalResource.create(image).toAutoCloseable());
            messages.append(pic);
        }

        return messages;
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
