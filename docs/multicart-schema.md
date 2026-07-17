# Multicart Schema Update

Run this against the MySQL database configured in `src/main/resources/application.properties`.

```sql
ALTER TABLE carts
    ADD COLUMN name VARCHAR(255) NULL,
    ADD COLUMN status VARCHAR(50) NULL,
    ADD COLUMN is_default BOOLEAN NULL,
    ADD COLUMN updated_at DATETIME NULL;

UPDATE carts
SET name = COALESCE(NULLIF(TRIM(name), ''), 'Gio hang cua toi'),
    status = COALESCE(status, 'ACTIVE'),
    is_default = COALESCE(is_default, TRUE)
WHERE status IS NULL
   OR name IS NULL
   OR TRIM(name) = ''
   OR is_default IS NULL;
```

Check whether `carts.user_id` still has a unique index:

```sql
SHOW INDEX FROM carts WHERE Column_name = 'user_id';
```

If the result contains a row with `Non_unique = 0`, drop that index before using multicart:

```sql
ALTER TABLE carts DROP INDEX <unique_index_name>;
```
