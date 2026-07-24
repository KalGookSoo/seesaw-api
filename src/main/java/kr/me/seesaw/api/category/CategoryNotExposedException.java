package kr.me.seesaw.api.category;

public class CategoryNotExposedException extends RuntimeException {

    public CategoryNotExposedException(String categoryId) {
        super("비노출 카테고리입니다. categoryId: " + categoryId);
    }

}
