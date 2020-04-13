# --- !Ups

CREATE TABLE "category" (
 "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
 "name" VARCHAR NOT NULL
);

CREATE TABLE "product" (
 "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
 "name" VARCHAR NOT NULL,
 "description" TEXT NOT NULL,
 "price" INTEGER NOT NULL
);

CREATE TABLE "product_category" (
 "productId" INTEGER NOT NULL,
 "categoryId" INTEGER NOT NULL,
 FOREIGN KEY(productId) references product(id),
 FOREIGN KEY(categoryId) references category(id)
);

# --- !Downs

DROP TABLE "category"
DROP TABLE "product"
DROP TABLE "product_category"
