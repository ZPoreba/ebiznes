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

CREATE TABLE "payment" (
 "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
 "date" DATE NOT NULL,
 "status" VARCHAR NOT NULL
);

CREATE TABLE "user" (
    "id" VARCHAR NOT NULL PRIMARY KEY,
    "firstName" VARCHAR,
    "lastName" VARCHAR,
    "fullName" VARCHAR,
    "email" VARCHAR,
    "avatarURL" VARCHAR,
    "address" VARCHAR NOT NULL
);

CREATE TABLE "cart_product" (
 "userId" VARCHAR NOT NULL,
 "productId" INTEGER NOT NULL,
 FOREIGN KEY(userId) references user(id),
 FOREIGN KEY(productId) references product(id)
);

CREATE TABLE "wishlist_product" (
 "userId" VARCHAR NOT NULL,
 "productId" INTEGER NOT NULL,
 FOREIGN KEY(userId) references user(id),
 FOREIGN KEY(productId) references product(id)
);

CREATE TABLE "discount_code" (
 "productId" INTEGER NOT NULL,
 "code" INTEGER NOT NULL,
 FOREIGN KEY(productId) references product(id)
);

CREATE TABLE "return" (
 "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
 "userId" VARCHAR NOT NULL,
 "productId" INTEGER NOT NULL,
 "status" VARCHAR NOT NULL,
 FOREIGN KEY(userId) references user(id),
 FOREIGN KEY(productId) references product(id)
);

CREATE TABLE "opinion" (
 "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
 "userId" VARCHAR NOT NULL,
 "productId" INTEGER NOT NULL,
 "content" VARCHAR NOT NULL,
 FOREIGN KEY(userId) references user(id),
 FOREIGN KEY(productId) references product(id)
);

CREATE TABLE "order_t" (
 "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
 "userId" VARCHAR NOT NULL,
 "paymentId" INTEGER NOT NULL,
 "status" VARCHAR NOT NULL,
 FOREIGN KEY(userId) references user(id),
 FOREIGN KEY(paymentId) references payment(id)
);

CREATE TABLE "order_product" (
 "orderId" INTEGER NOT NULL,
 "productId" INTEGER NOT NULL,
 FOREIGN KEY(orderId) references order_t(id),
 FOREIGN KEY(productId) references product(id)
);

# --- !Downs

DROP TABLE "category"
DROP TABLE "product"
DROP TABLE "product_category"
DROP TABLE "payment"
DROP TABLE "user"
DROP TABLE "cart_product"
DROP TABLE "wishlist_product"
DROP TABLE "discount_code"
DROP TABLE "return"
DROP TABLE "opinion"
DROP TABLE "order_t"
DROP TABLE "order_product"
