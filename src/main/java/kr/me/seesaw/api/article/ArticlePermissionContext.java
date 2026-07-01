package kr.me.seesaw.api.article;

public interface ArticlePermissionContext {

    boolean isOwner(String id, String username);

}
