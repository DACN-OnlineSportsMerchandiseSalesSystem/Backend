package com.javaweb.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [Database Migration] Running DatabaseMigrationRunner to remove unique constraints...");
        
        // 1. Kiểm tra xem có UNIQUE index nào trên cột user_id của bảng carts không
        try {
            String checkUniqueIndexSql = "SELECT DISTINCT INDEX_NAME FROM information_schema.STATISTICS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'carts' AND COLUMN_NAME = 'user_id' AND NON_UNIQUE = 0";
            java.util.List<String> uniqueIndexes = jdbcTemplate.queryForList(checkUniqueIndexSql, String.class);

            if (uniqueIndexes.isEmpty()) {
                System.out.println(">>> [Database Migration] Kiểm tra bảng carts: Không phát hiện unique index trên user_id. Bỏ qua migration.");
                return;
            }

            System.out.println(">>> [Database Migration] Phát hiện unique index cần gỡ bỏ: " + uniqueIndexes);

            // 2. Tìm các ràng buộc khóa ngoại (foreign key/constraint) liên quan đến user_id và xóa trước nếu có
            try {
                String findIndexSql = "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'carts' AND COLUMN_NAME = 'user_id' " +
                        "AND CONSTRAINT_NAME <> 'PRIMARY'";
                java.util.List<String> constraints = jdbcTemplate.queryForList(findIndexSql, String.class);
                for (String constraint : constraints) {
                    try {
                        jdbcTemplate.execute("ALTER TABLE carts DROP FOREIGN KEY " + constraint);
                        System.out.println(">>> [Database Migration] Successfully dropped foreign key: " + constraint);
                    } catch (Exception ex) {
                        try {
                            jdbcTemplate.execute("ALTER TABLE carts DROP CONSTRAINT " + constraint);
                            System.out.println(">>> [Database Migration] Successfully dropped constraint: " + constraint);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            // 3. Sau khi đã gỡ bỏ khóa ngoại tham chiếu, tiến hành xóa các chỉ mục UNIQUE tìm thấy
            for (String indexName : uniqueIndexes) {
                try {
                    jdbcTemplate.execute("ALTER TABLE carts DROP INDEX " + indexName);
                    System.out.println(">>> [Database Migration] Successfully dropped unique index '" + indexName + "' on carts table.");
                } catch (Exception ex) {
                    System.out.println(">>> [Database Migration] Could not drop index '" + indexName + "': " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            // Tránh văng lỗi nếu database chưa được tạo hoàn chỉnh
            System.out.println(">>> [Database Migration] Bỏ qua kiểm tra migration: " + e.getMessage());
        }
    }
}
