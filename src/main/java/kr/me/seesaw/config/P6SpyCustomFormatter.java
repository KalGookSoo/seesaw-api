package kr.me.seesaw.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

public class P6SpyCustomFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if ("statement".equals(category) && sql != null && !sql.trim().isEmpty()) {
            return sql;
        }
        return now + " | " + elapsed + "ms | " + category + " | connection " + connectionId + " | " + sql;
    }

}
