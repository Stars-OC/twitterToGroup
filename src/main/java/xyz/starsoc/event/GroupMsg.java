package xyz.starsoc.event;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;
import xyz.starsoc.TwitterToGroup;
import xyz.starsoc.file.Config;
import xyz.starsoc.file.TwitterInfo;
import xyz.starsoc.twitter.data.Twitter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupMsg extends SimpleListenerHost {

    private final Config config = Config.INSTANCE;
    private final Set<Long> group = config.getEnableGroup();
    private final TwitterInfo info = TwitterInfo.INSTANCE;
    private final Map<Long, Set<String>> groups = info.getTwitterToGroup();
    private final Map<Long, Set<Long>> per = info.getGroupPermission();

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

        String message = event.getMessage().contentToString();
        String command = message.toLowerCase();

        if (!(command.startsWith("!twitter") || command.startsWith("！twitter"))){
            return;
        }

        if (!per.containsKey(groupId)){
            per.put(groupId,new HashSet<>());
        }

        if (!per.get(groupId).contains(userId)){
            if(config.getMaster() != userId){
                return;
            }
        }

        String[] commands = message.split(" ");
        int commandLength = commands.length;
        if (commandLength == 2){

            switch (commands[1].toLowerCase()){
                case "help":
                    groupObject.sendMessage("======help=======" +
                            "\n!(！)twitter list 可以查看当前群聊订阅" +
                            "\n!(！)twitter add Twitter用户(不要带@) " +
                            "\n!(！)twitter addAll Twitter用户(不要带@)" +
                            "\n添加该Twitter用户至所有群聊" +
                            "\n!(！)twitter delete Twitter用户(不要带@)" +
                            "\n!(！)twitter deleteAll Twitter用户(不要带@)"+
                            "\n删除该Twitter用户至所有群聊" +
                            "\n!(！)twitter addPer 可以@群成员来添加使用权限" +
                            "\n以上弄多个用户 但是要有 \" \" (空格进行分别) ");
                    return;
                case "reload":
                    TwitterToGroup.INSTANCE.reload();
                    groupObject.sendMessage("插件重载成功");
                    return;
                case "list":
                    if(!groups.containsKey(groupId)){
                        groupObject.sendMessage("当前群聊没有推特订阅");
                        return;
                    }
                    String userList = "";
                    int i = 0;
                    for (String user : groups.get(groupId)){
                        userList += user;
                        if(++i == groups.get(groupId).size()-1){
                            break;
                        }
                        userList += " ";
                    }
                    groupObject.sendMessage("====当前群聊订阅(" + groupId + ")==== \n" + userList);
                    return;
                default:
                    break;
            }

        }

        if (commandLength < 3){
            groupObject.sendMessage("参数不足，请检查参数");
            return;
        }
        Set<Long> groupKey = groups.keySet();
        int groupSize = groupKey.size();
        switch (commands[1].toLowerCase()){
            case "add":
                String addUsers = "";
                for(int i = 2; i < commandLength; i++){
                    String user = commands[i];
                    addUsers += addTwitterUser(groupId,user)?user : "";

                    if(i == commandLength -1){
                        break;
                    }
                    addUsers += " ";
                }
                groupObject.sendMessage("添加推特用户[" + addUsers + "]成功");
                return;
            case "delete":
                String deleteUsers = "";
                for (int i = 2; i < commandLength; i++){
                    String user = commands[i];

                    deleteUsers += deleteTwitterUser(groupId,user)?user : "";

                    if(i == commandLength -1){
                        break;
                    }
                    deleteUsers += " ";
                }
                groupObject.sendMessage("删除推特用户[" + deleteUsers + "]成功");
                return;
            case "search":
                return;
            case "addper":
                String addUserPer = "";
                for(int i = 2; i < commandLength; i++){
                    long user = Long.parseLong(commands[i].replace("@",""));
                    addUserPer += addUserPer(groupId,user)?user+"" : "";

                    if(i == commandLength -1){
                        break;
                    }
                    addUserPer += " ";
                }
                groupObject.sendMessage("添加群聊 (" + groupId + ") 权限给 [" + addUserPer + "] 成功");
                return;
            case "deleteper":
                return;
            case "addall":
                // 添加订阅至所有
                String addAllUsers = "";
                for(int i = 2; i < commandLength; i++){
                    String user = commands[i];
                    int count = 0;
                    for(long group : groupKey){
                        count += addTwitterUser(group,user)?1 : 0;
                    }

                    addAllUsers += count > 0?user:"";
                    if(i == commandLength -1){
                        break;
                    }
                    addAllUsers += " ";
                }
                groupObject.sendMessage("添加推特用户[" + addAllUsers + "]成功");

                return;
            case "deleteall":
                String deleteAllUsers = "";
                for(int i = 2; i < commandLength; i++){
                    String user = commands[i];
                    int count = 0;
                    for(long group : groupKey){
                        count += deleteTwitterUser(group,user)?1 : 0;
                    }

                    deleteAllUsers += count > 0?user:"";
                    if(i == commandLength -1){
                        break;
                    }
                    deleteAllUsers += " ";
                }
                groupObject.sendMessage("删除推特用户[" + deleteAllUsers + "]成功");

                return;
            default:
                groupObject.sendMessage("参数不足，请检查参数");
        }

    }

    //TODO 将下面的重复进行优化
    private boolean addTwitterUser(long groupId,String user){
        if(!groups.containsKey(groupId)){
            groups.put(groupId,new HashSet<>());
        }
        Twitter.isChanged = true;
        return groups.get(groupId).add(user);
    }

    private boolean deleteTwitterUser(long groupId,String user){
        if(!groups.containsKey(groupId)){
            groups.put(groupId,new HashSet<>());
            return false;
        }
        Twitter.isChanged = true;
        return groups.get(groupId).remove(user);
    }

    private boolean addUserPer(long groupId,long userId){
        if(!per.containsKey(groupId)){
            per.put(groupId,new HashSet<>());
        }
        return per.get(groupId).add(userId);
    }

    private boolean deleteUserPer(long groupId,long userId){
        if(!per.containsKey(groupId)){
            per.put(groupId,new HashSet<>());
            return false;
        }

        return per.get(groupId).remove(userId);
    }
}
