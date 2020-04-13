# --- !Ups

INSERT INTO "category"("name") VALUES("sample1");
INSERT INTO "category"("name") VALUES("sample2");
INSERT INTO "product_category"("productId", "categoryId") VALUES(2, 2);

# --- !Downs

DELETE FROM "category" WHERE name="sample1";
DELETE FROM "category" WHERE name="sample2";
