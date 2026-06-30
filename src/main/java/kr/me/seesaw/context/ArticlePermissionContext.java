package kr.me.seesaw.context;

public interface ArticlePermissionContext {

    boolean isOwner(String id, String username);

}
