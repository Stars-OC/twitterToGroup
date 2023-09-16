package xyz.starsoc.event;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageContent;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupMsg extends SimpleListenerHost {

    private final Config config = Config.INSTANCE;
    private final Set<Long> group = config.getEnableGroup();
    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<Long, Set<String>> groups = info.getTwitterToGroup();

    @EventHandler
    public void onMessage(@NotNull GroupMessageEvent event) throws Exception {// 可以抛出任何异常, 将在 handleException 处理
        //只处理相关的命令
        Group groupObject = event.getGroup();
        long groupId = groupObject.getId();
        long userId = event.getSender().getId();

        if (!group.contains(groupId)){
            if(config.getMaster() != userId){
                return;
            }
        }

        MessageContent content = event.getMessage().get(PlainText.Key);
        if (content == null){
            return;
        }

        String message = content.contentToString();

        if (!message.startsWith("!twitter")){
            return;
        }

        String[] commands = message.split(" ");

        if(commands[1].equals("help")){
            //help
        }

        if (commands.length < 3){
            groupObject.sendMessage("参数不足，请检查参数");
            return;
        }

        switch (commands[1]){
            case "add":
                String addUsers = "";
                for(int i = 2;i < commands.length;i++){
                    String user = commands[i];
                    addUsers += user;
                    addTwitterUser(groupId,user);

                    if(i == commands.length-1){
                        break;
                    }
                    addUsers += " ";
                }
                groupObject.sendMessage("添加[" + addUsers + "]成功");
                return;
            case "delete":
                return;
            case "search":
                return;
            case "addPer":
                return;
            case "deletePer":
                return;
            default:
                groupObject.sendMessage("参数不足，请检查参数");
        }

    }

    private boolean addTwitterUser(long groupId,String user){
        if(!groups.containsKey(groupId)){
            groups.put(groupId,new HashSet<>());
        }
        return groups.get(groupId).add(user);
    }

}
